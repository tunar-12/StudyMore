package StudyMore.controllers;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import StudyMore.ApiClient;
import StudyMore.Main;
import StudyMore.models.SnowflakeIDGenerator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginRegisterController {
    
    @FXML
    private TextField loginUsernameField;

    @FXML
    private PasswordField loginPasswordField;
    
    @FXML
    private TextField registerUsernameField;

    @FXML
    private TextField registerEmailField;

    @FXML
    private PasswordField registerPasswordField;

    @FXML
    public void initialize() {

    }

    @FXML
    private void handleLogin(ActionEvent event) {
        //Will send a request to backend to verify the user.
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String username = registerUsernameField.getText();
        String email = registerEmailField.getText();
        String password = registerPasswordField.getText();

        final String sql = """
            INSERT INTO users(id, username, email, password_hash)
            VALUES (?, ?, ?, ?)
            """;

        try (PreparedStatement pstmt = Main.mngr.getConnection().prepareStatement(sql)) {
            long id = SnowflakeIDGenerator.generate();
            pstmt.setLong(1, id);          
            pstmt.setString(2, username);                               
            pstmt.setString(3, email);                                  
            pstmt.setString(4, sha256(password));                       

            int rows = pstmt.executeUpdate();
            if (rows != 1) {
                throw new IllegalStateException("Insert failed, no rows affected");
            }

            System.out.println("INSERTED USER");

            Main.mngr.initializeNewUserInventory(id);
            Main.mngr.insertAchievements(id);

            Main.user = Main.mngr.getUser(id);

            String syncBody = "{\"userId\":"        + id                  + ","
                            + "\"username\":\""     + username            + "\","
                            + "\"email\":\""        + email               + "\","
                            + "\"passwordHash\":\"" + sha256(password)    + "\"}";
            ApiClient.postAuth("/auth/users/sync", syncBody);


            Parent root = FXMLLoader.load(getClass().getResource("../fxml/Index.fxml"));
            Main.primarStageStatic.setTitle("StudyMore");
            Main.primarStageStatic.setScene(new Scene(root, 1200, 800));
            Main.primarStageStatic.show();

        } catch (Exception e) {e.printStackTrace();}
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