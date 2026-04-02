package StudyMore.db;

import java.sql.*;
import java.util.ArrayList;

import StudyMore.models.*;


public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:studymore_database.db";
    private Connection connection;

    public DatabaseManager() {
        initilizeDB();
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
                srs_enabled INTEGER NOT NULL DEFAULT 0,
                next_recall_date TEXT,
                is_complete INTEGER NOT NULL DEFAULT 0,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id)
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
            SELECT id, title, content, srs_enabled, is_complete, next_recall_date, created_at
            FROM tasks WHERE user_id = ?
            """;

        try (PreparedStatement userStmt = connection.prepareStatement(userQuery)) {
            userStmt.setLong(1, id);

            try (ResultSet rs = userStmt.executeQuery()) {
                if (!rs.next()) return null; // user not found

                long userId = rs.getLong("id");

                // Build Inventory 
                Inventory inventory = new Inventory((long) id, null);
                try (PreparedStatement invStmt = connection.prepareStatement(inventoryQuery)) {
                    invStmt.setLong(1, id);
                    try (ResultSet invRs = invStmt.executeQuery()) {
                        while (invRs.next()) {
                            long cosmeticId = invRs.getLong("cosmetic_id");
                            if (cosmeticId == 0) continue; 

                            Cosmetic cosmetic = new Cosmetic(
                                cosmeticId,
                                invRs.getString("name"),
                                CosmeticType.valueOf(invRs.getString("type")),
                                invRs.getInt("price"),
                                invRs.getString("image_path"),
                                invRs.getString("description")
                            );
                            inventory.addItem(cosmetic);

                            String equippedType = invRs.getString("equipped_type");
                            if (equippedType != null) {
                                inventory.equipItem(cosmetic);
                            }
                        }
                    }
                }

                MascotCat mascotCat = new MascotCat(userId);
                Cosmetic skin  = inventory.getEquipped(CosmeticType.MASCOT_SKIN);  
                Cosmetic hat   = inventory.getEquipped(CosmeticType.MASCOT_HAT);    
                Cosmetic house = inventory.getEquipped(CosmeticType.MASCOT_HOUSE); 
                if (skin  != null) mascotCat.changeSkin(skin);
                if (hat   != null) mascotCat.changeMascotHat(hat);
                if (house != null) mascotCat.changeMascotHouse(house);

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
                            Task task = new Task(
                                tasksRs.getString("title"),
                                tasksRs.getString("content"),
                                tasksRs.getInt("srs_enabled") == 1,
                                null // ReviewIntensity unknown at load time. handle in Task constructor
                            );
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
                    rs.getInt("rating"),       // returns 0 if null
                    rs.getInt("coin_balance"), // returns 0 if null
                    rs.getInt("study_streak"), // returns 0 if null
                    rs.getLong("total_study_time"),
                    rs.getLong("daily_study_time"),
                    mascotCat,
                    inventory,
                    friends,
                    tasks
                );

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
                if (!rs.next()) return null;

                Multiplier multiplier = new Multiplier(rs.getDouble("multiplier_value"));

                StudySession session = new StudySession(user, 
                    rs.getLong("id"),
                    rs.getTimestamp("start_time").toLocalDateTime(),
                    rs.getTimestamp("end_time") != null 
                        ? rs.getTimestamp("end_time").toLocalDateTime() 
                        : null,
                    multiplier,
                    rs.getInt("duration"),
                    rs.getInt("coins_earned")
                );

                return session;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
