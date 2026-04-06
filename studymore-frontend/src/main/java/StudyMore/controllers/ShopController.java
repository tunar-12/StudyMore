package StudyMore.controllers;

import StudyMore.Main;
import StudyMore.models.Cosmetic;
import StudyMore.models.CosmeticType;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
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
        // Main.mngr.updateUserCoinBalance(Main.user.getUserId(), 10000);
        // Main.user.setCoinBalance(10000); //FOR CHECKING 
        
        balanceLabel.setText(String.valueOf(Main.user.getCoinBalance()));
        loadShopUI();
    }

    private void loadShopUI() {
        List<Cosmetic> shopItems = getShopItemsFromDatabase();

        for (Cosmetic item : shopItems) {
            if (item.getType() == CosmeticType.TITLE || item.getType() == CosmeticType.MEDAL) { //ignore medals and titles
                continue; 
            }

            Pane itemCard = createShopItemCard(item);
            
            if(item.getType() == CosmeticType.MASCOT_SKIN) {
                mascotCatsContainer.getChildren().add(itemCard);  
            } else if (item.getType() == CosmeticType.MASCOT_HOUSE) {
                catHousesContainer.getChildren().add(itemCard);
            } else if (item.getType() == CosmeticType.BANNER || item.getType() == CosmeticType.BACKGROUND) {
                cosmeticsExtrasContainer.getChildren().add(itemCard);
            }
        }
    }

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

        try { //TO PREVENT TITLE IMAGES
            java.io.InputStream imageStream = getClass().getResourceAsStream("/StudyMore/" + item.getImagePath());
            if (imageStream != null) {
                imageView.setImage(new javafx.scene.image.Image(imageStream));
                imageStream.close(); 
            } 
        } catch (Exception e) {
            System.out.println("Error loading image for: " + item.getName());
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
        Button btn = new Button("BUY (" + item.getPrice() + ")");    
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #fbbf24; -fx-border-color: #fbbf24; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-font-weight: bold; -fx-padding: 7 15; -fx-cursor: hand;");
        
        btn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                int currentBalance = Main.user.getCoinBalance();
                int price = item.getPrice();
                
                if (currentBalance >= price) {
                    
                    showConfirmationOverlay(new Runnable() {
                        @Override
                        public void run() {
                            int newBalance = currentBalance - price; 
                            Main.user.setCoinBalance(newBalance);    
                            Main.user.getInventory().addItem(item);  
                            Main.mngr.updateUserCoinBalance(Main.user.getUserId(), newBalance); 
                            item.obtain(Main.user.getUserId(), Main.mngr); 
                            balanceLabel.setText(String.valueOf(newBalance)); 
                            ((javafx.scene.layout.Pane) card.getParent()).getChildren().remove(card); 
                        }
                    });

                } else {
                    showErrorOverlay("You need " + (price - currentBalance) + " more coins to buy this item.");
                }
            }
        });
        
        return btn;
    }

    private void showConfirmationOverlay(Runnable onConfirmAction) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/StudyMore/fxml/ConfirmationOverlay.fxml"));
            VBox overlay = loader.load();

            Button confirmBtn = (Button) overlay.lookup("#confirmProceedButton");
            Button cancelBtn = (Button) overlay.lookup("#confirmCancelButton");

            StackPane rootStack = null;
            Parent parent = mascotCatsContainer.getParent();
            
            while (parent != null) { //get the outermost parent to add the overlay to
                if (parent instanceof StackPane) {
                    rootStack = (StackPane) parent;
                }
                parent = parent.getParent();
            }

            if (rootStack != null) {
                final StackPane finalRootStack = rootStack;
                
                overlay.prefWidthProperty().bind(finalRootStack.widthProperty());
                overlay.prefHeightProperty().bind(finalRootStack.heightProperty());

                finalRootStack.getChildren().add(overlay); 
                overlay.toFront(); 

                confirmBtn.setOnAction(new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent event) {
                        onConfirmAction.run();
                        finalRootStack.getChildren().remove(overlay);
                    }
                });

                cancelBtn.setOnAction(new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent event) {
                        finalRootStack.getChildren().remove(overlay);
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Cosmetic> getShopItemsFromDatabase() {
        List<Cosmetic> allItems = Main.mngr.getAllCosmetics();
        List<Cosmetic> ownedItems = Main.user.getInventory().getOwnedItems();
        List<Cosmetic> unownedItems = new ArrayList<>();

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

    private void showErrorOverlay(String messageText) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/StudyMore/fxml/ErrorOverlay.fxml"));
            VBox overlay = loader.load();

            // Find the label and set the custom text
            Label msgLabel = (Label) overlay.lookup("#errorMessageLabel");
            if (msgLabel != null) {
                msgLabel.setText(messageText);
            }

            Button okBtn = (Button) overlay.lookup("#errorOkButton");

            StackPane rootStack = null;
            Parent parent = mascotCatsContainer.getParent();
            
            while (parent != null) {
                if (parent instanceof StackPane) {
                    rootStack = (StackPane) parent;
                }
                parent = parent.getParent();
            }

            if (rootStack != null) {
                final StackPane finalRootStack = rootStack;
                
                overlay.prefWidthProperty().bind(finalRootStack.widthProperty());
                overlay.prefHeightProperty().bind(finalRootStack.heightProperty());

                finalRootStack.getChildren().add(overlay);
                overlay.toFront();

                // Remove overlay when click button
                okBtn.setOnAction(new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent event) {
                        finalRootStack.getChildren().remove(overlay);
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}