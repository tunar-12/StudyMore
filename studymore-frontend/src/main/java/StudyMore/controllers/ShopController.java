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

public class ShopController {

    @FXML private Label balanceLabel;
    @FXML private HBox mascotCatsContainer;
    @FXML private HBox catHousesContainer;
    @FXML private HBox cosmeticsExtrasContainer;
    @FXML private VBox titlesContainer;
    @FXML private HBox medalsContainer;

    @FXML
    public void initialize() {
        // Main.mngr.updateUserCoinBalance(Main.user.getUserId(), 10000);
        // Main.user.setCoinBalance(10000); // FOR CHECKING 
        
        //Load user coin balance
        balanceLabel.setText(String.valueOf(Main.user.getCoinBalance()));
        
        //loads the shop with items not owned by the user 
        loadShopUI();
    }


    private void loadShopUI() {
        List<Cosmetic> shopItems = getShopItemsFromDatabase();

        for (Cosmetic item : shopItems) {
            
            //title box is different as it has no image
            if (item.getType() == CosmeticType.TITLE) {
                titlesContainer.getChildren().add(createTitleItemCard(item));
            } 
            //All other items use same box
            else {
                Pane itemCard = createShopItemCard(item);
                
                // box added to relavant container
                if (item.getType() == CosmeticType.MASCOT_SKIN) {
                    mascotCatsContainer.getChildren().add(itemCard);  
                }else if (item.getType() == CosmeticType.MASCOT_HOUSE) {
                    catHousesContainer.getChildren().add(itemCard);
                }else if (item.getType() == CosmeticType.BANNER || item.getType() == CosmeticType.BACKGROUND) {
                    cosmeticsExtrasContainer.getChildren().add(itemCard);
                }else if (item.getType() == CosmeticType.MEDAL) {
                    medalsContainer.getChildren().add(itemCard);
                }
            }
        }
        // Check all containers after loading to see if the user has already bought everything
        checkEmptyContainer(mascotCatsContainer);
        checkEmptyContainer(catHousesContainer);
        checkEmptyContainer(cosmeticsExtrasContainer);
        checkEmptyContainer(titlesContainer);
        checkEmptyContainer(medalsContainer);
    }

    // if user has bought everything show appropriate msg
    private void checkEmptyContainer(Pane container) { 
        if (container.getChildren().isEmpty()) {
            Label emptyLabel = new Label("YOU HAVE BOUGHT EVERYTHING ALREADY");
            emptyLabel.setStyle("-fx-text-fill: #737373; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 32;");
            container.getChildren().add(emptyLabel);
        }
    }

    // Horizontal box for title without any picture 
    private HBox createTitleItemCard(Cosmetic item) {
        HBox card = new HBox(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16));
        card.setMaxWidth(Double.MAX_VALUE); 
        card.setStyle("-fx-border-color: #262626; -fx-background-color: #0a0a0a; -fx-border-width: 1; -fx-background-radius: 4; -fx-border-radius: 4;");

        Label nameLabel = new Label(item.getName().toUpperCase());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button buyBtn = createBuyButton(item, card);
        card.getChildren().addAll(nameLabel, spacer, buyBtn);
        
        return card;
    }

    //for other items except title
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

    // accepts both hbox and vbox
    private Button createBuyButton(Cosmetic item, Pane card) {
        Button btn = new Button("BUY (" + item.getPrice() + ")");    
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #fbbf24; -fx-border-color: #fbbf24; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-font-weight: bold; -fx-padding: 7 15; -fx-cursor: hand;");
        
        btn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                int currentBalance = Main.user.getCoinBalance();
                int price = item.getPrice();
                
                //Check if user has enough coins
                if (currentBalance >= price) {
                    // shows the confirmation overlay
                    showConfirmationOverlay(new Runnable() {
                        @Override
                        public void run() {
                            int newBalance = currentBalance - price; 
                            
                            // Update memory and Database
                            Main.user.setCoinBalance(newBalance);    
                            Main.user.getInventory().addItem(item);  
                            Main.mngr.updateUserCoinBalance(Main.user.getUserId(), newBalance); 
                            item.obtain(Main.user.getUserId(), Main.mngr); 
                            
                            // Update balance on gui
                            balanceLabel.setText(String.valueOf(newBalance)); 
                            
                            // Remove the purchased item card from the gui
                            Pane parentContainer = (Pane) card.getParent();
                            parentContainer.getChildren().remove(card); 
                            
                            // If that was the last item, show the empty label
                            checkEmptyContainer(parentContainer);
                        }
                    });

                } else {
                    // Show error overlay if they are brokies without money
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

            //finds the highest level StackPane
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
                
                //Bind dimensions so it resizes with the window
                overlay.prefWidthProperty().bind(finalRootStack.widthProperty());
                overlay.prefHeightProperty().bind(finalRootStack.heightProperty());

                finalRootStack.getChildren().add(overlay); 
                overlay.toFront(); 

                confirmBtn.setOnAction(new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent event) {
                        onConfirmAction.run(); // runs the purchase logic passed in earlier
                        finalRootStack.getChildren().remove(overlay); // Close overlay
                    }
                });

                cancelBtn.setOnAction(new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent event) {
                        finalRootStack.getChildren().remove(overlay); // Close overlay without purchasing
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showErrorOverlay(String messageText) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/StudyMore/fxml/ErrorOverlay.fxml"));
            VBox overlay = loader.load();

            // Find the specific text label in the FXML and update it with the coins needed
            Label msgLabel = (Label) overlay.lookup("#errorMessageLabel");
            if (msgLabel != null) {
                msgLabel.setText(messageText);
            }

            Button okBtn = (Button) overlay.lookup("#errorOkButton");

            // find the highest level StackPane
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

                okBtn.setOnAction(new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent event) {
                        finalRootStack.getChildren().remove(overlay); // Dismiss error
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private List<Cosmetic> getShopItemsFromDatabase() {
        List<Cosmetic> allItems = Main.mngr.getAllCosmetics(); //get all items
        List<Cosmetic> ownedItems = Main.user.getInventory().getOwnedItems(); // get owned items
        List<Cosmetic> unownedItems = new ArrayList<>(); // get unowned items using loops

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