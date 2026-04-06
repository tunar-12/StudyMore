package StudyMore.db;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.json.*;

import StudyMore.models.*;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:studymore_database.db";
    private Connection connection;

    private static final java.util.Map<String, CosmeticType> TYPE_MAP = java.util.Map.of(
            "CATHOUSE", CosmeticType.MASCOT_HOUSE,
            "MASCOT_HOUSE", CosmeticType.MASCOT_HOUSE,
            "MASCOT_SKIN", CosmeticType.MASCOT_SKIN,
            "BANNER", CosmeticType.BANNER,
            "BACKGROUND", CosmeticType.BACKGROUND,
            "MEDAL", CosmeticType.MEDAL,
            "TITLE", CosmeticType.TITLE,
            "AVATAR", CosmeticType.AVATAR);

    public DatabaseManager() {
        initilizeDB();
        insertAssets();
    }

    private void initilizeDB() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            createTables();
            System.out.println("Database connected successfully!");
        } catch (SQLException e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
        }
    }

    private void createTables() {
        String createTasksTable = """
                    CREATE TABLE IF NOT EXISTS tasks (
                        id INTEGER PRIMARY KEY,
                        user_id INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        content TEXT,
                        is_complete INTEGER NOT NULL DEFAULT 0,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        srs_enabled INTEGER NOT NULL DEFAULT 0,
                        -- SRS Specific Statistics
                        repetition_count INTEGER DEFAULT 0,
                        ease_factor REAL DEFAULT 2.5,
                        current_interval INTEGER DEFAULT 0,
                        review_intensity TEXT DEFAULT 'STANDARD',
                        next_recall_date TEXT,
                        FOREIGN KEY (user_id) REFERENCES users(id)
                    );
                """;
        String createTaskHistoryTable = """
                    CREATE TABLE IF NOT EXISTS task_srs_history (
                        id INTEGER PRIMARY KEY,
                        task_id INTEGER NOT NULL,
                        ease_factor_at_time REAL,
                        interval_at_time INTEGER,
                        quality_score INTEGER,
                        timestamp TIMESTAMP,
                        FOREIGN KEY (task_id) REFERENCES tasks(id)
                    );
                """;

        String createMultipliersTable = """
                    CREATE TABLE IF NOT EXISTS multipliers (
                        id INTEGER PRIMARY KEY,
                        user_id INTEGER NOT NULL UNIQUE,
                        current_value REAL NOT NULL DEFAULT 1.0,
                        max_value REAL NOT NULL DEFAULT 5.0,
                        increment_interval INTEGER NOT NULL DEFAULT 0,
                        cooldown_interval INTEGER NOT NULL DEFAULT 0,
                        last_active_time TIMESTAMP,
                        FOREIGN KEY (user_id) REFERENCES users(id)
                    );
                """;

        String createCosmeticsTable = """
                    CREATE TABLE IF NOT EXISTS cosmetics (
                        id INTEGER PRIMARY KEY,
                        name TEXT NOT NULL,
                        type TEXT NOT NULL,
                        price INTEGER NOT NULL DEFAULT 0,
                        image_path TEXT,
                        description TEXT
                    );
                """;

        String createInventoryTable = """
                    CREATE TABLE IF NOT EXISTS inventory (
                        id INTEGER PRIMARY KEY,
                        user_id INTEGER NOT NULL UNIQUE,
                        FOREIGN KEY (user_id) REFERENCES users(id)
                    );
                """;

        String createInventoryOwnedItemsTable = """
                    CREATE TABLE IF NOT EXISTS inventory_owned_items (
                        inventory_id INTEGER NOT NULL,
                        cosmetic_id INTEGER NOT NULL,
                        PRIMARY KEY (inventory_id, cosmetic_id),
                        FOREIGN KEY (inventory_id) REFERENCES inventory(id),
                        FOREIGN KEY (cosmetic_id) REFERENCES cosmetics(id)
                    );
                """;

        String createInventoryEquippedItemsTable = """
                    CREATE TABLE IF NOT EXISTS inventory_equipped_items (
                        inventory_id INTEGER NOT NULL,
                        cosmetic_type TEXT NOT NULL,
                        cosmetic_id INTEGER NOT NULL,
                        PRIMARY KEY (inventory_id, cosmetic_type),
                        FOREIGN KEY (inventory_id) REFERENCES inventory(id),
                        FOREIGN KEY (cosmetic_id) REFERENCES cosmetics(id)
                    );
                """;

        String createSettingsTable = """
                    CREATE TABLE IF NOT EXISTS settings (
                        id INTEGER PRIMARY KEY,
                        user_id INTEGER NOT NULL UNIQUE,
                        dark_mode INTEGER NOT NULL DEFAULT 0,
                        lock_in_mode INTEGER NOT NULL DEFAULT 0,
                        show_mascot INTEGER NOT NULL DEFAULT 1,
                        study_time INTEGER NOT NULL DEFAULT 25,
                        short_break INTEGER NOT NULL DEFAULT 5,
                        long_break INTEGER NOT NULL DEFAULT 15,
                        long_break_after INTEGER NOT NULL DEFAULT 4,
                        start_sound INTEGER NOT NULL DEFAULT 1,
                        break_alert INTEGER NOT NULL DEFAULT 1,
                        popups INTEGER NOT NULL DEFAULT 1,
                        FOREIGN KEY (user_id) REFERENCES users(id)
                    );
                """;

        String createAchievementsTable = """
                    CREATE TABLE IF NOT EXISTS achievements (
                        id INTEGER PRIMARY KEY,
                        title TEXT NOT NULL,
                        description TEXT,
                        type TEXT NOT NULL,
                        target_value INTEGER NOT NULL DEFAULT 0,
                        reward INTEGER NOT NULL DEFAULT 0,
                        icon_path TEXT
                    );
                """;

        String createUserAchievementsTable = """
                    CREATE TABLE IF NOT EXISTS user_achievements (
                        id INTEGER PRIMARY KEY,
                        user_id INTEGER NOT NULL,
                        achievement_id INTEGER NOT NULL,
                        progress INTEGER NOT NULL DEFAULT 0,
                        is_completed INTEGER NOT NULL DEFAULT 0,
                        completed_at TIMESTAMP,
                        UNIQUE (user_id, achievement_id),
                        FOREIGN KEY (user_id) REFERENCES users(id),
                        FOREIGN KEY (achievement_id) REFERENCES achievements(id)
                    );
                """;

        String createStudyGroupsTable = """
                    CREATE TABLE IF NOT EXISTS study_groups (
                        id INTEGER PRIMARY KEY,
                        title TEXT NOT NULL,
                        host_id INTEGER NOT NULL,
                        study_goal INTEGER NOT NULL DEFAULT 0,
                        max_members INTEGER NOT NULL DEFAULT 10,
                        is_active INTEGER NOT NULL DEFAULT 1,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (host_id) REFERENCES users(id)
                    );
                """;

        String createStudyGroupMembersTable = """
                    CREATE TABLE IF NOT EXISTS study_group_members (
                        group_id INTEGER NOT NULL,
                        user_id INTEGER NOT NULL,
                        PRIMARY KEY (group_id, user_id),
                        FOREIGN KEY (group_id) REFERENCES study_groups(id),
                        FOREIGN KEY (user_id) REFERENCES users(id)
                    );
                """;

        String createFriendRequestsTable = """
                    CREATE TABLE IF NOT EXISTS friend_requests (
                        id INTEGER PRIMARY KEY,
                        sender_id INTEGER NOT NULL,
                        receiver_id INTEGER NOT NULL,
                        status TEXT NOT NULL DEFAULT 'PENDING',
                        sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        UNIQUE (sender_id, receiver_id),
                        FOREIGN KEY (sender_id) REFERENCES users(id),
                        FOREIGN KEY (receiver_id) REFERENCES users(id)
                    );
                """;

        String createFriendsTable = """
                    CREATE TABLE IF NOT EXISTS friends (
                        user_id INTEGER NOT NULL,
                        friend_id INTEGER NOT NULL,
                        PRIMARY KEY (user_id, friend_id),
                        FOREIGN KEY (user_id) REFERENCES users(id),
                        FOREIGN KEY (friend_id) REFERENCES users(id)
                    );
                """;

        String createUserStatsTable = """
                    CREATE TABLE IF NOT EXISTS user_stats (
                        user_id INTEGER PRIMARY KEY,
                        rank TEXT NOT NULL DEFAULT 'BRONZE',
                        rating INTEGER NOT NULL DEFAULT 0,
                        coin_balance INTEGER NOT NULL DEFAULT 0,
                        study_streak INTEGER NOT NULL DEFAULT 0,
                        total_study_time INTEGER NOT NULL DEFAULT 0,
                        daily_study_time INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY (user_id) REFERENCES users(id)
                    );
                """;

        String createUsersTable = """
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY,
                        username TEXT NOT NULL UNIQUE,
                        email TEXT NOT NULL UNIQUE,
                        password_hash TEXT NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    );
                """;

        String createSessionsTable = """
                    CREATE TABLE IF NOT EXISTS sessions (
                        id INTEGER PRIMARY KEY,
                        user_id INTEGER NOT NULL,
                        start_time TIMESTAMP NOT NULL,
                        end_time TIMESTAMP,
                        multiplier_value REAL NOT NULL DEFAULT 1.0,
                        coins_earned INTEGER NOT NULL DEFAULT 0,
                        duration INTEGER NOT NULL DEFAULT 0,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (user_id) REFERENCES users(id)
                    );
                """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createUserStatsTable);
            stmt.execute(createSessionsTable);
            stmt.execute(createTasksTable);
            stmt.execute(createTaskHistoryTable);
            stmt.execute(createMultipliersTable);
            stmt.execute(createCosmeticsTable);
            stmt.execute(createInventoryTable);
            stmt.execute(createInventoryOwnedItemsTable);
            stmt.execute(createInventoryEquippedItemsTable);
            stmt.execute(createSettingsTable);
            stmt.execute(createAchievementsTable);
            stmt.execute(createUserAchievementsTable);
            stmt.execute(createStudyGroupsTable);
            stmt.execute(createStudyGroupMembersTable);
            stmt.execute(createFriendRequestsTable);
            stmt.execute(createFriendsTable);

            System.out.println("Database Succesfuly initilized");
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }

    public User getUser(long id) {
        String userQuery = """
                SELECT u.id, u.username, u.email, u.password_hash,
                    us.rank, us.rating, us.coin_balance, us.study_streak,
                    us.total_study_time, us.daily_study_time
                FROM users u
                LEFT JOIN user_stats us ON u.id = us.user_id
                WHERE u.id = ?
                """;

        String inventoryQuery = """
                SELECT i.id AS inventory_id,
                    c.id AS cosmetic_id, c.name, c.type, c.price, c.image_path, c.description,
                    ie.cosmetic_type AS equipped_type
                FROM inventory i
                LEFT JOIN inventory_owned_items ioi ON i.id = ioi.inventory_id
                LEFT JOIN cosmetics c ON ioi.cosmetic_id = c.id
                LEFT JOIN inventory_equipped_items ie ON i.id = ie.inventory_id AND ie.cosmetic_id = c.id
                WHERE i.user_id = ?
                """;

        String friendsQuery = """
                SELECT friend_id FROM friends WHERE user_id = ?
                """;

        String tasksQuery = """
                SELECT id, title, content, srs_enabled, is_complete, next_recall_date, created_at,
                    repetition_count, ease_factor, current_interval, review_intensity
                FROM tasks WHERE user_id = ?
                """;

        try (PreparedStatement userStmt = connection.prepareStatement(userQuery)) {
            userStmt.setLong(1, id);

            try (ResultSet rs = userStmt.executeQuery()) {
                if (!rs.next())
                    return null; // user not found

                long userId = rs.getLong("id");

                // Build Inventory
                Inventory inventory = new Inventory((long) id, null);
                try (PreparedStatement invStmt = connection.prepareStatement(inventoryQuery)) {
                    invStmt.setLong(1, id);
                    try (ResultSet invRs = invStmt.executeQuery()) {
                        while (invRs.next()) {
                            long cosmeticId = invRs.getLong("cosmetic_id");
                            if (cosmeticId == 0)
                                continue;

                            Cosmetic cosmetic = new Cosmetic(
                                    cosmeticId,
                                    invRs.getString("name"),
                                    CosmeticType.valueOf(invRs.getString("type")),
                                    invRs.getInt("price"),
                                    invRs.getString("image_path"),
                                    invRs.getString("description"));
                            inventory.addItem(cosmetic);

                            String equippedType = invRs.getString("equipped_type");
                            if (equippedType != null) {
                                inventory.equipItem(cosmetic);
                            }
                        }
                    }
                }

                MascotCat mascotCat = new MascotCat(userId);
                Cosmetic skin = inventory.getEquipped(CosmeticType.MASCOT_SKIN);
                Cosmetic house = inventory.getEquipped(CosmeticType.MASCOT_HOUSE);
                if (skin != null)
                    mascotCat.changeSkin(skin);
                if (house != null)
                    mascotCat.changeMascotHouse(house);

                // Build Friends list (shallow — only IDs to avoid infinite recursion)
                ArrayList<User> friends = new ArrayList<>();
                try (PreparedStatement friendsStmt = connection.prepareStatement(friendsQuery)) {
                    friendsStmt.setLong(1, id);
                    try (ResultSet friendsRs = friendsStmt.executeQuery()) {
                        while (friendsRs.next()) {
                            friends.add(new User(friendsRs.getLong("friend_id"))); // uses the User(Long) constructor
                        }
                    }
                }

                // build tasks
                ArrayList<Task> tasks = new ArrayList<>();
                try (PreparedStatement tasksStmt = connection.prepareStatement(tasksQuery)) {
                    tasksStmt.setLong(1, id);
                    try (ResultSet tasksRs = tasksStmt.executeQuery()) {
                        while (tasksRs.next()) {
                            String intensityStr = tasksRs.getString("review_intensity");
                            ReviewIntensity intensity = (intensityStr != null) ? ReviewIntensity.valueOf(intensityStr)
                                    : ReviewIntensity.STANDARD;
                            String createdAtStr = tasksRs.getString("created_at");

                            Task task = new Task(
                                    tasksRs.getString("title"),
                                    tasksRs.getString("content"),
                                    tasksRs.getInt("srs_enabled") == 1,
                                    intensity);

                            task.setCompleted(tasksRs.getInt("is_complete") == 1);
                            task.setTaskId(tasksRs.getLong("id"));
                            task.setCreatedAtFromString(createdAtStr);

                            if (task.isSrsEnabled() && task.getSrsData() != null) {
                                task.getSrsData().setRepetitionCount(tasksRs.getInt("repetition_count"));
                                task.getSrsData().setCurrentEaseFactor(tasksRs.getDouble("ease_factor"));
                                task.getSrsData().setCurrentInterval(tasksRs.getInt("current_interval"));
                            }

                            tasks.add(task);
                        }
                    }
                }

                // make user
                User user = new User(
                        userId,
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getString("rank") != null ? Rank.valueOf(rs.getString("rank")) : Rank.BRONZE,
                        rs.getInt("rating"), // returns 0 if null
                        rs.getInt("coin_balance"), // returns 0 if null
                        rs.getInt("study_streak"), // returns 0 if null
                        rs.getLong("total_study_time"),
                        rs.getLong("daily_study_time"),
                        mascotCat,
                        inventory,
                        friends,
                        tasks);

                return user;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public StudySession getTodaysStudySession(User user) {

        String query = """
                SELECT s.id, s.start_time, s.end_time, s.multiplier_value, s.coins_earned, s.duration
                FROM sessions s
                WHERE s.user_id = ?
                AND DATE(s.start_time, 'localtime') = DATE('now', 'localtime')
                ORDER BY s.start_time DESC
                LIMIT 1
                """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, user.getUserId());

            // I left your debug block intact so you can still track timezone weirdness if
            // needed!
            try (PreparedStatement debugStmt = connection.prepareStatement(
                    "SELECT id, user_id, start_time, DATE(start_time), DATE('now'), DATE('now','localtime') FROM sessions")) {
                try (ResultSet debugRs = debugStmt.executeQuery()) {
                    while (debugRs.next()) {
                        System.out.println("DEBUG SESSION ROW: id=" + debugRs.getLong(1)
                                + " user_id=" + debugRs.getLong(2)
                                + " start_time=" + debugRs.getString(3)
                                + " DATE(start_time)=" + debugRs.getString(4)
                                + " DATE('now')=" + debugRs.getString(5)
                                + " DATE('now','localtime')=" + debugRs.getString(6));
                    }
                }
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next())
                    return null;

                Multiplier multiplier = new Multiplier(rs.getDouble("multiplier_value"));

                // Safely grab the raw SQLite strings
                String startStr = rs.getString("start_time");
                String endStr = rs.getString("end_time");

                // Parse them manually using the formatter
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime startTime = (startStr != null) ? LocalDateTime.parse(startStr, formatter) : null;
                LocalDateTime endTime = (endStr != null) ? LocalDateTime.parse(endStr, formatter) : null;

                StudySession session = new StudySession(user,
                        rs.getLong("id"),
                        startTime,
                        endTime,
                        multiplier,
                        rs.getInt("duration"),
                        rs.getInt("coins_earned"));

                return session;
            }

        } catch (SQLException e) {
            System.err.println("Database fetch failed for today's session: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<StudySession> getStudySessions(User user) {
        ArrayList<StudySession> sessionsList = new ArrayList<>();
        // Use the exact same formatter you used in StudySession.java
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String query = """
                SELECT s.id, s.start_time, s.end_time, s.multiplier_value, s.coins_earned, s.duration
                FROM sessions s
                WHERE s.user_id = ?
                ORDER BY s.start_time DESC
                """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, user.getUserId());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Multiplier multiplier = new Multiplier(rs.getDouble("multiplier_value"));

                    // Safely grab the raw SQLite strings
                    String startStr = rs.getString("start_time");
                    String endStr = rs.getString("end_time");

                    // Parse them manually to completely bypass JDBC Timestamp crashes
                    LocalDateTime startTime = (startStr != null) ? LocalDateTime.parse(startStr, formatter) : null;
                    LocalDateTime endTime = (endStr != null) ? LocalDateTime.parse(endStr, formatter) : null;

                    StudySession session = new StudySession(user,
                            rs.getLong("id"),
                            startTime,
                            endTime,
                            multiplier,
                            rs.getInt("duration"),
                            rs.getInt("coins_earned"));

                    sessionsList.add(session);
                }
            }

        } catch (SQLException e) {
            System.err.println("Database fetch failed: " + e.getMessage());
            e.printStackTrace();
        }

        return sessionsList;
    }

    private JSONArray loadJsonArray(String resourcePath) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null)
                return null;
            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return new JSONArray(content);
        } catch (Exception e) {
            System.err.println("Failed to read " + resourcePath + ": " + e.getMessage());
            return null;
        }
    }

    public boolean obtainCosmetic(long userId, long cosmeticId) {
        // Look up the user's inventory id.
        String getInventoryId = "SELECT id FROM inventory WHERE user_id = ?";
        String insertOwned = """
                INSERT INTO inventory_owned_items (inventory_id, cosmetic_id)
                VALUES (?, ?)
                ON CONFLICT(inventory_id, cosmetic_id) DO NOTHING
                """;

        try (PreparedStatement invStmt = connection.prepareStatement(getInventoryId)) {
            invStmt.setLong(1, userId);
            try (ResultSet rs = invStmt.executeQuery()) {
                if (!rs.next()) {
                    System.err.println("No inventory found for user " + userId);
                    return false;
                }
                long inventoryId = rs.getLong("id");

                try (PreparedStatement ownedStmt = connection.prepareStatement(insertOwned)) {
                    ownedStmt.setLong(1, inventoryId);
                    ownedStmt.setLong(2, cosmeticId);
                    int affected = ownedStmt.executeUpdate();
                    if (affected == 0) {
                        System.out.println("Cosmetic " + cosmeticId + " already owned by user " + userId);
                        return false; // already owned
                    }
                    System.out.println("Cosmetic " + cosmeticId + " added to inventory of user " + userId);
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("obtainCosmetic failed: " + e.getMessage());
            return false;
        }
    }

    public void initializeNewUserInventory(long userId) {
        // Added SQL to insert the base inventory row
        String createInventorySQL = "INSERT INTO inventory (user_id) VALUES (?) ON CONFLICT DO NOTHING";
        String getInventoryId = "SELECT id FROM inventory WHERE user_id = ?";
        String firstCosmeticSQL = "SELECT id FROM cosmetics WHERE type = ? ORDER BY id ASC LIMIT 1";
        String insertOwnedSQL = "INSERT INTO inventory_owned_items (inventory_id, cosmetic_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        String equipSQL = "INSERT INTO inventory_equipped_items (inventory_id, cosmetic_type, cosmetic_id) VALUES (?, ?, ?) ON CONFLICT DO NOTHING";

        try {
            // Create the inventory row for the new user
            try (PreparedStatement createStmt = connection.prepareStatement(createInventorySQL)) {
                createStmt.setLong(1, userId);
                createStmt.executeUpdate();
            }

            // Now get the ID and proceed
            try (PreparedStatement getInvStmt = connection.prepareStatement(getInventoryId)) {
                getInvStmt.setLong(1, userId);
                ResultSet rsInv = getInvStmt.executeQuery();

                if (!rsInv.next()) {
                    System.err.println("Inventory creation failed for user: " + userId);
                    return;
                }
                long inventoryId = rsInv.getLong("id");

                try (PreparedStatement firstItem = connection.prepareStatement(firstCosmeticSQL);
                        PreparedStatement owned = connection.prepareStatement(insertOwnedSQL);
                        PreparedStatement equip = connection.prepareStatement(equipSQL)) {

                    for (CosmeticType type : CosmeticType.values()) {
                        firstItem.setString(1, type.name());
                        ResultSet rsItem = firstItem.executeQuery();

                        if (rsItem.next()) {
                            long defaultCosmeticId = rsItem.getLong("id");

                            // Own it
                            owned.setLong(1, inventoryId);
                            owned.setLong(2, defaultCosmeticId);
                            owned.executeUpdate();

                            // Equip it
                            equip.setLong(1, inventoryId);
                            equip.setString(2, type.name());
                            equip.setLong(3, defaultCosmeticId);
                            equip.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to initialize inventory for new user: " + e.getMessage());
        }
    }

    public void insertAssets() {
        // Each entry: { jsonClasspathResource, typeOverride (null = read from JSON) }
        // typeOverride is used when the JSON "type" field differs from CosmeticType
        // names.
        String[][] assetFiles = {
                { "StudyMore/assets/backgrounds/backgrounds.json", null },
                { "StudyMore/assets/banners/banners.json", null },
                { "StudyMore/assets/cathouses/cathouses.json", "MASCOT_HOUSE" },
                { "StudyMore/assets/cats/cats.json", "MASCOT_SKIN" },
                { "StudyMore/assets/medals/medals.json", null },
                { "StudyMore/assets/titles.json", null }
        };

        String upsertSQL = """
                INSERT INTO cosmetics (id, name, type, price, image_path, description)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT(id) DO NOTHING
                """;

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement stmt = connection.prepareStatement(upsertSQL)) {

                for (String[] entry : assetFiles) {
                    String resourcePath = entry[0];
                    String typeOverride = entry[1]; // may be null

                    JSONArray items = loadJsonArray(resourcePath);
                    if (items == null) {
                        System.err.println("Asset file not found or unreadable: " + resourcePath);
                        continue;
                    }

                    for (int i = 0; i < items.length(); i++) {
                        JSONObject item = items.getJSONObject(i);

                        long id = item.getLong("id");
                        String name = item.getString("name");
                        String description = item.optString("description", "");
                        int price = item.optInt("price", 0);
                        String path = item.optString("path", "");

                        // Resolve the CosmeticType: prefer the override, then the JSON field.
                        String rawType = (typeOverride != null)
                                ? typeOverride
                                : item.optString("type", "").toUpperCase();

                        CosmeticType cosmeticType = TYPE_MAP.get(rawType);
                        if (cosmeticType == null) {
                            System.err.println(
                                    "Unknown cosmetic type '" + rawType + "' for item: " + name + " — skipping.");
                            continue;
                        }

                        // Build the full image path: path + name + ".png"
                        String imagePath = path + name + ".png";

                        stmt.setLong(1, id);
                        stmt.setString(2, name);
                        stmt.setString(3, cosmeticType.name());
                        stmt.setInt(4, price);
                        stmt.setString(5, imagePath);
                        stmt.setString(6, description);
                        stmt.addBatch();
                    }
                }

                stmt.executeBatch();
            }

            connection.commit();
            System.out.println("Assets registered successfully.");

        } catch (Exception e) {
            System.err.println("insertAssets failed: " + e.getMessage());
            try {
                connection.rollback();
            } catch (SQLException ignored) {
            }
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
    }

    public void insertAchievements(long userId) {
        JSONArray achievements = loadJsonArray("StudyMore/assets/achievements.json");

        if (achievements == null) {
            System.err.println("Failed to load achievements.json");
            return;
        }

        String upsertAchievementSQL = """
                INSERT INTO achievements (id, title, description, type, target_value, reward)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT(id) DO NOTHING
                """;

        String insertUserAchievementSQL = """
                INSERT INTO user_achievements (id, user_id, achievement_id, progress, is_completed)
                VALUES (?, ?, ?, 0, 0)
                ON CONFLICT(user_id, achievement_id) DO NOTHING
                """;

        try {
            // Disable auto commit to handle this as a single atomic transaction
            connection.setAutoCommit(false);

            try (PreparedStatement achStmt = connection.prepareStatement(upsertAchievementSQL);
                    PreparedStatement userAchStmt = connection.prepareStatement(insertUserAchievementSQL)) {

                for (int i = 0; i < achievements.length(); i++) {
                    JSONObject obj = achievements.getJSONObject(i);

                    long id = obj.getLong("id");
                    String title = obj.getString("title");
                    String description = obj.getString("description");
                    String type = obj.getString("type");
                    int targetValue = obj.getInt("target_value");
                    int reward = obj.getInt("reward");

                    // Prepare the master achievement definition batch
                    achStmt.setLong(1, id);
                    achStmt.setString(2, title);
                    achStmt.setString(3, description);
                    achStmt.setString(4, type);
                    achStmt.setInt(5, targetValue);
                    achStmt.setInt(6, reward);
                    achStmt.addBatch();

                    // Prepare the user-specific progress batch
                    userAchStmt.setLong(1, SnowflakeIDGenerator.generate());
                    userAchStmt.setLong(2, userId);
                    userAchStmt.setLong(3, id);
                    userAchStmt.addBatch();
                }

                // Execute both batches
                achStmt.executeBatch();
                userAchStmt.executeBatch();
            }

            connection.commit();
            System.out.println("Achievements and user progress records initialized successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Cosmetic> getAllCosmetics() {
        List<Cosmetic> allItems = new ArrayList<>();
        String query = "SELECT id, name, type, price, image_path, description FROM cosmetics";

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Cosmetic cosmetic = new Cosmetic(
                        rs.getLong("id"),
                        rs.getString("name"),
                        CosmeticType.valueOf(rs.getString("type")),
                        rs.getInt("price"),
                        rs.getString("image_path"),
                        rs.getString("description"));
                allItems.add(cosmetic);
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch all cosmetics: " + e.getMessage());
        }
        return allItems;
    }

    public void updateUserCoinBalance(long userId, int newBalance) {
        // Add a new row if empty or else update the already made row.
        String query = """
                INSERT INTO user_stats (user_id, coin_balance)
                VALUES (?, ?)
                ON CONFLICT(user_id) DO UPDATE SET coin_balance = excluded.coin_balance
                """;

        try (java.sql.PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, newBalance);
            stmt.setLong(2, userId);
            stmt.executeUpdate();
        } catch (java.sql.SQLException e) {
            System.err.println("Failed to save new coin balance: " + e.getMessage());
        }
    }

    public void equipCosmetic(long userId, Cosmetic item) {
        String getInventoryId = "SELECT id FROM inventory WHERE user_id = ?";
        String upsertEquipped = """
                INSERT INTO inventory_equipped_items (inventory_id, cosmetic_type, cosmetic_id)
                VALUES (?, ?, ?)
                ON CONFLICT(inventory_id, cosmetic_type) DO UPDATE SET cosmetic_id = excluded.cosmetic_id
                """;

        try (PreparedStatement invStmt = connection.prepareStatement(getInventoryId)) {
            invStmt.setLong(1, userId);
            try (ResultSet rs = invStmt.executeQuery()) {
                if (rs.next()) {
                    long inventoryId = rs.getLong("id");

                    try (PreparedStatement equipStmt = connection.prepareStatement(upsertEquipped)) {
                        equipStmt.setLong(1, inventoryId);
                        equipStmt.setString(2, item.getType().name());
                        equipStmt.setLong(3, item.getId());
                        equipStmt.executeUpdate();
                        System.out.println("Database successfully saved equipped " + item.getType().name() + "!");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to save equipped item: " + e.getMessage());
        }
    }

    public void saveSettings(long userId, Settings settings) {
        String sql = """
                INSERT INTO settings (
                    user_id, dark_mode, lock_in_mode, show_mascot,
                    study_time, short_break, long_break, long_break_after,
                    start_sound, break_alert, popups
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(user_id) DO UPDATE SET
                    dark_mode = excluded.dark_mode,
                    lock_in_mode = excluded.lock_in_mode,
                    show_mascot = excluded.show_mascot,
                    study_time = excluded.study_time,
                    short_break = excluded.short_break,
                    long_break = excluded.long_break,
                    long_break_after = excluded.long_break_after,
                    start_sound = excluded.start_sound,
                    break_alert = excluded.break_alert,
                    popups = excluded.popups
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setInt(2, settings.isDarkMode() ? 1 : 0);
            stmt.setInt(3, settings.isLockInMode() ? 1 : 0);
            stmt.setInt(4, settings.isMascotVisible() ? 1 : 0);
            stmt.setInt(5, settings.getStudyTime());
            stmt.setInt(6, settings.getShortBreak());
            stmt.setInt(7, settings.getLongBreak());
            stmt.setInt(8, settings.getLongBreakAfter());
            stmt.setInt(9, settings.isStartSound() ? 1 : 0);
            stmt.setInt(10, settings.isBreakAlert() ? 1 : 0);
            stmt.setInt(11, settings.isPopups() ? 1 : 0);
            stmt.executeUpdate();
            System.out.println("Settings saved for user " + userId);
        } catch (SQLException e) {
            System.err.println("Failed to save settings: " + e.getMessage());
        }
    }

    public Settings getSettings(long userId) {
        String sql = """
                SELECT dark_mode, lock_in_mode, show_mascot,
                    study_time, short_break, long_break, long_break_after,
                    start_sound, break_alert, popups
                FROM settings WHERE user_id = ?
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Settings settings = new Settings();
                    settings.setDarkMode(rs.getInt("dark_mode") == 1);
                    settings.setLockInMode(rs.getInt("lock_in_mode") == 1);
                    settings.setMascotVisible(rs.getInt("show_mascot") == 1);
                    settings.setStudyTime(rs.getInt("study_time"));
                    settings.setShortBreak(rs.getInt("short_break"));
                    settings.setLongBreak(rs.getInt("long_break"));
                    settings.setLongBreakAfter(rs.getInt("long_break_after"));
                    settings.setStartSound(rs.getInt("start_sound") == 1);
                    settings.setBreakAlert(rs.getInt("break_alert") == 1);
                    settings.setPopups(rs.getInt("popups") == 1);
                    return settings;
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to load settings: " + e.getMessage());
        }

        return new Settings();
    }

    public void addTask(long userId, Task task) {
        String sql = """
            INSERT INTO tasks (id, user_id, title, content, created_at, srs_enabled, 
                            repetition_count, ease_factor, current_interval, 
                            review_intensity, is_complete, next_recall_date) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) 
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, task.getID());
            stmt.setLong(2, userId);
            stmt.setString(3, task.getTitle());
            stmt.setString(4, task.getContent());
            stmt.setString(5, task.getCreatedAtAsString());
            stmt.setInt(6, task.isSrsEnabled() ? 1 : 0);
            stmt.setString(12, task.getNextRecallDateAsString());

            if (task.isSrsEnabled() && task.getSrsData() != null) {
                SRSMetadata data = task.getSrsData();
                stmt.setInt(7, data.getRepetitionCount());
                stmt.setDouble(8, data.getCurrentEaseFactor());
                stmt.setInt(9, data.getCurrentInterval());
                stmt.setString(10, data.getIntensity().name());
            } else {
                // Setting up defaults to bypass NullPointerException later
                stmt.setInt(7, 0);
                stmt.setDouble(8, 2.5);
                stmt.setInt(9, 0);
                stmt.setString(10, "STANDARD");
            }

            stmt.setInt(11, task.isCompleted() ? 1 : 0);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateTask(Task task) {
        String sql = """
                    UPDATE tasks SET
                        title = ?,
                        content = ?,
                        is_complete = ?,
                        next_recall_date = ?
                    WHERE id = ?
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, task.getTitle());
            stmt.setString(2, task.getContent());
            stmt.setInt(3, task.isCompleted() ? 1 : 0);
            stmt.setString(4, task.getNextRecallDateAsString());
            stmt.setLong(5, task.getID());

            stmt.executeUpdate();
            System.out.println("Task updated in database!");
        } catch (SQLException e) {
            System.err.println("Failed to update task: " + e.getMessage());
        }
    }

    public void deleteTask(Task task) {
        String deleteHistorySql = "DELETE FROM task_srs_history WHERE task_id = ?";
        String deleteTaskSql = "DELETE FROM tasks WHERE id = ?";

        try (PreparedStatement historyStmt = connection.prepareStatement(deleteHistorySql);
                PreparedStatement taskStmt = connection.prepareStatement(deleteTaskSql)) {

            historyStmt.setLong(1, task.getID());
            historyStmt.executeUpdate();

            taskStmt.setLong(1, task.getID());
            taskStmt.executeUpdate();

            System.out.println("Task and its history deleted from the database!");
        } catch (SQLException e) {
            System.err.println("Failed to delete task: " + e.getMessage());
        }
    }

    public JSONObject loadSyncPayload(long userId) {
        JSONObject payload = new JSONObject();

        try {
            // Base User Data (1-to-1 relationships)
            payload.put("user", fetchSingleRow("SELECT * FROM users WHERE id = ?", userId));
            payload.put("user_stats", fetchSingleRow("SELECT * FROM user_stats WHERE user_id = ?", userId));
            payload.put("settings", fetchSingleRow("SELECT * FROM settings WHERE user_id = ?", userId));
            payload.put("multipliers", fetchSingleRow("SELECT * FROM multipliers WHERE user_id = ?", userId));
            payload.put("inventory", fetchSingleRow("SELECT * FROM inventory WHERE user_id = ?", userId));

            // User Data Collections (1-to-Many relationships)
            payload.put("sessions", fetchArray("SELECT * FROM sessions WHERE user_id = ?", userId));
            payload.put("tasks", fetchArray("SELECT * FROM tasks WHERE user_id = ?", userId));
            payload.put("user_achievements", fetchArray("SELECT * FROM user_achievements WHERE user_id = ?", userId));
            
            // Nested Relationships (Joining tables to verify ownership)
            payload.put("task_srs_history", fetchArray(
                "SELECT tsh.* FROM task_srs_history tsh JOIN tasks t ON tsh.task_id = t.id WHERE t.user_id = ?", 
                userId));
                
            payload.put("inventory_owned_items", fetchArray(
                "SELECT ioi.* FROM inventory_owned_items ioi JOIN inventory i ON ioi.inventory_id = i.id WHERE i.user_id = ?", 
                userId));
                
            payload.put("inventory_equipped_items", fetchArray(
                "SELECT iei.* FROM inventory_equipped_items iei JOIN inventory i ON iei.inventory_id = i.id WHERE i.user_id = ?", 
                userId));

            // Social & Study Groups
            payload.put("friends", fetchArray(
                "SELECT * FROM friends WHERE user_id = ? OR friend_id = ?", 
                userId, userId));
                
            payload.put("friend_requests", fetchArray(
                "SELECT * FROM friend_requests WHERE sender_id = ? OR receiver_id = ?", 
                userId, userId));

            // Pull study groups where the user is either the host OR a member
            payload.put("study_groups", fetchArray(
                "SELECT DISTINCT sg.* FROM study_groups sg LEFT JOIN study_group_members sgm ON sg.id = sgm.group_id WHERE sg.host_id = ? OR sgm.user_id = ?", 
                userId, userId));

        } catch (SQLException e) {
            System.err.println("Error building sync payload: " + e.getMessage());
        }

        return payload;
    }


    private JSONObject fetchSingleRow(String query, Object... params) throws SQLException {
        JSONArray array = fetchArray(query, params);
        // Return the first object if it exists, otherwise return an empty object
        return array.length() > 0 ? array.getJSONObject(0) : new JSONObject();
    }

    private JSONArray fetchArray(String query, Object... params) throws SQLException {
        JSONArray jsonArray = new JSONArray();
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                while (rs.next()) {
                    JSONObject jsonObject = new JSONObject();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        Object columnValue = rs.getObject(i);
                        
                        // Handle nulls
                        if (columnValue == null) {
                            jsonObject.put(columnName, JSONObject.NULL);
                        } else {
                            jsonObject.put(columnName, columnValue);
                        }
                    }
                    jsonArray.put(jsonObject);
                }
            }
        }
        return jsonArray;
    }

    public static void debugDatabase() {
        System.out.println("DEBUG");
        // TODO
    }
    public void insertTaskHistory(long taskId, SRSHistoryEntry entry) {
        String sql = """
            INSERT INTO task_srs_history (task_id, ease_factor_at_time, interval_at_time, quality_score, timestamp)
            VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, taskId);
            stmt.setDouble(2, entry.getEaseFactorAtTime());
            stmt.setInt(3, entry.getIntervalAtTime());
            stmt.setInt(4, entry.getQualityScore());

            stmt.executeUpdate();
            System.out.println("SRS History logged successfully!");
        } catch (SQLException e) {
            System.err.println("Failed to log SRS history: " + e.getMessage());
        }
    }



}