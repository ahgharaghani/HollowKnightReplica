package com.sut.hollowknight.model;

import java.util.ArrayList;
import java.util.List;

public class GameData {

    // ---- Slot metadata ----
    public int    slotIndex;
    public String knightName;
    public int    playTimeSeconds;
    public int    completionPercent;
    public String lastArea;
    public boolean empty;

    // ---- Player state ----
    public int     hpMasks;
    public int     maxMasks;
    public int     soulAmount;
    public float   posX;
    public float   posY;
    public String  currentRoom;
    public boolean bossDefeated;
    public int     deathCount;
    public int     enemyKillCount;

    // ---- Achievements (list of unlocked IDs) ----
    public List<String> unlockedAchievementIds = new ArrayList<>();

    // ---- Equipped charms (list of charm names) ----
    public List<String> equippedCharms = new ArrayList<>();

    // ---- Zote (NPC) progress ----
    public boolean zoteMet;          // intro heard; precepts come next
    public int     zotePreceptIndex; // next precept to recite (0-56)

    public static GameData newGame(int slotIndex) {
        GameData d = new GameData();
        d.slotIndex         = slotIndex;
        d.knightName        = "Knight";
        d.playTimeSeconds   = 0;
        d.completionPercent = 0;
        d.lastArea          = "City of Tears";
        d.empty             = false;

        d.hpMasks           = 5;
        d.maxMasks          = 5;
        d.soulAmount        = 0;
        d.posX              = 0;
        d.posY              = 0;
        d.currentRoom       = "forgotten_crossroads_start";
        d.bossDefeated      = false;
        d.deathCount        = 0;
        d.enemyKillCount    = 0;

        return d;
    }
}
