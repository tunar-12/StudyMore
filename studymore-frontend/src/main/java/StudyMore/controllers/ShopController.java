package StudyMore.controllers;

import StudyMore.Main;
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
import javafx.scene.control.Alert;


public class ShopController {

    @FXML private Label balanceLabel;
    @FXML private HBox mascotCatsContainer;
    @FXML private HBox catHousesContainer;
    @FXML private HBox cosmeticsExtrasContainer;

    @FXML
    public void initialize() {
        balanceLabel.setText(String.valueOf(Main.user.getCoinBalance()));
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

        javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView();
        imageView.setFitWidth(160);
        imageView.setFitHeight(80);
        imageView.setPreserveRatio(true);

        try {
            String imageResourcePath = "/StudyMore/" + item.getImagePath();
            java.io.InputStream imageStream = getClass().getResourceAsStream(imageResourcePath);
            if (imageStream != null) {
                javafx.scene.image.Image img = new javafx.scene.image.Image(imageStream);
                imageView.setImage(img);
            } else {
                System.out.println("COULD NOT FIND IMAGE: " + imageResourcePath); 
            }
        } catch (Exception e) {
            System.out.println("Error loading image for " + item.getName());
        }

        StackPane imagePlaceholder = new StackPane(imageView);
        imagePlaceholder.setPrefSize(180, 100);
        imagePlaceholder.setMinSize(180, 100);
        imagePlaceholder.setStyle("-fx-background-color: #111111; -fx-border-color: #262626; -fx-border-style: dashed;");

        Label nameLabel = new Label(item.getName().toUpperCase());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        Button buyBtn = createBuyButton(item, card);
        card.getChildren().addAll(imagePlaceholder, nameLabel, buyBtn);
        return card;
    }

    private Button createBuyButton(Cosmetic item, VBox card) {
        String buttonText = "BUY (" + item.getPrice() + ")";
        Button btn = new Button(buttonText);    
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #fbbf24; -fx-border-color: #fbbf24; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-font-weight: bold; -fx-padding: 7 15; -fx-cursor: hand;");
        
        btn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                //check if the user have enough money
                int currentBalance = Main.user.getCoinBalance();
                int price = item.getPrice();
                if (currentBalance >= price) {
                    int newBalance = currentBalance - price; //deduct the money
                    Main.user.setCoinBalance(newBalance);    //update their balance 
                    Main.user.getInventory().addItem(item);  //add the bought item in their owned items
                    Main.mngr.updateUserCoinBalance(Main.user.getUserId(), newBalance); //save new balance 
                    item.obtain(Main.user.getUserId(), Main.mngr); //save item to owned item
                    balanceLabel.setText(String.valueOf(newBalance)); //show new balacne on top right
                    ((javafx.scene.layout.Pane) card.getParent()).getChildren().remove(card); //remove this item from shop
                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Purchase Successful!");
                    success.setHeaderText(null);
                    success.setContentText("You successfully bought " + item.getName());
                    success.show();

                } else {
                    // SHOW VISUAL
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Not Enough Coins");
                    error.setHeaderText(null);
                    int coinsNeeded = price - currentBalance; //HOW MANY MORE COINS NEEDED
                    error.setContentText("You need " + coinsNeeded + " more coins to buy this.");
                    error.show();
                }
            }
        });
        
        return btn;
    }

    private List<Cosmetic> getShopItemsFromDatabase() {
        List<Cosmetic> allItems = Main.mngr.getAllCosmetics();
        
        //Get the items the user already owns
        List<Cosmetic> ownedItems = Main.user.getInventory().getOwnedItems();
        
        //Create a empty list for the items we actually want to show in the shop
        List<Cosmetic> unownedItems = new ArrayList<>();

        // Remove the already owned items
        for (Cosmetic catalogItem : allItems) {
            boolean alreadyOwnsIt = false;
            
            for (Cosmetic ownedItem : ownedItems) {
                if (ownedItem.getName().equals(catalogItem.getName())) {
                    alreadyOwnsIt = true;
                    break; 
                }
            }
            if (!alreadyOwnsIt) {
                unownedItems.add(catalogItem);
            }
        }
        
        return unownedItems;
    }
}