package StudyMore;

import StudyMore.db.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.sql.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import StudyMore.models.Settings;
import StudyMore.models.User;

public class Main extends Application {
    public static DatabaseManager mngr;
    public static Stage primarStageStatic;
    public static User user;
    public static Settings settings; 
    private static ScheduledExecutorService syncScheduler;

    @Override
    public void start(Stage primaryStage) throws Exception {
        primarStageStatic = primaryStage; 
        long check = isUserLoggedIn();

        if(check != -1) {
            user = mngr.getUser(check);
            settings = mngr.getSettings(user.getUserId()); 

            if (settings == null) {
                settings = new Settings();
                mngr.saveSettings(user.getUserId(), settings);
            }
            if (user != null) {
                startSyncLoop();
                Parent root = FXMLLoader.load(getClass().getResource("fxml/Index.fxml"));
                primaryStage.setTitle("StudyMore");
                primaryStage.setScene(new Scene(root, 1200, 800));
                primaryStage.show();
            } else {
                System.err.println("Local DB corrupted or incomplete. Forcing re-login.");
                mngr.wipeAndRebuildDatabase(); // Use the wipe method 
                loadLoginScreen(primaryStage);
            }
        } else {
            loadLoginScreen(primaryStage);
        }
    }

    @Override
    public void stop() throws Exception {
        stopSyncLoop();
        
        if(mngr != null) {
            mngr.closeConnection();
        }

        super.stop();
    }

    // Helper method 
    private void loadLoginScreen(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("fxml/loginRegister.fxml"));
        stage.setTitle("StudyMore - Login");
        stage.setScene(new Scene(root, 1200, 800));
        stage.show();
    }


    public static void main(String[] args) {
        mngr = new DatabaseManager();

        launch(args);
    }

    public static long isUserLoggedIn() {
        String query = "SELECT * FROM users";

        try (Statement stmt = mngr.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(query)) {

            int count = 0;
            long userId = -1;

            while (rs.next()) {
                count++;
                userId = rs.getLong("id");
            }

            if (count != 1) {
                return -1;
            }

            return userId;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    public static void startSyncLoop() {
        // Prevent multiple loops from starting if called twice
        if (syncScheduler != null && !syncScheduler.isShutdown()) {
            return; 
        }

        syncScheduler = Executors.newSingleThreadScheduledExecutor();
        syncScheduler.scheduleAtFixedRate(() -> {
            if (user == null) return; // Failsafe

            // Heartbeat
            try {
                String heartbeatBody = "{\"userId\":" + user.getUserId() + "}";
                ApiClient.postAuth("/auth/users/heartbeat", heartbeatBody);
            } catch (Exception e) {
                System.out.println("Heartbeat failed: " + e.getMessage());
            }

            // Sync
            org.json.JSONObject payload = Main.mngr.loadSyncPayload(user.getUserId());
            try {
                org.json.JSONObject response = ApiClient.sync(user.getUserId(), payload); 
                System.out.println("Auto-sync successful.");
            } catch (Exception e) {
                System.out.println("ERROR SYNC");
            }
        }, 0, 5, TimeUnit.MINUTES); 
    }

    public static void stopSyncLoop() {
        if (syncScheduler != null && !syncScheduler.isShutdown()) {
            syncScheduler.shutdownNow();
            syncScheduler = null;
        }
    }
}
    