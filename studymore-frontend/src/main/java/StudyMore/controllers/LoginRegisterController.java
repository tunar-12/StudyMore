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

        if (username.isEmpty() || password.isEmpty()) return;
        String loginBody = "{\"username\":\"" + username + "\","
                        + "\"passwordHash\":\"" + sha256(password) + "\"}";
        String loginResponse = ApiClient.postAuth("/auth/login", loginBody);

        long userId = -1;
        String email = "";
        try {
            org.json.JSONObject res = new org.json.JSONObject(loginResponse);
            if (!res.has("userId")) {
                loginPasswordField.setStyle("-fx-border-color: red; -fx-background-color: #000; -fx-text-fill: white; -fx-padding: 10px;");
                return;
            }
            userId = res.getLong("userId");
            email  = res.optString("email", "");
        } catch (Exception e) {
            loginPasswordField.setStyle("-fx-border-color: red; -fx-background-color: #000; -fx-text-fill: white; -fx-padding: 10px;");
            return;
        }
        final long finalUserId = userId;
        final String finalEmail = email;
        try {
            try (java.sql.PreparedStatement check = Main.mngr.getConnection().prepareStatement(
                    "SELECT id FROM users WHERE id = ?")) {
                check.setLong(1, finalUserId);
                try (java.sql.ResultSet rs = check.executeQuery()) {
                    if (!rs.next()) {
                        try (java.sql.PreparedStatement ins = Main.mngr.getConnection().prepareStatement(
                                "INSERT INTO users(id, username, email, password_hash) VALUES(?,?,?,?)")) {
                            ins.setLong(1, finalUserId);
                            ins.setString(2, username);
                            ins.setString(3, finalEmail);
                            ins.setString(4, sha256(password));
                            ins.executeUpdate();
                        }
                        Main.mngr.initializeNewUserInventory(finalUserId);
                        Main.mngr.insertAchievements(finalUserId);
                    }
                }
            }

            Main.user = Main.mngr.getUser(finalUserId);

            ApiClient.postAuth("/auth/users/heartbeat",
                    "{\"userId\":" + finalUserId + "}");
            Main.startSyncLoop();

            Parent root = FXMLLoader.load(getClass().getResource("../fxml/Index.fxml"));
            Main.primarStageStatic.setTitle("StudyMore");
            Main.primarStageStatic.setScene(new Scene(root, 1200, 800));
            Main.primarStageStatic.show();

        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String username = registerUsernameField.getText().trim();
        String email    = registerEmailField.getText().trim();
        String password = registerPasswordField.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showRegisterError("Please fill in all fields.");
            return;
        }

        long   generatedId   = SnowflakeIDGenerator.generate();
        String passwordHash  = sha256(password);
        String syncBody = "{\"userId\":"        + generatedId   + ","
                        + "\"username\":\""     + username      + "\","
                        + "\"email\":\""        + email         + "\","
                        + "\"passwordHash\":\"" + passwordHash  + "\"}";

        try {
            String syncResponse = ApiClient.postAuth("/auth/users/sync", syncBody);
            org.json.JSONObject res = new org.json.JSONObject(syncResponse);

            if (res.has("error")) {
                showRegisterError("Registration failed: " + res.getString("error"));
                return;
            }
            String sql = "INSERT INTO users(id, username, email, password_hash) VALUES (?, ?, ?, ?)";
            try (java.sql.PreparedStatement pstmt = Main.mngr.getConnection().prepareStatement(sql)) {
                pstmt.setLong(1, generatedId);
                pstmt.setString(2, username);
                pstmt.setString(3, email);
                pstmt.setString(4, passwordHash);
                pstmt.executeUpdate();
            }

            Main.mngr.initializeNewUserInventory(generatedId);
            Main.mngr.insertAchievements(generatedId);
            Main.user = Main.mngr.getUser(generatedId);
            ApiClient.postAuth("/auth/users/heartbeat",
                    "{\"userId\":" + generatedId + "}");
            Main.startSyncLoop();

            navigateToMain();

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