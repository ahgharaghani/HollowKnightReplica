package com.sut.hollowknight.controller;

import com.sut.hollowknight.controller.enemy.CrystalGuardianController;
import com.sut.hollowknight.controller.enemy.EnemyController;
import com.sut.hollowknight.controller.enemy.WingedSentryController;
import com.sut.hollowknight.controller.spell.HowlingWraithController;
import com.sut.hollowknight.controller.spell.VengefulSpiritController;
import com.sut.hollowknight.model.Knight;
import com.sut.hollowknight.model.charms.Charm;
import com.sut.hollowknight.model.collision.AABB;
import com.sut.hollowknight.model.collision.CollisionRect;
import com.sut.hollowknight.model.enemy.Javelin;
import com.sut.hollowknight.model.enemy.Laser;
import com.sut.hollowknight.model.spell.HowlingWraith;
import com.sut.hollowknight.model.spell.VengefulSpirit;

import java.util.List;

public class CombatSystem {

    // Contact damage (enemy body touching the knight)
    private static final int   CONTACT_DAMAGE      = 1;
    private static final float CONTACT_KNOCKBACK_X = 420f;
    private static final float CONTACT_KNOCKBACK_Y = 260f;

    // Javelin damage
    private static final float JAVELIN_KNOCKBACK_X = 380f;
    private static final float JAVELIN_KNOCKBACK_Y = 260f;

    // Crystal Guardian laser damage
    private static final float LASER_KNOCKBACK_X = 420f;
    private static final float LASER_KNOCKBACK_Y = 260f;
    /** March step along the beam when testing the knight's hurtbox. */
    private static final float LASER_SAMPLE_STEP = 12f;

    // Vengeful Spirit — the ball passes through bodies, so knockback is a
    // shove along its travel direction rather than away from the knight.
    private static final float SPIRIT_KNOCKBACK_SCALE = 1.2f;

    // Howling Wraiths — three light ticks; the shove pushes away from the
    // scream's center with a small upward lift.
    private static final float WRAITH_KNOCKBACK_SCALE = 0.6f;

    // Nail attack
    private static final int   NAIL_DAMAGE = 1;
    /** 1 = standard recoil strength (a Heavy Blow charm would pass ~1.75). */
    private static final float NAIL_KNOCKBACK_SCALE = 1f;

    // ---- Charm modifiers (spec: Charms & Inventory System) ----
    /** Unbreakable Strength: nail damage boosted (+50%, rounded up). */
    private static final int   UNBREAKABLE_NAIL_DAMAGE = 2;
    /** Soul Catcher: extra SOUL absorbed per successful nail hit. */
    private static final int   SOUL_CATCHER_BONUS_SOUL = 3;
    /** Heavy Blow: enemies recoil much further when struck. */
    private static final float HEAVY_BLOW_KNOCKBACK_SCALE = 1.75f;
    /** Void Heart: ability (spell) damage is raised by 50%. */
    private static final float VOID_HEART_SPELL_SCALE = 1.5f;

    private final Knight knight;
    private final List<EnemyController> enemies;

    // Sharp Shadow: one hit per enemy per dash, tracked with NEGATIVE ids
    // so they can never collide with the knight's positive attack ids.
    private boolean wasDashing;
    private int sharpShadowDashId = -1;

    public CombatSystem(Knight knight, List<EnemyController> enemies) {
        this.knight = knight;
        this.enemies = enemies;
    }

    public void resolve(float delta) {
        updateSharpShadowDash();
        for (int i = 0; i < enemies.size(); i++) {
            EnemyController enemy = enemies.get(i);
            resolveNailHit(enemy);
            resolveSharpShadowHit(enemy);
            resolveContactDamage(enemy);
            if (enemy instanceof WingedSentryController) {
                resolveJavelinDamage((WingedSentryController) enemy);
            }
            if (enemy instanceof CrystalGuardianController) {
                resolveLaserDamage((CrystalGuardianController) enemy);
            }
        }
    }

    /**
     * Vengeful Spirit vs enemies: the ball passes THROUGH bodies, damaging
     * each enemy at most once per cast (tracked by identity in the model).
     * Spell kills grant no soul — soul is a nail-combat resource.
     */
    public void resolveSpiritHits(List<VengefulSpiritController> spirits) {
        for (int i = 0; i < spirits.size(); i++) {
            VengefulSpirit spirit = spirits.get(i).getSpirit();
            if (!spirit.isFlying()) continue;

            CollisionRect damageBox = spirit.getDamageBox();
            for (int j = 0; j < enemies.size(); j++) {
                EnemyController enemy = enemies.get(j);
                if (!enemy.isAlive()) continue;

                int id = System.identityHashCode(enemy);
                if (!spirit.canHit(id)) continue;
                if (!AABB.overlaps(enemy.getBodyBox(), damageBox)) continue;

                spirit.markHit(id);
                float dirX = spirit.isFacingRight() ? 1f : -1f;
                enemy.hitByNail(spellDamage(VengefulSpirit.DAMAGE), dirX, 0f, SPIRIT_KNOCKBACK_SCALE);
            }
        }
    }

    /**
     * Howling Wraiths vs enemies: the blast is stationary and fires three
     * rapid ticks; every alive enemy inside the hitbox takes each tick.
     * pollTick() is drained in a loop so a long frame cannot swallow ticks.
     */
    public void resolveWraithHits(List<HowlingWraithController> wraiths) {
        for (int i = 0; i < wraiths.size(); i++) {
            HowlingWraith wraith = wraiths.get(i).getWraith();
            while (wraith.pollTick()) {
                CollisionRect damageBox = wraith.getDamageBox();
                for (int j = 0; j < enemies.size(); j++) {
                    EnemyController enemy = enemies.get(j);
                    if (!enemy.isAlive()) continue;

                    AABB body = enemy.getBodyBox();
                    if (!AABB.overlaps(body, damageBox)) continue;

                    float centerX = (body.getLeft() + body.getRight()) / 2f;
                    float dirX = centerX >= wraith.getAnchorX() ? 1f : -1f;
                    enemy.hitByNail(spellDamage(HowlingWraith.DAMAGE_PER_TICK), dirX, 1f,
                        WRAITH_KNOCKBACK_SCALE);
                }
            }
        }
    }

    // ------------------------------------------------------------------
    //  Charm helpers (spec: Charms & Inventory System)
    // ------------------------------------------------------------------

    /** Unbreakable Strength: the nail strikes harder. */
    private int nailDamage() {
        return knight.hasCharm(Charm.UNBREAKABLE_STRENGTH)
            ? UNBREAKABLE_NAIL_DAMAGE : NAIL_DAMAGE;
    }

    /** Heavy Blow: enemies are thrown much further by each hit. */
    private float nailKnockbackScale() {
        return knight.hasCharm(Charm.HEAVY_BLOW)
            ? NAIL_KNOCKBACK_SCALE * HEAVY_BLOW_KNOCKBACK_SCALE
            : NAIL_KNOCKBACK_SCALE;
    }

    /** Void Heart: abilities strike 50% harder. */
    private int spellDamage(int baseDamage) {
        return knight.hasCharm(Charm.VOID_HEART)
            ? Math.round(baseDamage * VOID_HEART_SPELL_SCALE)
            : baseDamage;
    }

    private boolean isShadowDashing() {
        return knight.isDashing() && knight.hasCharm(Charm.SHARP_SHADOW);
    }

    /** Assigns a fresh (negative) hit id to every new Sharp Shadow dash. */
    private void updateSharpShadowDash() {
        boolean dashingNow = knight.isDashing();
        if (dashingNow && !wasDashing) sharpShadowDashId--;
        wasDashing = dashingNow;
    }

    /** Sharp Shadow: dashing through a body cuts it once per dash. */
    private void resolveSharpShadowHit(EnemyController enemy) {
        if (!isShadowDashing() || !enemy.isAlive()) return;
        if (enemy.getLastNailHitId() == sharpShadowDashId) return;
        if (!AABB.overlaps(enemy.getBodyBox(), knight.getHurtBox())) return;

        enemy.setLastNailHitId(sharpShadowDashId);
        float dirX = enemy.getBodyBox().getCenterX() >= knight.getX() ? 1f : -1f;
        enemy.hitByNail(nailDamage(), dirX, 0f, NAIL_KNOCKBACK_SCALE);
    }

    /** Knight's active slash vs the enemy body. One hit per swing per enemy. */
    private void resolveNailHit(EnemyController enemy) {
        if (!knight.isAttacking()) return;
        if (!enemy.isAlive()) return;
        if (enemy.getLastNailHitId() == knight.getAttackId()) return;

        CollisionRect slash = knight.getActiveSlashBox();
        if (slash == null || !AABB.overlaps(enemy.getBodyBox(), slash)) return;

        enemy.setLastNailHitId(knight.getAttackId());

        // The attack direction drives the recoil
        float dirX = 0f;
        float dirY = 0f;
        switch (knight.getState()) {
            case UP_SLASH:   dirY = 1f;  break;
            case DOWN_SLASH: dirY = -1f; break;
            default:
                dirX = enemy.getBodyBox().getCenterX() >= knight.getX() ? 1f : -1f;
                break;
        }
        enemy.hitByNail(nailDamage(), dirX, dirY, nailKnockbackScale());

        // Soul Catcher: nail combat feeds the vessel faster.
        int soulGain = knight.hasCharm(Charm.SOUL_CATCHER)
            ? Knight.SOUL_PER_NAIL_HIT + SOUL_CATCHER_BONUS_SOUL
            : Knight.SOUL_PER_NAIL_HIT;
        knight.addSoul(soulGain);

        if (knight.getState() == Knight.State.DOWN_SLASH) {
            knight.pogoBounce();
        }
    }

    private void resolveContactDamage(EnemyController enemy) {
        if (knight.isInvincible()) return;
        // Sharp Shadow: the shade passes harmlessly through bodies mid-dash.
        if (isShadowDashing()) return;
        if (!enemy.isAlive() || !enemy.overlapsKnight()) return;

        int knockbackDir = knight.getX() < enemy.getBodyBox().getCenterX() ? -1 : 1;
        knight.takeDamage(CONTACT_DAMAGE);
        knight.applyKnockback(knockbackDir * CONTACT_KNOCKBACK_X, CONTACT_KNOCKBACK_Y);
    }

    private void resolveJavelinDamage(WingedSentryController sc) {
        if (knight.isInvincible()) return;

        Javelin javelin = sc.getJavelin();
        if (javelin == null || javelin.getState() != Javelin.State.FLYING) return;

        CollisionRect damageBox = javelin.getDamageBox();
        CollisionRect hurtBox   = knight.getHurtBox();
        if (!AABB.overlaps(damageBox, hurtBox)) return;

        int knockbackDir = knight.getX() < javelin.getCenterX() ? -1 : 1;
        knight.takeDamage(Javelin.DAMAGE);
        knight.applyKnockback(knockbackDir * JAVELIN_KNOCKBACK_X, JAVELIN_KNOCKBACK_Y);
        javelin.setState(Javelin.State.SNAP);
    }

    private void resolveLaserDamage(CrystalGuardianController gc) {
        if (knight.isInvincible()) return;

        Laser laser = gc.getLaser();
        if (laser == null || !laser.isActive() || laser.isHarmless()) return;

        CollisionRect hurtBox = knight.getHurtBox();
        float half   = Laser.THICKNESS / 2f;
        float left   = hurtBox.getLeft()   - half;
        float right  = hurtBox.getRight()  + half;
        float bottom = hurtBox.getBottom() - half;
        float top    = hurtBox.getTop()    + half;

        float ox = laser.getOriginX();
        float oy = laser.getOriginY();
        float dirX = laser.getDirX();
        float dirY = laser.getDirY();
        float length = laser.getLength();

        boolean hit = false;
        for (float t = 0f; t <= length; t += LASER_SAMPLE_STEP) {
            float px = ox + dirX * t;
            float py = oy + dirY * t;
            if (px >= left && px <= right && py >= bottom && py <= top) {
                hit = true;
                break;
            }
        }
        if (!hit) return;

        int knockbackDir = knight.getX() < ox ? -1 : 1;
        knight.takeDamage(Laser.DAMAGE);
        knight.applyKnockback(knockbackDir * LASER_KNOCKBACK_X, LASER_KNOCKBACK_Y);
    }
}
