package com.sut.hollowknight.model.db;

import com.badlogic.gdx.Gdx;
import com.sut.hollowknight.model.GameData;
import com.sut.hollowknight.model.GameSettings;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GameDatabase {

    private static final String DB_DIR  = System.getProperty("user.home") + "/.hollowknight";
    private static final String DB_PATH = DB_DIR + "/saves.db";
    private static final String DB_URL  = "jdbc:sqlite:" + DB_PATH;

    private static final int SLOT_COUNT = 4;

    // Lifecycle

    public static void initialise() {
        try {
            // Create directory if needed
            java.io.File dir = new java.io.File(DB_DIR);
            if (!dir.exists()) dir.mkdirs();

            try (Connection conn = getConnection()) {
                createTables(conn);
                migrateSettingsTable(conn);
                ensureSlots(conn);
                ensureSettings(conn);
            }
        } catch (SQLException e) {
            Gdx.app.error("GameDatabase", "Failed to initialise database", e);
        }
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    // Schema

    private static void createTables(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS save_slots (" +
                    "  slot_index          INTEGER PRIMARY KEY," +
                    "  knight_name         TEXT    NOT NULL DEFAULT 'Knight'," +
                    "  play_time_seconds   INTEGER NOT NULL DEFAULT 0," +
                    "  completion_percent  INTEGER NOT NULL DEFAULT 0," +
                    "  last_area           TEXT    DEFAULT NULL," +
                    "  is_empty            INTEGER NOT NULL DEFAULT 1" +
                    ")");

            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS player_state (" +
                    "  slot_index     INTEGER PRIMARY KEY REFERENCES save_slots(slot_index)," +
                    "  hp_masks       INTEGER NOT NULL DEFAULT 5," +
                    "  max_masks      INTEGER NOT NULL DEFAULT 5," +
                    "  soul_amount    INTEGER NOT NULL DEFAULT 0," +
                    "  pos_x          REAL    NOT NULL DEFAULT 0," +
                    "  pos_y          REAL    NOT NULL DEFAULT 0," +
                    "  current_room   TEXT    DEFAULT 'forgotten_crossroads_start'," +
                    "  boss_defeated  INTEGER NOT NULL DEFAULT 0," +
                    "  death_count    INTEGER NOT NULL DEFAULT 0," +
                    "  enemy_kills    INTEGER NOT NULL DEFAULT 0" +
                    ")");

            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS slot_achievements (" +
                    "  slot_index      INTEGER NOT NULL REFERENCES save_slots(slot_index)," +
                    "  achievement_id  TEXT    NOT NULL," +
                    "  PRIMARY KEY (slot_index, achievement_id)" +
                    ")");

            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS slot_charms (" +
                    "  slot_index  INTEGER NOT NULL REFERENCES save_slots(slot_index)," +
                    "  charm_name  TEXT    NOT NULL," +
                    "  PRIMARY KEY (slot_index, charm_name)" +
                    ")");

            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS settings (" +
                    "  id                INTEGER PRIMARY KEY CHECK (id = 1)," +
                    "  music_volume      REAL    NOT NULL DEFAULT 0.7," +
                    "  music_muted       INTEGER NOT NULL DEFAULT 0," +
                    "  sfx_muted         INTEGER NOT NULL DEFAULT 0," +
                    "  sfx_volume        REAL    NOT NULL DEFAULT 0.8," +
                    "  brightness        REAL    NOT NULL DEFAULT 1.0," +
                    "  language          TEXT    NOT NULL DEFAULT 'ENGLISH'," +
                    "  menu_theme        TEXT    NOT NULL DEFAULT 'THEME_01'," +
                    "  move_left_key     INTEGER NOT NULL DEFAULT 29," +   // A
                    "  move_right_key    INTEGER NOT NULL DEFAULT 32," +   // D
                    "  jump_key          INTEGER NOT NULL DEFAULT 62," +   // Space
                    "  attack_key        INTEGER NOT NULL DEFAULT 1000," + // Mouse LEFT
                    "  dash_key          INTEGER NOT NULL DEFAULT 129," +  // L Shift
                    "  move_up_key       INTEGER NOT NULL DEFAULT 51," +   // W
                    "  move_down_key     INTEGER NOT NULL DEFAULT 47," +   // S
                    "  focus_cast_key    INTEGER NOT NULL DEFAULT 45," +   // Q
                    "  inventory_key     INTEGER NOT NULL DEFAULT 37," +   // I
                    "  quick_map_key     INTEGER NOT NULL DEFAULT 41," +   // M
                    "  super_dash_key    INTEGER NOT NULL DEFAULT 46," +   // R
                    "  dream_nail_key    INTEGER NOT NULL DEFAULT 1001," + // Mouse RIGHT
                    "  quick_cast_key    INTEGER NOT NULL DEFAULT 36" +    // F
                    ")");
        }
    }

    private static void migrateSettingsTable(Connection conn) throws SQLException {
        String[][] newColumns = {
            {"move_up_key",       "51"},   // Input.Keys.W
            {"move_down_key",     "47"},   // Input.Keys.S
            {"focus_cast_key",    "45"},   // Input.Keys.Q
            {"inventory_key",     "37"},   // Input.Keys.I
            {"quick_map_key",     "41"},   // Input.Keys.M
            {"super_dash_key",    "46"},   // Input.Keys.R
            {"dream_nail_key",    "1001"}, // MOUSE_OFFSET + RIGHT
            {"quick_cast_key",    "36"},   // Input.Keys.F
        };
        try (Statement st = conn.createStatement()) {
            for (String[] col : newColumns) {
                try {
                    st.executeUpdate(
                        "ALTER TABLE settings ADD COLUMN " + col[0] +
                            " INTEGER NOT NULL DEFAULT " + col[1]);
                    Gdx.app.log("GameDatabase",
                        "Added column settings." + col[0]);
                } catch (SQLException e) {
                }
            }
        }
    }

    private static void ensureSlots(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
            "INSERT OR IGNORE INTO save_slots (slot_index) VALUES (?)")) {
            for (int i = 1; i <= SLOT_COUNT; i++) {
                ps.setInt(1, i);
                ps.executeUpdate();
            }
        }
        // Also ensure every slot has a player_state row
        try (PreparedStatement ps = conn.prepareStatement(
            "INSERT OR IGNORE INTO player_state (slot_index) VALUES (?)")) {
            for (int i = 1; i <= SLOT_COUNT; i++) {
                ps.setInt(1, i);
                ps.executeUpdate();
            }
        }
    }

    private static void ensureSettings(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
            "INSERT OR IGNORE INTO settings (id) VALUES (1)")) {
            ps.executeUpdate();
        }
    }

    // Read

    public static GameData load(int slotIndex) {
        GameData data = new GameData();
        data.slotIndex = slotIndex;

        try (Connection conn = getConnection()) {
            // Slot metadata
            try (PreparedStatement ps = conn.prepareStatement(
                "SELECT knight_name, play_time_seconds, completion_percent, " +
                    "       last_area, is_empty FROM save_slots WHERE slot_index = ?")) {
                ps.setInt(1, slotIndex);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        data.knightName        = rs.getString("knight_name");
                        data.playTimeSeconds   = rs.getInt("play_time_seconds");
                        data.completionPercent = rs.getInt("completion_percent");
                        data.lastArea          = rs.getString("last_area");
                        data.empty             = rs.getInt("is_empty") == 1;
                    }
                }
            }

            // Player state
            try (PreparedStatement ps = conn.prepareStatement(
                "SELECT hp_masks, max_masks, soul_amount, pos_x, pos_y, " +
                    "       current_room, boss_defeated, death_count, enemy_kills " +
                    "FROM player_state WHERE slot_index = ?")) {
                ps.setInt(1, slotIndex);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        data.hpMasks       = rs.getInt("hp_masks");
                        data.maxMasks      = rs.getInt("max_masks");
                        data.soulAmount    = rs.getInt("soul_amount");
                        data.posX          = rs.getFloat("pos_x");
                        data.posY          = rs.getFloat("pos_y");
                        data.currentRoom   = rs.getString("current_room");
                        data.bossDefeated  = rs.getInt("boss_defeated") == 1;
                        data.deathCount    = rs.getInt("death_count");
                        data.enemyKillCount = rs.getInt("enemy_kills");
                    }
                }
            }

            // Achievements
            data.unlockedAchievementIds = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(
                "SELECT achievement_id FROM slot_achievements WHERE slot_index = ?")) {
                ps.setInt(1, slotIndex);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        data.unlockedAchievementIds.add(rs.getString("achievement_id"));
                    }
                }
            }

            // Charms
            data.equippedCharms = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(
                "SELECT charm_name FROM slot_charms WHERE slot_index = ?")) {
                ps.setInt(1, slotIndex);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        data.equippedCharms.add(rs.getString("charm_name"));
                    }
                }
            }

        } catch (SQLException e) {
            Gdx.app.error("GameDatabase", "Failed to load slot " + slotIndex, e);
        }

        return data;
    }

    public static List<GameData> loadAll() {
        List<GameData> list = new ArrayList<>();
        for (int i = 1; i <= SLOT_COUNT; i++) {
            list.add(load(i));
        }
        return list;
    }

    // Write

    public static void save(GameData data) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                int i = data.slotIndex;

                // Upsert save_slots
                try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE save_slots SET " +
                        "  knight_name = ?, play_time_seconds = ?, completion_percent = ?, " +
                        "  last_area = ?, is_empty = ? " +
                        "WHERE slot_index = ?")) {
                    ps.setString(1, data.knightName);
                    ps.setInt(2, data.playTimeSeconds);
                    ps.setInt(3, data.completionPercent);
                    ps.setString(4, data.lastArea);
                    ps.setInt(5, data.empty ? 1 : 0);
                    ps.setInt(6, i);
                    ps.executeUpdate();
                }

                // Upsert player_state
                try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE player_state SET " +
                        "  hp_masks = ?, max_masks = ?, soul_amount = ?, " +
                        "  pos_x = ?, pos_y = ?, current_room = ?, " +
                        "  boss_defeated = ?, death_count = ?, enemy_kills = ? " +
                        "WHERE slot_index = ?")) {
                    ps.setInt(1, data.hpMasks);
                    ps.setInt(2, data.maxMasks);
                    ps.setInt(3, data.soulAmount);
                    ps.setFloat(4, data.posX);
                    ps.setFloat(5, data.posY);
                    ps.setString(6, data.currentRoom);
                    ps.setInt(7, data.bossDefeated ? 1 : 0);
                    ps.setInt(8, data.deathCount);
                    ps.setInt(9, data.enemyKillCount);
                    ps.setInt(10, i);
                    ps.executeUpdate();
                }

                // Replace achievements
                try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM slot_achievements WHERE slot_index = ?")) {
                    ps.setInt(1, i);
                    ps.executeUpdate();
                }
                if (data.unlockedAchievementIds != null) {
                    try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO slot_achievements (slot_index, achievement_id) VALUES (?, ?)")) {
                        for (String id : data.unlockedAchievementIds) {
                            ps.setInt(1, i);
                            ps.setString(2, id);
                            ps.executeUpdate();
                        }
                    }
                }

                // Replace charms
                try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM slot_charms WHERE slot_index = ?")) {
                    ps.setInt(1, i);
                    ps.executeUpdate();
                }
                if (data.equippedCharms != null) {
                    try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO slot_charms (slot_index, charm_name) VALUES (?, ?)")) {
                        for (String charm : data.equippedCharms) {
                            ps.setInt(1, i);
                            ps.setString(2, charm);
                            ps.executeUpdate();
                        }
                    }
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            Gdx.app.error("GameDatabase", "Failed to save slot " + data.slotIndex, e);
        }
    }

    public static void clearSlot(int slotIndex) {
        GameData empty = new GameData();
        empty.slotIndex = slotIndex;
        empty.empty = true;
        empty.knightName = "Knight";
        empty.hpMasks = 5;
        empty.maxMasks = 5;
        save(empty);
    }

    // Settings persistence

    public static void loadSettings() {
        GameSettings settings = GameSettings.getInstance();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT music_volume, music_muted, sfx_muted, sfx_volume, " +
                     "       brightness, language, menu_theme, " +
                     "       move_left_key, move_right_key, jump_key, attack_key, dash_key, " +
                     "       move_up_key, move_down_key, focus_cast_key, inventory_key, " +
                     "       quick_map_key, super_dash_key, dream_nail_key, quick_cast_key " +
                     "FROM settings WHERE id = 1");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                settings.setMusicVolume(rs.getFloat("music_volume"));
                settings.setMusicMuted(rs.getInt("music_muted") == 1);
                settings.setSfxMuted(rs.getInt("sfx_muted") == 1);
                settings.setSfxVolume(rs.getFloat("sfx_volume"));
                settings.setBrightness(rs.getFloat("brightness"));
                settings.setLanguage(GameSettings.Language.valueOf(rs.getString("language")));
                settings.setCurrentMenuTheme(
                    com.sut.hollowknight.model.enums.MenuTheme.valueOf(rs.getString("menu_theme")));
                settings.setMoveLeftKey(rs.getInt("move_left_key"));
                settings.setMoveRightKey(rs.getInt("move_right_key"));
                settings.setJumpKey(rs.getInt("jump_key"));
                settings.setAttackKey(rs.getInt("attack_key"));
                settings.setDashKey(rs.getInt("dash_key"));
                settings.setMoveUpKey(rs.getInt("move_up_key"));
                settings.setMoveDownKey(rs.getInt("move_down_key"));
                settings.setFocusCastKey(rs.getInt("focus_cast_key"));
                settings.setInventoryKey(rs.getInt("inventory_key"));
                settings.setQuickMapKey(rs.getInt("quick_map_key"));
                settings.setSuperDashKey(rs.getInt("super_dash_key"));
                settings.setDreamNailKey(rs.getInt("dream_nail_key"));
                settings.setQuickCastKey(rs.getInt("quick_cast_key"));
            }
        } catch (SQLException e) {
            Gdx.app.error("GameDatabase", "Failed to load settings", e);
        }
    }

    public static void saveSettings() {
        GameSettings s = GameSettings.getInstance();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "UPDATE settings SET " +
                     "  music_volume = ?, music_muted = ?, sfx_muted = ?, sfx_volume = ?, " +
                     "  brightness = ?, language = ?, menu_theme = ?, " +
                     "  move_left_key = ?, move_right_key = ?, jump_key = ?, " +
                     "  attack_key = ?, dash_key = ?, " +
                     "  move_up_key = ?, move_down_key = ?, focus_cast_key = ?, " +
                     "  inventory_key = ?, quick_map_key = ?, super_dash_key = ?, " +
                     "  dream_nail_key = ?, quick_cast_key = ? " +
                     "WHERE id = 1")) {
            ps.setFloat(1, s.getMusicVolume());
            ps.setInt(2, s.isMusicMuted() ? 1 : 0);
            ps.setInt(3, s.isSfxMuted() ? 1 : 0);
            ps.setFloat(4, s.getSfxVolume());
            ps.setFloat(5, s.getBrightness());
            ps.setString(6, s.getLanguage().name());
            ps.setString(7, s.getCurrentMenuTheme().name());
            ps.setInt(8, s.getMoveLeftKey());
            ps.setInt(9, s.getMoveRightKey());
            ps.setInt(10, s.getJumpKey());
            ps.setInt(11, s.getAttackKey());
            ps.setInt(12, s.getDashKey());
            ps.setInt(13, s.getMoveUpKey());
            ps.setInt(14, s.getMoveDownKey());
            ps.setInt(15, s.getFocusCastKey());
            ps.setInt(16, s.getInventoryKey());
            ps.setInt(17, s.getQuickMapKey());
            ps.setInt(18, s.getSuperDashKey());
            ps.setInt(19, s.getDreamNailKey());
            ps.setInt(20, s.getQuickCastKey());
            ps.executeUpdate();
        } catch (SQLException e) {
            Gdx.app.error("GameDatabase", "Failed to save settings", e);
        }
    }
}
