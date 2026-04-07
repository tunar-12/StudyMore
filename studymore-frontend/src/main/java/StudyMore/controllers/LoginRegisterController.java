package StudyMore.controllers;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import StudyMore.ApiClient;
import StudyMore.Main;
import StudyMore.models.SnowflakeIDGenerator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginRegisterController {

    @FXML private TextField loginUsernameField;
    @FXML private PasswordField loginPasswordField;
    @FXML private TextField registerUsernameField;
    @FXML private TextField registerEmailField;
    @FXML private PasswordField registerPasswordField;
    @FXML private Label loginErrorLabel;
    @FXML private Label registerErrorLabel;

    @FXML
    public void initialize() {
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = loginUsernameField.getText().trim();
        String password = loginPasswordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showLoginError("Please fill in all fields.");
            return;
        }

        // Authenticate with the backend first
        String loginBody = "{\"email\":\"" + username + "\", \"password\":\"" + password + "\"}";
        try {
            String loginResponse = ApiClient.postAuth("/auth/login", loginBody);

            if (loginResponse != null && loginResponse.contains("\"userId\"")) {
                org.json.JSONObject userJson = new org.json.JSONObject(loginResponse);
                long serverUserId = userJson.getLong("userId");

                // Fetch Master Payload
                String syncResponse = ApiClient.get("/api/sync/pull/" + serverUserId); 
                org.json.JSONObject masterPayload = new org.json.JSONObject(syncResponse);

                // WIPE AND REBUILD
                Main.mngr.wipeAndRebuildDatabase();
                Main.mngr.restoreFromSyncPayload(masterPayload);

                Main.user = Main.mngr.getUser(serverUserId);
                
                // Start heartbeat/sync loops
                try {
                    ApiClient.postAuth("/auth/users/heartbeat", "{\"userId\":" + serverUserId + "}");
                } catch (Exception ignored) {}

                navigateToMain();
            } else {
                // Not found on server, fallback to local check (for offline logins)
                String sql = "SELECT id FROM users WHERE email = ? AND password_hash = ?";
                try (java.sql.PreparedStatement pstmt = Main.mngr.getConnection().prepareStatement(sql)) {
                    pstmt.setString(1, username);
                    pstmt.setString(2, sha256(password));
                    try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            Main.user = Main.mngr.getUser(rs.getLong("id"));
                            navigateToMain();
                            return;
                        }
                    }
                }
                showLoginError("Invalid username or password.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showLoginError("Login failed. Check your connection.");
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String username = registerUsernameField.getText().trim();
        String email = registerEmailField.getText().trim();
        String password = registerPasswordField.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showRegisterError("Please fill in all fields.");
            return;
        }

        // Generate the Snowflake ID locally first
        long generatedId = SnowflakeIDGenerator.generate();

        // Send the registration request to the backend including the new ID
        String requestBody = "{\"userId\":\"" + generatedId + "\","
                           + "\"username\":\"" + username + "\","
                           + "\"email\":\"" + email + "\","
                           + "\"password\":\"" + password + "\"}";

        try {
            String response = ApiClient.postAuth("/auth/register", requestBody);

            // Basic check to see if the server accepted it
            if (response == null || response.contains("error") || response.contains("taken") || response.contains("use")) {
                showRegisterError("Registration failed. Username or email may already exist.");
                return;
            }

            // The backend successfully registered the user. Now save locally.
            final String sql = """
                INSERT INTO users(id, username, email, password_hash)
                VALUES (?, ?, ?, ?)
                """;

            try (java.sql.PreparedStatement pstmt = Main.mngr.getConnection().prepareStatement(sql)) {
                pstmt.setLong(1, generatedId);
                pstmt.setString(2, username);
                pstmt.setString(3, email);
                pstmt.setString(4, sha256(password)); 
                int rows = pstmt.executeUpdate();

                if (rows != 1) {
                    throw new IllegalStateException("Insert failed locally, no rows affected");
                }

                System.out.println("User successfully registered on server and saved locally.");
                
                // Initialize local states
                Main.mngr.initializeNewUserInventory(generatedId);
                Main.mngr.insertAchievements(generatedId);
                Main.user = Main.mngr.getUser(generatedId);

                try {
                    ApiClient.postAuth("/auth/users/heartbeat", "{\"userId\":" + generatedId + "}");
                } catch (Exception ignored) {}

                navigateToMain();

            } catch (Exception e) {
                e.printStackTrace();
                showRegisterError("Local database error during registration.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showRegisterError("Could not connect to the server.");
        }
    }

    private void navigateToMain() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("../fxml/Index.fxml"));
        Main.primarStageStatic.setTitle("StudyMore");
        Main.primarStageStatic.setScene(new Scene(root, 1200, 800));
        Main.primarStageStatic.show();
    }

    private void showLoginError(String message) {
        if (loginErrorLabel != null) {
            loginErrorLabel.setText(message);
            loginErrorLabel.setVisible(true);
        } else {
            System.out.println("Login error: " + message);
        }
    }

    private void showRegisterError(String message) {
        if (registerErrorLabel != null) {
            registerErrorLabel.setText(message);
            registerErrorLabel.setVisible(true);
        } else {
            System.out.println("Register error: " + message);
        }
    }

    public static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}