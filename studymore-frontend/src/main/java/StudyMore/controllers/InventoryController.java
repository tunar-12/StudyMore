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

        bannersContainer.getChildren().clear();
        titlesContainer.getChildren().clear();
        mascotSkinsContainer.getChildren().clear();
        mascotHousesContainer.getChildren().clear();
        medalsContainer.getChildren().clear();
        backgroundsContainer.getChildren().clear(); 
        List<Cosmetic> myItems = getOwnedCosmeticsFromDatabase(); 

        // Make box for each item and add to relevant container
        for (Cosmetic item : myItems) { 
            Node itemNode = createItemBox(item);

            if (item.getType() == CosmeticType.BANNER) {
                bannersContainer.getChildren().add(itemNode);
            }else if (item.getType() == CosmeticType.TITLE) {
                titlesContainer.getChildren().add(itemNode);
            } else if (item.getType() == CosmeticType.MASCOT_SKIN) {
                mascotSkinsContainer.getChildren().add(itemNode);
            } else if (item.getType() == CosmeticType.MASCOT_HOUSE) {
                mascotHousesContainer.getChildren().add(itemNode);
            } else if (item.getType() == CosmeticType.MEDAL) { 
                medalsContainer.getChildren().add(itemNode); 
            } else if (item.getType() == CosmeticType.BACKGROUND) { 
                backgroundsContainer.getChildren().add(itemNode); 
            }
        }
    }

    private Node createItemBox(Cosmetic item) { 
        if (item.getType() == CosmeticType.BANNER) {
            return createBannerStyleCard(item); 
        } else if (item.getType() == CosmeticType.BACKGROUND || item.getType() == CosmeticType.MASCOT_SKIN || item.getType() == CosmeticType.MASCOT_HOUSE) {
            return createStandardStyleCard(item); 
        } else if (item.getType() == CosmeticType.TITLE) {
            return createTitleStyleCard(item);
        } else if (item.getType() == CosmeticType.MEDAL) {
            return createMedalStyleCard(item);
        }
        return new VBox(); 
    }

    private VBox createBannerStyleCard(Cosmetic item) { 
        VBox card = new VBox(16);
        card.setPadding(new Insets(16));
        card.setPrefWidth(420); 
        card.setStyle("-fx-border-color: #262626; -fx-background-color: #0a0a0a; -fx-border-width: 1;");

        //TODO: Replace ImagePlaceHolder with actual image
        StackPane imagePlaceholder = new StackPane();
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
        card.setStyle("-fx-border-color: #262626; -fx-background-color: #0a0a0a; -fx-border-width: 1;");

        //TODO: Replace ImagePlaceHolder with actual image
        StackPane imagePlaceholder = new StackPane();
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
        card.getChildren().addAll(imagePlaceholder, nameLabel, bottomRow); // Restored nameLabel to card

        return card;
    }

    private HBox createTitleStyleCard(Cosmetic item) { 
        HBox card = new HBox(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(12, 16, 12, 16));
        card.setPrefWidth(350);
        card.setStyle("-fx-border-color: #262626; -fx-background-color: #0a0a0a; -fx-border-width: 1;");

        Label nameLabel = new Label(item.getName().toUpperCase());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button equipBtn = createEquipButton(item); 

        card.getChildren().addAll(nameLabel, spacer, equipBtn);
        return card;
    }
    
    private VBox createMedalStyleCard(Cosmetic item) {
        VBox card = new VBox(24);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(24, 16, 16, 16));
        card.setPrefWidth(140);
        card.setPrefHeight(160);
        card.setStyle("-fx-border-color: #262626; -fx-background-color: #0a0a0a; -fx-border-width: 1;");

        //TODO: Replace ImagePlaceHolder with actual image
        StackPane imagePlaceholder = new StackPane();
        imagePlaceholder.setPrefSize(64, 64);
        imagePlaceholder.setMinSize(64, 64);
        imagePlaceholder.setStyle("-fx-background-color: #111111; -fx-border-color: #262626; -fx-border-style: dashed;");

        Button equipBtn = createEquipButton(item);
        equipBtn.setMaxWidth(Double.MAX_VALUE); 

        card.getChildren().addAll(imagePlaceholder, equipBtn);
        return card;
    }

    private Button createEquipButton(Cosmetic item) {
        Button btn = new Button("EQUIP");
        btn.setCursor(javafx.scene.Cursor.HAND);
        btn.setStyle("-fx-background-color: transparent; -fx-border-color: #262626; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 15 5 15;");

        btn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                System.out.println("Equipped: " + item.getName());
                //TODO: ADD EQUIP LOGIC 
            }
        });
        return btn;
    }

    private List<Cosmetic> getOwnedCosmeticsFromDatabase() {
        return Main.user.getInventory().getOwnedItems();
    }
}