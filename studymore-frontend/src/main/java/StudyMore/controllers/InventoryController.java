package StudyMore.controllers;

import java.util.ArrayList;
import java.util.List;

import StudyMore.Main;
import StudyMore.models.Cosmetic;
import StudyMore.models.CosmeticType;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class InventoryController {

    @FXML private HBox bannersContainer;
    @FXML private HBox titlesContainer;
    @FXML private HBox medalsContainer;
    @FXML private HBox backgroundsContainer; 
    @FXML private HBox mascotSkinsContainer;
    @FXML private HBox mascotHousesContainer;

    @FXML
    public void initialize() {
        loadInventoryUI();
    }

    private void loadInventoryUI() {

        //Clear containers
        bannersContainer.getChildren().clear();
        titlesContainer.getChildren().clear();
        mascotSkinsContainer.getChildren().clear();
        mascotHousesContainer.getChildren().clear();
        medalsContainer.getChildren().clear();
        backgroundsContainer.getChildren().clear(); 
        
        List<Cosmetic> myItems = getOwnedCosmeticsFromDatabase(); 

        for (Cosmetic item : myItems) { 
            Node itemNode = createItemBox(item);
            Cosmetic equippedItem = Main.user.getInventory().getEquipped(item.getType()); //Get what is currently equipped for this category

            // Check if the current item is the one equipped
            boolean isEquipped = false;
            if (equippedItem != null && item.getName().equals(equippedItem.getName())) {
                isEquipped = true;
            }

            // Put it in the correct box and if it is the equipped item add to front of list
            if (item.getType() == CosmeticType.BANNER) {
                if (isEquipped) {
                    bannersContainer.getChildren().add(0, itemNode);
                }else{
                    bannersContainer.getChildren().add(itemNode);
                }  
            } else if (item.getType() == CosmeticType.TITLE) {
                if (isEquipped){
                    titlesContainer.getChildren().add(0, itemNode);
                }else{
                    titlesContainer.getChildren().add(itemNode);
                }
            } else if (item.getType() == CosmeticType.MASCOT_SKIN) {
                if (isEquipped) {
                    mascotSkinsContainer.getChildren().add(0, itemNode);
                }else {
                    mascotSkinsContainer.getChildren().add(itemNode);
                }  
            } else if (item.getType() == CosmeticType.MASCOT_HOUSE) {
                if (isEquipped) {
                    mascotHousesContainer.getChildren().add(0, itemNode);
                }else {
                    mascotHousesContainer.getChildren().add(itemNode);
                }
            } else if (item.getType() == CosmeticType.MEDAL) { 
                if (isEquipped) {
                    medalsContainer.getChildren().add(0, itemNode);
                }else{
                    medalsContainer.getChildren().add(itemNode);
                }
            } else if (item.getType() == CosmeticType.BACKGROUND) { 
                if (isEquipped) {
                    backgroundsContainer.getChildren().add(0, itemNode);
                }else {
                    backgroundsContainer.getChildren().add(itemNode);
                }    
            }
        }
    }

    //create relevant box type
    private Node createItemBox(Cosmetic item) { 
        if (item.getType() == CosmeticType.BANNER) {
            return createBannerStyleCard(item); 
        } else if (item.getType() == CosmeticType.BACKGROUND || item.getType() == CosmeticType.MASCOT_SKIN || item.getType() == CosmeticType.MASCOT_HOUSE || item.getType() == CosmeticType.MEDAL) {
            return createStandardStyleCard(item); 
        } else if (item.getType() == CosmeticType.TITLE) {
            return createTitleStyleCard(item);
        } 
        return new VBox();
    }


    private VBox createBannerStyleCard(Cosmetic item) { 
        VBox card = new VBox(16);
        card.setPadding(new Insets(16));
        card.setPrefWidth(420); // Wider
        card.setStyle("-fx-border-color: #262626; -fx-background-color: #0a0a0a; -fx-border-width: 1; -fx-background-radius: 4; -fx-border-radius: 4;");

        javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView();
        imageView.setFitHeight(100);
        imageView.setPreserveRatio(true);
        
        try {
            String imageResourcePath = "/StudyMore/" + item.getImagePath();
            java.io.InputStream imageStream = getClass().getResourceAsStream(imageResourcePath);
            if (imageStream != null) {
                imageView.setImage(new javafx.scene.image.Image(imageStream));
                imageStream.close();
            }
        } catch (Exception e) {
            System.out.println("Error loading image for " + item.getName());
        }
        
        StackPane imagePlaceholder = new StackPane(imageView); 
        imagePlaceholder.setPrefHeight(120);
        imagePlaceholder.setStyle("-fx-background-color: #111111; -fx-border-color: #262626; -fx-border-style: dashed;");

        Label nameLabel = new Label(item.getName().toUpperCase());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        HBox bottomRow = new HBox(); 
        bottomRow.setAlignment(Pos.CENTER);

        Label ownedLabel = new Label("OWNED");
        ownedLabel.setStyle("-fx-text-fill: #a3a3a3; -fx-font-weight: bold; -fx-font-size: 10px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button equipBtn = createEquipButton(item); 

        bottomRow.getChildren().addAll(ownedLabel, spacer, equipBtn);
        card.getChildren().addAll(imagePlaceholder, nameLabel, bottomRow);

        return card;
    }


    private VBox createStandardStyleCard(Cosmetic item) { 
        VBox card = new VBox(16);
        card.setPadding(new Insets(16));
        card.setPrefWidth(280);
        card.setStyle("-fx-border-color: #262626; -fx-background-color: #0a0a0a; -fx-border-width: 1; -fx-background-radius: 4; -fx-border-radius: 4;");

        javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView();
        imageView.setFitHeight(100);
        imageView.setPreserveRatio(true);
        
        try {
            String imageResourcePath = "/StudyMore/" + item.getImagePath();
            java.io.InputStream imageStream = getClass().getResourceAsStream(imageResourcePath);
            if (imageStream != null) {
                imageView.setImage(new javafx.scene.image.Image(imageStream));
                imageStream.close();
            }
        } catch (Exception e) {
            System.out.println("Error loading image for " + item.getName());
        }

        StackPane imagePlaceholder = new StackPane(imageView); 
        imagePlaceholder.setPrefHeight(120);
        imagePlaceholder.setStyle("-fx-background-color: #111111; -fx-border-color: #262626; -fx-border-style: dashed;");

        Label nameLabel = new Label(item.getName().toUpperCase());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        HBox bottomRow = new HBox(); 
        bottomRow.setAlignment(Pos.CENTER);

        Label ownedLabel = new Label("OWNED");
        ownedLabel.setStyle("-fx-text-fill: #a3a3a3; -fx-font-weight: bold; -fx-font-size: 10px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button equipBtn = createEquipButton(item); 

        bottomRow.getChildren().addAll(ownedLabel, spacer, equipBtn);
        card.getChildren().addAll(imagePlaceholder, nameLabel, bottomRow);

        return card;
    }


    private HBox createTitleStyleCard(Cosmetic item) { 
        HBox card = new HBox(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(12, 16, 12, 16));
        card.setPrefWidth(350);
        card.setStyle("-fx-border-color: #262626; -fx-background-color: #0a0a0a; -fx-border-width: 1; -fx-background-radius: 4; -fx-border-radius: 4;");

        Label nameLabel = new Label(item.getName().toUpperCase());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button equipBtn = createEquipButton(item); 

        card.getChildren().addAll(nameLabel, spacer, equipBtn);
        return card;
    }

    private Button createEquipButton(Cosmetic item) {
        Button btn = new Button("EQUIP");
        
        // Ask the inventory if this specific item is the one currently equipped in its category
        Cosmetic equippedItem = Main.user.getInventory().getEquipped(item.getType());
        
        // If it is already equipped grey the button and disable clicking
        if (equippedItem != null && item.getName().equals(equippedItem.getName())) {
            btn.setText("EQUIPPED");
            btn.setStyle("-fx-background-color: #262626; -fx-border-color: #262626; -fx-text-fill: #a3a3a3; -fx-font-weight: bold; -fx-padding: 5 15 5 15;");
            btn.setDisable(true);
        } else {
            // make it look clickable
            btn.setCursor(javafx.scene.Cursor.HAND);
            btn.setStyle("-fx-background-color: transparent; -fx-border-color: #262626; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 15 5 15;");
        }

        btn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                // Update local memory and Database
                Main.user.getInventory().equipItem(item);
                Main.mngr.equipCosmetic(Main.user.getUserId(), item);
                
                // refresh UI to update the button colors and move the equipped item to the front
                loadInventoryUI(); 
                
                // refresh background
                if (item.getType() == CosmeticType.BACKGROUND) {
                    Controller.instance.refreshBackground();
                }
            }
        }); 
        return btn;
    }

    private List<Cosmetic> getOwnedCosmeticsFromDatabase() {
        return Main.user.getInventory().getOwnedItems();
    }
}