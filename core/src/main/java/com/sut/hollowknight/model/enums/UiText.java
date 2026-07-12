package com.sut.hollowknight.model.enums;

import com.sut.hollowknight.model.GameSettings;

/**
 * THE single source of truth for every player-facing string in the game,
 * in every supported language (Settings > Language).
 *
 * Architecture notes:
 *  - Each entry carries all translations side by side; get() resolves
 *    against the CURRENT GameSettings language on every call, so any
 *    screen built (or rebuilt) after a language switch is localised
 *    with zero extra plumbing.
 *  - Model enums that need display text (Charm, BindingAction) and the
 *    Achievement class store UiText REFERENCES, not resolved strings -
 *    resolution happens at draw/build time, never at load time.
 *  - get() allocates nothing (returns a stored reference), so it is safe
 *    to call from render paths like the achievement toast.
 *  - Adding a language = one column here + one enum constant in
 *    GameSettings.Language. Nothing else changes.
 */
public enum UiText {

    // ---------------- Main menu ----------------
    START_GAME("Start Game", "Commencer une partie"),
    SETTINGS("Settings", "Paramètres"),
    GUIDE("Guide", "Guide"),
    ACHIEVEMENTS("Achievements", "Succès"),
    QUIT_GAME("Quit Game", "Quitter le jeu"),

    // ---------------- Shared navigation ----------------
    NEW_GAME("New Game", "Nouvelle partie"),
    BACK("Back", "Retour"),
    BACK_CAPS("BACK", "RETOUR"),
    BACK_TO_MAIN_MENU("Back to Main Menu", "Retour au menu principal"),
    BACK_TO_GAME("Back to Game", "Retour au jeu"),
    MAIN_MENU("Main Menu", "Menu principal"),
    RESTART("Restart", "Recommencer"),

    // ---------------- Save slots ----------------
    SLOT_TITLE("Slot %d", "Emplacement %d"),
    EMPTY_SLOT("\u2014 Empty Slot \u2014", "\u2014 Emplacement vide \u2014"),

    // ---------------- Settings ----------------
    MUSIC_VOLUME("Music Volume", "Volume de la musique"),
    MUTE_MUSIC("Mute Music", "Couper la musique"),
    MUTE_SFX("Mute SFX (default reduction)", "Couper les effets (réduction par défaut)"),
    BRIGHTNESS("Brightness", "Luminosité"),
    KEYBOARD_CONTROLS("Keyboard Controls", "Commandes clavier"),
    KEY_BINDINGS("Key Bindings", "Raccourcis clavier"),
    LANGUAGE("Language", "Langue"),
    CHANGE_LANGUAGE("Change Language", "Changer de langue"),
    /** Display name of the CURRENT language, shown next to the toggle. */
    LANGUAGE_NAME("English", "Français"),
    THEME("Theme", "Thème"),
    CHANGE_THEME("Change Theme", "Changer de thème"),

    // ---------------- Key bindings screen ----------------
    KEYBOARD_TITLE("KEYBOARD", "CLAVIER"),
    RESET_DEFAULTS("RESET DEFAULTS", "RÉINITIALISER"),
    ACTION_UP("Up", "Haut"),
    ACTION_DOWN("Down", "Bas"),
    ACTION_LEFT("Left", "Gauche"),
    ACTION_RIGHT("Right", "Droite"),
    ACTION_JUMP("Jump", "Saut"),
    ACTION_ATTACK("Attack", "Attaque"),
    ACTION_DASH("Dash", "Ruée"),
    ACTION_FOCUS_CAST("Focus / Cast", "Focalisation / Sort"),
    ACTION_INVENTORY("Inventory", "Inventaire"),
    ACTION_QUICK_MAP("Quick Map", "Carte rapide"),
    ACTION_SUPER_DASH("Super Dash", "Super ruée"),
    ACTION_DREAM_NAIL("Dream Nail", "Aiguillon des rêves"),
    ACTION_QUICK_CAST("Quick Cast", "Sort rapide"),
    ACTION_PAUSE("Pause", "Pause"),

    // ---------------- Guide ----------------
    ABILITIES("Abilities", "Capacités"),
    CHEAT_CODES("Cheat Codes", "Codes de triche"),
    ABILITIES_BODY(
        "Nail Attack - Strike enemies with your nail to deal damage and gain Soul.\n\n"
            + "Dash - A quick horizontal dash with brief invulnerability.\n\n"
            + "Double Jump - Jump again while airborne.\n\n"
            + "Focus - Channel Soul to heal one mask. Must remain stationary.\n\n"
            + "Vengeful Spirit - Fire a horizontal magic projectile (costs 33 Soul).\n\n"
            + "Howling Wraiths - Upward magic burst dealing 3 rapid hits (costs 33 Soul).\n\n"
            + "Pogo - Downward strike while airborne to bounce off enemies/spikes.",
        "Attaque à l'aiguillon - Frappez les ennemis avec votre aiguillon pour infliger des dégâts et gagner de l'Âme.\n\n"
            + "Ruée - Une ruée horizontale rapide avec une brève invulnérabilité.\n\n"
            + "Double saut - Sautez à nouveau en plein vol.\n\n"
            + "Focalisation - Canalisez l'Âme pour soigner un masque. Vous devez rester immobile.\n\n"
            + "Esprit vengeur - Tirez un projectile magique horizontal (coûte 33 Âme).\n\n"
            + "Spectres hurlants - Explosion magique vers le haut infligeant 3 coups rapides (coûte 33 Âme).\n\n"
            + "Pogo - Frappez vers le bas en plein vol pour rebondir sur les ennemis et les piques."),
    CHEAT_CODES_BODY(
        "Left Ctrl + B - Teleport to Boss Arena\n"
            + "Left Ctrl + F - Noclip / Spectator Mode\n"
            + "Left Ctrl + H - Emergency Heal\n"
            + "Left Ctrl + S - Refill Soul Vessel\n"
            + "Left Ctrl + G - God Mode (toggle)\n"
            + "Left Ctrl + K - Insta-Kill All Enemies",
        "Ctrl gauche + B - Téléportation vers l'arène du boss\n"
            + "Ctrl gauche + F - Noclip / Mode spectateur\n"
            + "Ctrl gauche + H - Soin d'urgence\n"
            + "Ctrl gauche + S - Recharge de la jauge d'Âme\n"
            + "Ctrl gauche + G - Mode Dieu (bascule)\n"
            + "Ctrl gauche + K - Tuer tous les ennemis"),

    // ---------------- Pause ----------------
    PAUSED("PAUSED", "PAUSE"),
    CONTINUE("Continue", "Continuer"),
    SAVE_AND_QUIT("Save and Quit", "Sauvegarder et quitter"),

    // ---------------- End screen ----------------
    END_TITLE("THE FALSE KNIGHT FALLS", "LE FAUX CHEVALIER EST TOMBÉ"),
    END_DEATHS("Deaths", "Morts"),
    END_KILLS("Enemies Felled", "Ennemis vaincus"),
    END_TIME("Time Played", "Temps de jeu"),

    // ---------------- Achievements ----------------
    ACH_COMPLETION_TITLE("Completion", "Achèvement"),
    ACH_COMPLETION_DESC("Finish the game.", "Terminez le jeu."),
    ACH_SPEEDRUN_TITLE("Speedrun", "Speedrun"),
    ACH_SPEEDRUN_DESC("Finish the game in under 10 minutes.", "Terminez le jeu en moins de 10 minutes."),
    ACH_TRUE_HUNTER_TITLE("True Hunter", "Vrai chasseur"),
    ACH_TRUE_HUNTER_DESC("Kill all the enemies.", "Tuez tous les ennemis."),
    ACH_FALSEHOOD_TITLE("Falsehood", "Imposture"),
    ACH_FALSEHOOD_DESC("Defeat the False Knight.", "Vainquez le Faux Chevalier."),
    ACH_CHARMED_TITLE("Charmed", "Charmé"),
    ACH_CHARMED_DESC("Acquire Void Heart.", "Obtenez le Cœur du Vide."),

    // ---------------- Inventory / charms UI ----------------
    CHARMS("Charms", "Charmes"),
    EQUIPPED("Equipped", "Équipés"),
    NOTCHES("Notches", "Encoches"),
    COST("Cost", "Coût"),
    INVENTORY_HINT("Click a charm to equip or unequip   -   Esc / I to close",
        "Cliquez sur un charme pour l'équiper ou le retirer   -   Échap / I pour fermer"),
    COLLECT("COLLECT", "RAMASSER"),
    LISTEN("LISTEN", "ÉCOUTER"),

    // ---------------- Zote dialogue ----------------
    ZOTE_INTRO_1(
        "You there! Why are you skulking about in the shadows?\n"
            + "Yes, your eyes do not deceive you. I am Zote the Mighty, "
            + "a knight of great renown. Tremble before me!",
        "Vous, là ! Pourquoi rôdez-vous ainsi dans l'ombre ?\n"
            + "Oui, vos yeux ne vous trompent pas. Je suis Zote le Puissant, "
            + "un chevalier de grande renommée. Tremblez devant moi !"),
    ZOTE_INTRO_2(
        "While you were hiding here in your dingy little village, I "
            + "ventured into the dark pit below us and slew a great beast. "
            + "It had sharp mandibles and atrocious manners.",
        "Pendant que vous vous cachiez dans votre petit village sordide, "
            + "je me suis aventuré dans le gouffre obscur sous nos pieds et y ai "
            + "terrassé une grande bête. Elle avait des mandibules acérées et des "
            + "manières atroces."),
    ZOTE_INTRO_3(
        "Yes, yes. All glory to me. But I don't have time for your "
            + "adulation! I must rest and prepare for my next journey down.",
        "Oui, oui. Toute la gloire me revient. Mais je n'ai pas le temps "
            + "pour votre adulation ! Je dois me reposer et préparer ma prochaine "
            + "descente."),

    // ---------------- Charm names & descriptions ----------------
    CHARM_SOUL_CATCHER_NAME("Soul Catcher", "Attrape-âme"),
    CHARM_SOUL_CATCHER_DESC(
        "Used by shamans to draw more SOUL from the world.\n\n"
            + "While the bearer is striking enemies with their nail, they "
            + "will absorb a greater amount of SOUL.",
        "Utilisé par les chamans pour tirer davantage d'ÂME du monde.\n\n"
            + "Lorsque le porteur frappe les ennemis avec son aiguillon, "
            + "il absorbe une plus grande quantité d'ÂME."),
    CHARM_DASHMASTER_NAME("Dashmaster", "Maître de la ruée"),
    CHARM_DASHMASTER_DESC(
        "Bears the likeness of an eccentric bug known only as 'The "
            + "Dashmaster'.\n\nThe bearer will be able to dash more often "
            + "as well as dash downwards. Perfect for those who want to "
            + "move around as quickly as possible.",
        "À l'effigie d'un insecte excentrique connu seulement sous le nom "
            + "de « Maître de la ruée ».\n\nLe porteur peut effectuer des "
            + "ruées plus souvent, y compris vers le bas. Parfait pour ceux "
            + "qui veulent se déplacer aussi vite que possible."),
    CHARM_UNBREAKABLE_STRENGTH_NAME("Unbreakable Strength", "Force incassable"),
    CHARM_UNBREAKABLE_STRENGTH_DESC(
        "Strengthens the bearer, increasing the damage they deal to "
            + "enemies with their nail.\n\nThis charm is unbreakable.",
        "Renforce le porteur, augmentant les dégâts qu'il inflige aux "
            + "ennemis avec son aiguillon.\n\nCe charme est incassable."),
    CHARM_QUICK_SLASH_NAME("Quick Slash", "Entaille rapide"),
    CHARM_QUICK_SLASH_DESC(
        "Found embedded in the greatest of the Kingsmoulds. Its blade is "
            + "still sharp with ancient technology.\n\nAllows the bearer to "
            + "slash much more rapidly with their nail.",
        "Trouvé enchâssé dans le plus grand des Moules royaux. Sa lame "
            + "reste affûtée par une technologie ancienne.\n\nPermet au "
            + "porteur de frapper beaucoup plus rapidement avec son aiguillon."),
    CHARM_QUICK_FOCUS_NAME("Quick Focus", "Focalisation rapide"),
    CHARM_QUICK_FOCUS_DESC(
        "A charm containing a crystal lens.\n\nIncreases the speed of "
            + "focusing SOUL, allowing the bearer to heal damage faster.",
        "Un charme contenant une lentille de cristal.\n\nAugmente la "
            + "vitesse de focalisation de l'ÂME, permettant au porteur de "
            + "se soigner plus vite."),
    CHARM_HEAVY_BLOW_NAME("Heavy Blow", "Coup puissant"),
    CHARM_HEAVY_BLOW_DESC(
        "Formed from the shell of a fallen warrior.\n\nIncreases the "
            + "force of the bearer's nail, causing enemies to recoil "
            + "further when hit.",
        "Formé à partir de la carapace d'un guerrier tombé au combat.\n\n"
            + "Augmente la force de l'aiguillon du porteur, faisant reculer "
            + "davantage les ennemis touchés."),
    CHARM_SHARP_SHADOW_NAME("Sharp Shadow", "Ombre acérée"),
    CHARM_SHARP_SHADOW_DESC(
        "Contains the essence of a shadow creature.\n\nWhen using Shadow "
            + "Dash, the bearer's body will sharpen and damage enemies.",
        "Contient l'essence d'une créature de l'ombre.\n\nLors d'une Ruée "
            + "de l'ombre, le corps du porteur s'aiguise et blesse les ennemis."),
    CHARM_VOID_HEART_NAME("Void Heart", "Cœur du Vide"),
    CHARM_VOID_HEART_DESC(
        "An emptiness that was hidden within, now unconstrained.\n\n"
            + "Unifies the void under the bearer's will.",
        "Un vide autrefois caché à l'intérieur, désormais sans entraves.\n\n"
            + "Unifie le Vide sous la volonté du porteur.");

    private final String en;
    private final String fr;

    UiText(String en, String fr) {
        this.en = en;
        this.fr = fr;
    }

    /** The string for the CURRENT language. Allocation-free. */
    public String get() {
        return GameSettings.getInstance().getLanguage() == GameSettings.Language.FRENCH
            ? fr : en;
    }

    /** For parameterised entries like SLOT_TITLE ("Slot %d"). */
    public String format(Object... args) {
        return String.format(get(), args);
    }
}
