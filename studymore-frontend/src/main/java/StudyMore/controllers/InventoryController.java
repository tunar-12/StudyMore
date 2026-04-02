package StudyMore.controllers;

import java.util.ArrayList;
import java.util.List;
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
    @FXML private VBox avatarsContainer;
    @FXML private HBox mascotSkinsContainer;
    @FXML private HBox mascotHousesContainer;

    @FXML
    public void initialize() {
        loadInventoryUI();
    }

    private void loadInventoryUI() {
        List<Cosmetic> myItems = getOwnedCosmeticsFromDatabase();//myItems arraylist stores all the cosmetic items owned by the user 

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
            } else if (item.getType() == CosmeticType.AVATAR) { 
                avatarsContainer.getChildren().add(itemNode); 
            }
        }
    }

    private Node createItemBox(Cosmetic item) { //Create different kind of node/box for different cosmetic item type
        if (item.getType() == CosmeticType.BANNER || item.getType() == CosmeticType.MASCOT_SKIN || item.getType() == CosmeticType.MASCOT_HOUSE) {
            return createBannerStyleCard(item);
        } else if (item.getType() == CosmeticType.TITLE) {
            return createTitleStyleCard(item);
        } else if (item.getType() == CosmeticType.AVATAR) {
            return createAvatarStyleCard(item);
        } else if (item.getType() == CosmeticType.MEDAL) {
            return createMedalStyleCard(item);
        }
        return new VBox(); 
    }

    private VBox createBannerStyleCard(Cosmetic item) { //for banner, cats and cat houses 
        VBox card = new VBox(16);
        card.setPadding(new Insets(16));
        card.setPrefWidth(280);
        card.setStyle("-fx-border-color: #262626; -fx-background-color: #0a0a0a; -fx-border-width: 1;");

        //Image placeholder
        //TO DO: ADD ACTUAL IMAGES TO imagePlaceholder
        StackPane imagePlaceholder = new StackPane();
        imagePlaceholder.setPrefHeight(120);
        imagePlaceholder.setStyle("-fx-background-color: #111111; -fx-border-color: #262626; -fx-border-style: dashed;");

        HBox bottomRow = new HBox(); //area bellow the image
        bottomRow.setAlignment(Pos.CENTER);

        Label ownedLabel = new Label("OWNED");
        ownedLabel.setStyle("-fx-text-fill: #a3a3a3; -fx-font-weight: bold; -fx-font-size: 10px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button equipBtn = createEquipButton(item); //TO DO: Button does nothing right now 

        bottomRow.getChildren().addAll(ownedLabel, spacer, equipBtn);
        card.getChildren().addAll(imagePlaceholder, bottomRow);

        return card;
    }

    private HBox createTitleStyleCard(Cosmetic item) { //for title items types
        HBox card = new HBox(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(12, 16, 12, 16));
        card.setPrefWidth(350);
        card.setStyle("-fx-border-color: #262626; -fx-background-color: #0a0a0a; -fx-border-width: 1;");

        Label nameLabel = new Label(item.getName().toUpperCase());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button equipBtn = createEquipButton(item); //TO DO

        card.getChildren().addAll(nameLabel, spacer, equipBtn);
        return card;
    }

    private HBox createAvatarStyleCard(Cosmetic item) {
        HBox card = new HBox(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(12, 16, 12, 16));
        card.setPrefWidth(350);
        card.setStyle("-fx-border-color: #262626; -fx-background-color: #0a0a0a; -fx-border-width: 1;");

        //TO DO: ADD AVATAR IMAGES HERE TO imagePlaceholder
        StackPane imagePlaceholder = new StackPane();
        imagePlaceholder.setPrefSize(48, 48);
        imagePlaceholder.setMinSize(48, 48);
        imagePlaceholder.setStyle("-fx-background-color: #111111; -fx-border-color: #262626; -fx-border-style: dashed;");

        Label nameLabel = new Label(item.getName().toUpperCase());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button equipBtn = createEquipButton(item);

        card.getChildren().addAll(imagePlaceholder, nameLabel, spacer, equipBtn);
        return card;
    }
    
    private VBox createMedalStyleCard(Cosmetic item) {
        VBox card = new VBox(24);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(24, 16, 16, 16));
        card.setPrefWidth(140);
        card.setPrefHeight(160);
        card.setStyle("-fx-border-color: #262626; -fx-background-color: #0a0a0a; -fx-border-width: 1;");

        //TODO: ADD ACTUAL MEDAL IMAGES TO imagePlaceholder
        StackPane imagePlaceholder = new StackPane();
        imagePlaceholder.setPrefSize(64, 64);
        imagePlaceholder.setMinSize(64, 64);
        imagePlaceholder.setStyle("-fx-background-color: #111111; -fx-border-color: #262626; -fx-border-style: dashed;");

        Button equipBtn = createEquipButton(item);
        equipBtn.setMaxWidth(Double.MAX_VALUE); 

        card.getChildren().addAll(imagePlaceholder, equipBtn);
        return card;
    }

    //created general equip button for now to tests. Implement different buttons for different object later
    private Button createEquipButton(Cosmetic item) {
        Button btn = new Button("EQUIP");
        btn.setCursor(javafx.scene.Cursor.HAND);
        btn.setStyle("-fx-background-color: transparent; -fx-border-color: #262626; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 15 5 15;");

        btn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                System.out.println("Equipped: " + item.getName());
            }
        });
        return btn;
    }

    //TO DO: Retrieve actual data from the databases. Using test data for now 
    private List<Cosmetic> getOwnedCosmeticsFromDatabase() {
        List<Cosmetic> items = new ArrayList<>();
        long id = 1;
        items.add(new Cosmetic(id++, "Banner1", CosmeticType.BANNER, 500, "", ""));
        items.add(new Cosmetic(id++, "Title1", CosmeticType.TITLE, 200, "", ""));
        items.add(new Cosmetic(id++, "Medel1", CosmeticType.MEDAL, 1000, "", ""));
        items.add(new Cosmetic(id++, "avatar1", CosmeticType.AVATAR, 0, "", ""));
        items.add(new Cosmetic(id++, "cat1", CosmeticType.MASCOT_SKIN, 1500, "", ""));
        items.add(new Cosmetic(id++, "house1", CosmeticType.MASCOT_HOUSE, 3000, "", ""));

        return items;
    }
}