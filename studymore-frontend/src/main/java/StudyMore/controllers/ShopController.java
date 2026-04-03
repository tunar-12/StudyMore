package StudyMore.controllers;

import StudyMore.models.Cosmetic;
import StudyMore.models.CosmeticType;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import java.util.ArrayList;
import java.util.List;

public class ShopController {

    @FXML private Label balanceLabel;
    @FXML private HBox mascotCatsContainer;
    @FXML private HBox catHousesContainer;
    @FXML private HBox cosmeticsExtrasContainer;

    @FXML
    public void initialize() {
        balanceLabel.setText("0");
        loadShopUI();
    }

    private void loadShopUI() {

        List<Cosmetic> shopItems = getShopItemsFromDatabase();

        for (Cosmetic item : shopItems) {
            Pane itemCard = createShopItemCard(item);
            if(item.getType() == CosmeticType.MASCOT_SKIN) {
                mascotCatsContainer.getChildren().add(itemCard);  
            } else if (item.getType() == CosmeticType.MASCOT_HOUSE) {
                catHousesContainer.getChildren().add(itemCard);
                
            }else if (item.getType() == CosmeticType.BANNER || item.getType() == CosmeticType.BACKGROUND) {
                cosmeticsExtrasContainer.getChildren().add(itemCard);
            }
        }
    }

    //all items will use the same box
    private VBox createShopItemCard(Cosmetic item) {
        VBox card = new VBox(16);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(16));
        card.setPrefWidth(220);
        card.setStyle("-fx-border-color: #262626; -fx-background-color: #0a0a0a; -fx-border-width: 1; -fx-background-radius: 4; -fx-border-radius: 4;");

        StackPane imagePlaceholder = new StackPane();
        imagePlaceholder.setPrefSize(180, 100);
        imagePlaceholder.setMinSize(180, 100);
        imagePlaceholder.setStyle("-fx-background-color: #111111; -fx-border-color: #262626; -fx-border-style: dashed;");

        Label nameLabel = new Label(item.getName().toUpperCase());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        Button buyBtn = createBuyButton(item);
        card.getChildren().addAll(imagePlaceholder, nameLabel, buyBtn);
        return card;
    }

private Button createBuyButton(Cosmetic item) {
        String buttonText = "BUY (" + item.getPrice() + ")";
        Button btn = new Button(buttonText);
                    
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #fbbf24; -fx-border-color: #fbbf24; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-font-weight: bold; -fx-padding: 7 15; -fx-cursor: hand;");
        btn.setOnAction(new EventHandler<ActionEvent>() {
             //TODO: Implement the actual action the buy button does
            public void handle(ActionEvent event) {
                System.out.println("Attempting to buy: " + item.getName() + " for " + item.getPrice() + " coins.");
            }
        });
        
        return btn;
    }

    // TODO: Fetch data from database. Test data is used here for now 
    private List<Cosmetic> getShopItemsFromDatabase() {
        List<Cosmetic> items = new ArrayList<>();
        long id = 1; 
        items.add(new Cosmetic(id++, "Cat1", CosmeticType.MASCOT_SKIN, 1000, "", ""));
        items.add(new Cosmetic(id++, "house1", CosmeticType.MASCOT_HOUSE, 100, "", ""));
        items.add(new Cosmetic(id++, "Banner1", CosmeticType.BANNER, 1000, "", ""));
        items.add(new Cosmetic(id++, "Avatar1", CosmeticType.BANNER, 1000, "", ""));
        
        return items;
    }
}