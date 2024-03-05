package com.example.frontend;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
//
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import javafx.collections.*;
import javafx.beans.value.*;

import com.example.frontend.DatabaseOperations.Inventory;

public class ManagerViewController implements Initializable{

    @FXML
    private ScrollPane managerScroll;
    @FXML
    private ImageView menu_close;
    @FXML
    private Pane testPane;

    @FXML
    private ImageView menu_open;

    @FXML
    private AnchorPane slide_menu;

    @FXML
    private ImageView switchBtn;
    private Boolean employeeView;

    @FXML
    private Pane lowStockPane;

    @FXML
    private Button updateInventoryButton;
    @FXML
    private Button addInventoryButton;

    @FXML
    private Button updateMenuButton;

    @FXML
    private Button switchSceneBtn;
    @FXML
    private Boolean managerView;
    Connection conn;

    ArrayList<CheckBox> checkInventory;
    ArrayList<Inventory> checkInventoryName;
    ArrayList<Inventory> checkedItems;
    @FXML
    void switchSceneButtonClicked(ActionEvent event) {
        try {
            Stage stage = (Stage) switchSceneBtn.getScene().getWindow();
            String name = "";
            if (managerView){
                name = "manager-graph-view.fxml";
                managerView = false;
            }
            else{
                name = "manager-view.fxml";
                managerView = true;
            }
            FXMLLoader loader = new FXMLLoader(StartApplication.class.getResource(name));
            Parent root = loader.load();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void switchButton(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/frontend/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Optionally, you can use an alert to notify the user that the view switch failed
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("View Switch Failed");
            alert.setContentText("Unable to load the login view.");
            alert.showAndWait();
        }
    }

    void populateLowStockPane(){

        // Setting up containers and description
        ScrollPane inventoryScroll = new ScrollPane();
        inventoryScroll.setPrefSize(290, 575);
        VBox lowStockVBox = new VBox(10);
        lowStockVBox.setPrefWidth(270);
        lowStockVBox.setPadding(new Insets(25));
        Label header = new Label("Low Stock Items");
        header.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 22));
        Label description = new Label("These items are low stock, we recommend that you restock them before they run out.");
        description.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 12));
        description.wrapTextProperty().setValue(true);
        description.setPrefWidth(285);
        lowStockVBox.getChildren().addAll(header, description);

        // Querying the database to see which items are low
        String name = "";
        int qty = 0;
        String unit = "";
        try{
            //Asking for all the menu items from the database
            Statement stmt = conn.createStatement();
            String sqlStatement = "SELECT name, quantity, unit FROM inventory WHERE quantity < 100 ORDER BY quantity ASC;";
            ResultSet result = stmt.executeQuery(sqlStatement);

            while (result.next()) {
                name = result.getString("name");
                qty = result.getInt("quantity");
                unit = result.getString("unit");
                Label toAdd = new Label("Item: " + name + "\nQuanitiy remaining: " + qty + " "  +  unit);
                toAdd.setPrefWidth(270);
                toAdd.setFont(Font.font("verdana", FontWeight.NORMAL, FontPosture.REGULAR, 12));
                toAdd.wrapTextProperty().setValue(true);

                // Adding the items that are low stock
                lowStockVBox.getChildren().add(toAdd);
            }
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Error accessing Database.");
        }

        // ADding the low stock items to the panel
        inventoryScroll.setContent(lowStockVBox);
        lowStockPane.getChildren().add(inventoryScroll);
    }

    @FXML
    void handleAddInventoryButton(ActionEvent event) {

        // New modal stage to lock interaction with other windows
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Update Inventory");

        // Define the modal content
        VBox modalVBox = new VBox(10);
        modalVBox.setPadding(new Insets(10));
        Label instructionLabel = new Label("Enter Inventory Details:");

        TextField inventoryFieldName = new TextField();
        TextField inventoryFieldQuantity = new TextField();
        TextField inventoryFieldUnit = new TextField();
        inventoryFieldName.setPromptText("Enter Item Name");
        inventoryFieldQuantity.setPromptText("Enter Item Quantity");
        inventoryFieldUnit.setPromptText("Enter Item Unit");

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> handleInventorySaveAction(inventoryFieldName.getText(), inventoryFieldQuantity.getText(), inventoryFieldUnit.getText(), modalStage));

        // Adding and displaying the items to the modal
        modalVBox.getChildren().addAll(instructionLabel, inventoryFieldName, inventoryFieldQuantity, inventoryFieldUnit, saveButton);
        Scene modalScene = new Scene(modalVBox, 300, 200);
        modalStage.setScene(modalScene);
        modalStage.showAndWait();
    }
    @FXML
    void handleUpdateInventoryButton(ActionEvent event) {

        // New modal stage to lock interaction with other windows
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Update Inventory");

        // Getting the current inventory items and adding them to a dropdown
        ComboBox inventoryList = new ComboBox();
        ArrayList<Inventory> inventoryItems = new ArrayList<Inventory>();
        try {
            String sqlStatement = "SELECT * FROM Inventory;";
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(sqlStatement);

            while (result.next()) {
                int id = result.getInt("Id");
                String name = result.getString("Name");
                int qty = result.getInt("Quantity");
                String unit = result.getString("Unit");
                Inventory toAdd = new Inventory(id, name, qty, unit);
                inventoryItems.add(toAdd);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error accessing Database.");
        }
        inventoryList.getItems().addAll(FXCollections.observableArrayList(inventoryItems));

        Label instructionLabel = new Label("Select Inventory Item:");
        Label quantityLabel = new Label("Quantity to add:");
        TextField inventoryFieldQuantity = new TextField();
        TextField inventoryFieldUnit = new TextField();

        // Updating the text fields with appropriate properties when an item is selected
        final String[] updateName = new String[1];
        inventoryList.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

            // if the item of the list is changed
            public void changed(ObservableValue ov, Number value, Number new_value)
            {
                // set the text for the label to the selected item
                instructionLabel.setText("Current stock: " + inventoryItems.get(new_value.intValue()).getQuantity());
                inventoryFieldUnit.setText("" + inventoryItems.get(new_value.intValue()).getUnit());
                updateName[0] = inventoryItems.get(new_value.intValue()).getName();
            }
        });

        // Define the modal content
        VBox modalVBox = new VBox(10);
        modalVBox.setPadding(new Insets(10));

        inventoryFieldQuantity.setPromptText("Enter Item Quantity");
        inventoryFieldUnit.setPromptText("Enter Item Unit");

        // Connecting the save button to update the database
        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> handleInventorySaveAction(updateName[0], inventoryFieldQuantity.getText(), inventoryFieldUnit.getText(), modalStage));

        modalVBox.getChildren().addAll(instructionLabel, inventoryList, quantityLabel, inventoryFieldQuantity, inventoryFieldUnit, saveButton);

        // Show the modal window
        Scene modalScene = new Scene(modalVBox, 280, 230);
        modalStage.setScene(modalScene);
        modalStage.showAndWait();
    }

    void setCheckedItems(){
        checkedItems.clear();
        for(int i = 0; i < checkInventory.size(); i++){
            if (checkInventory.get(i).isSelected()){
                checkedItems.add(checkInventoryName.get(i));
            }
        }
    }

    @FXML
    void handleUpdateMenuButton(ActionEvent event) {

        // New modal stage to lock interaction with other windows
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Add New Menu Item");

        // Scroll pane to hold the recipe items of the menu itmes
        ScrollPane recipeScroll = new ScrollPane();
        recipeScroll.setPadding(new Insets(10));

        VBox modalVBox = new VBox(10);
        Label instructionLabel = new Label("New Menu Item Details:");
        instructionLabel.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 13));

        // Labels and Text Fields asking the user for a new menu item info
        HBox descriptionHeaders = new HBox(5);
        Label nameLabel = new Label("Name: ");
        nameLabel.setPrefWidth(130);
        Label priceLabel = new Label("Price: ");
        priceLabel.setPrefWidth(60);
        Label caloriesLabel = new Label("Calories: ");
        caloriesLabel.setPrefWidth(70);
        Label categoryLabel = new Label("Category: ");
        categoryLabel.setPrefWidth(90);
        descriptionHeaders.getChildren().addAll(nameLabel, priceLabel, caloriesLabel, categoryLabel);
        HBox descriptionHbox = new HBox(5);
        TextField nameField = new TextField();
        nameField.setPrefWidth(130);
        TextField priceField = new TextField();
        priceField.setPrefWidth(60);
        TextField caloriesField = new TextField();
        caloriesField.setPrefWidth(70);
        TextField categoryField = new TextField();
        categoryField.setPrefWidth(90);
        descriptionHbox.getChildren().addAll(nameField, priceField, caloriesField, categoryField);

        // Section to allow user to enter in recipe quantity, units, and items
        Label recipeDes = new Label("Menu Item Recipe");
        recipeDes.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 13));
        modalVBox.getChildren().addAll(instructionLabel, descriptionHeaders, descriptionHbox, recipeDes);
        HBox recipeHeader = new HBox(10);
        Label nameHeader = new Label("Item Name");
        nameHeader.setPrefWidth(120);
        Label qtyHeader = new Label("Quantity");
        qtyHeader.setPrefWidth(80);
        Label unitHeader = new Label("Unit");
        unitHeader.setPrefWidth(120);

        // Setting the recipe items to be a part of a scroll pane then modal
        recipeHeader.getChildren().addAll(nameHeader, qtyHeader, unitHeader);
        modalVBox.getChildren().add(recipeHeader);

        // Populating recipe boxes with items that the user selected
        VBox recipeContainer = new VBox(10);
        setCheckedItems();
        for(int i = 0; i < checkedItems.size(); i++){
            HBox recipeItem = new HBox(10);

            TextField nameFiled = new TextField();
            nameFiled.setText(checkedItems.get(i).getName());
            nameFiled.setPrefWidth(120);
            TextField quantityField = new TextField();
            quantityField.setPrefWidth(80);
            TextField unitField = new TextField();
            unitField.setText(checkedItems.get(i).getUnit());
            unitField.setPrefWidth(120);

            recipeItem.getChildren().addAll(nameFiled, quantityField, unitField);
            recipeContainer.getChildren().add(recipeItem);
        }

        // Define the modal content
        recipeScroll.setContent(recipeContainer);
        modalVBox.getChildren().add(recipeScroll);
        modalVBox.setPadding(new Insets(20));



        // Updating the database with the contents of the new menu item through the modal
        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> {
            populateCheckedItemsQuantity(recipeContainer);
            handleMenuSaveAction(nameField.getText(), priceField.getText(), caloriesField.getText(), categoryField.getText(), modalStage);
        });

        saveButton.setAlignment(Pos.CENTER);
        modalVBox.getChildren().addAll(saveButton);

        // Showing the modal window
        Scene modalScene = new Scene(modalVBox, 420, 400);
        modalStage.setScene(modalScene);
        modalStage.showAndWait();
    }

    private void populateCheckedItemsQuantity(VBox recipeContainer) {
        
        for (int i = 0; i < recipeContainer.getChildren().size(); i++) {
            if (i >= checkedItems.size()) {
                // Handle the case where there are more recipeContainer children than checkedItems
                break;
            }
    
            Node node = recipeContainer.getChildren().get(i);
            if (node instanceof HBox) {
                HBox recipeItem = (HBox) node;
                if (recipeItem.getChildren().size() > 1 && recipeItem.getChildren().get(1) instanceof TextField) {
                    TextField quantityField = (TextField) recipeItem.getChildren().get(1);
                    String quantityText = quantityField.getText();
    
                    if (quantityText != null && !quantityText.isEmpty()) {
                        
                        int quantity = Integer.parseInt(quantityText);
                        checkedItems.get(i).setQuantity(quantity);
                       
                    }
                }
            }
        }
    }

    private void handleInventorySaveAction(String name, String quantity, String unit, Stage modalStage) {
        System.out.println("Updating Inventory Item:");
        System.out.println("Name: " + name);
        System.out.println("Quantity: " + quantity);
        System.out.println("Unit: " + unit);
    
        conn = DatabaseConnectionManager.getConnection();
        PreparedStatement pstmtCheck = null;
        PreparedStatement pstmtUpdate = null;
        PreparedStatement pstmtInsert = null;
    
        try {
            // Check if the inventory item exists
            String sqlQueryToCheck = "SELECT EXISTS(SELECT 1 FROM inventory WHERE name = ?)";
            pstmtCheck = conn.prepareStatement(sqlQueryToCheck);
            pstmtCheck.setString(1, name);
            ResultSet rs = pstmtCheck.executeQuery();
            boolean inventoryExist = false;
            if (rs.next()) {
                inventoryExist = rs.getBoolean(1);
            }
    
            if (inventoryExist) {
                // SQL statement to update inventory item
                String sqlStatement = "UPDATE inventory SET quantity = ?, unit = ? WHERE name = ?";
                pstmtUpdate = conn.prepareStatement(sqlStatement);
                pstmtUpdate.setInt(1, Integer.parseInt(quantity));       
                pstmtUpdate.setString(2, unit);
                pstmtUpdate.setString(3, name);
                int rowsAffected = pstmtUpdate.executeUpdate();
    
                if (rowsAffected > 0) {
                    System.out.println("Inventory updated successfully.");
                } else {
                    System.out.println("Inventory update failed. No item found with the specified name.");
                }
            } else {
                // SQL statement to insert a new inventory item
                String sqlInsert = "INSERT INTO inventory (name, quantity, unit) VALUES (?, ?, ?);";
                pstmtInsert = conn.prepareStatement(sqlInsert);
                pstmtInsert.setString(1, name);
                pstmtInsert.setInt(2, Integer.parseInt(quantity));
                pstmtInsert.setString(3, unit);
                pstmtInsert.executeUpdate();
                System.out.println("New inventory item created successfully.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error accessing Database.");
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format for quantity: " + e.getMessage());
        } finally {
            // Close resources
            try {
                if (pstmtCheck != null) pstmtCheck.close();
                if (pstmtUpdate != null) pstmtUpdate.close();
                if (pstmtInsert != null) pstmtInsert.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            modalStage.close();
        }
        setUpdateInventory();
        populateLowStockPane();
    }

    

    private void handleMenuSaveAction(String name, String price, String calories, String category, Stage modalStage) {
        System.out.println("Updating Menu Item:");
        System.out.println("Name: " + name);
        System.out.println("Price: " + price);
        System.out.println("Calories: " + calories);
        System.out.println("Category: " + category);
    
        conn = DatabaseConnectionManager.getConnection();
        PreparedStatement pstmtCheck = null;
        PreparedStatement pstmtUpdate = null;
        PreparedStatement pstmtInsert = null;

        try {
            // Check if the menu item exists
            String sqlQueryToCheck = "SELECT EXISTS(SELECT 1 FROM menu_item WHERE name = ?)";
            pstmtCheck = conn.prepareStatement(sqlQueryToCheck);
            pstmtCheck.setString(1, name);
            ResultSet rs = pstmtCheck.executeQuery();
            boolean menuExist = false;
            if (rs.next()) {
                menuExist = rs.getBoolean(1);
            }

            if (menuExist) {
                // Adjust the SQL statement according to your database schema
                String sqlStatement = "UPDATE menu_item SET price = ?, calories = ?, category = ? WHERE name = ?;";
        
                PreparedStatement pstmt = conn.prepareStatement(sqlStatement);
        
                // Assuming 'price' and 'calories' are stored as decimals and integers in your database
                pstmt.setDouble(1, Double.parseDouble(price));
                pstmt.setInt(2, Integer.parseInt(calories));
                pstmt.setString(3, category);
                pstmt.setString(4, name);
        
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Menu updated successfully.");
                } else {
                    System.out.println("Menu update failed. No item found with the specified name.");
                }
            } else {

                // SQL statement to insert a new menu item
                String sqlInsertMenu = "INSERT INTO menu_item (name, price, calories, category) VALUES (?, ?, ?, ?);";
                pstmtInsert = conn.prepareStatement(sqlInsertMenu);
                pstmtInsert.setString(1, name);
                pstmtInsert.setDouble(2, Double.parseDouble(price));
                pstmtInsert.setInt(3, Integer.parseInt(calories));
                pstmtInsert.setString(4, category);
                pstmtInsert.executeUpdate();

                // I need to get menu_item_id, inventory_item_id, qty
                // menu_item_id can be extracted by doing select respect to menu_item_name

                // i need to do a for loop for all recepie item
                    // inventory_item_id extracted from vector
                    // checkedItems vector gives in a format: [Bacon, Onion, Texas Toast]
                    // qty extracted from user input


                int menuID = -1;
                PreparedStatement pstmt2 = conn.prepareStatement("SELECT id from menu_item where name = ?");
                pstmt2.setString(1, name);
                ResultSet rs2 = pstmt2.executeQuery();
                if (rs2.next()) {
                    menuID = rs2.getInt("id");
                }

                // Add Recepie to Menu Item
                String sqlInsertRecipe = "INSERT INTO recipe (menu_item, inventory_item, qty) VALUES (?, ?, ?);";
                PreparedStatement pstmt3 = conn.prepareStatement(sqlInsertRecipe);
                for (int i = 0; i < checkedItems.size(); i++) {
                    
                    pstmt3.setInt(1, menuID);

                    // get Inventory ID
                    String inventoryName = checkedItems.get(i).getName();
                    int inventoryID = -1;
                    PreparedStatement pstmt4 = conn.prepareStatement("SELECT id from inventory where name = ?");
                    pstmt4.setString(1, inventoryName);

                    ResultSet rs4 = pstmt4.executeQuery();

                    if (rs4.next()) {
                        inventoryID = rs4.getInt("id");
                    }

                    pstmt3.setInt(2, inventoryID);

                    int quantity = checkedItems.get(i).getQuantity();
                    if (quantity < 0 || quantity > 100) {
                        throw new IllegalArgumentException("Quantity empty");
                    }
                    pstmt3.setInt(3, quantity);

                    pstmt3.executeUpdate();
                }

                for (int i = 0; i < checkedItems.size(); i++) {
                    System.out.println("Recipe " + (i + 1) +" : " + checkedItems.get(i).getName() + " " + checkedItems.get(i).getQuantity() + " " + checkedItems.get(i).getUnit());
                }
                
                System.out.println("New menu item created successfully.");
            }

            
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error accessing Database.");
        } catch (NumberFormatException e) {
            System.out.println("Error in number format: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        } 
        finally {
            modalStage.close();
        }
    }

    @FXML
    void close_menu(MouseEvent event) {
        // Setting the side menu to close when press the icon
        TranslateTransition slide =  new TranslateTransition();
        slide.setDuration(Duration.seconds(0.5));
        slide.setNode(slide_menu);

        slide.setToX(-100);
        slide.play();

        slide_menu.setTranslateX(0);

        slide.setOnFinished((ActionEvent e) ->{
            menu_close.setVisible(false);
            menu_open.setVisible(true);
        });
    }

    @FXML
    void open_menu(MouseEvent event) {
        // Setting the side menu to open when clicking the menu icon
        TranslateTransition slide =  new TranslateTransition();
        slide.setDuration(Duration.seconds(0.5));
        slide.setNode(slide_menu);

        slide.setToX(0);
        slide.play();

        slide_menu.setTranslateX(-100);

        slide.setOnFinished((ActionEvent e) ->{
            menu_close.setVisible(true);
            menu_open.setVisible(false);
        });
    }

    public void setUpdateInventory(){

        // Resetting the inventory items when inventory is added or updated
        checkInventory.clear();
        checkInventoryName.clear();
        String name = "";
        String unit = "";
        int quant = 0;
        int id = 0;

        // Requesting all inventory items from the database
        conn = DatabaseConnectionManager.getConnection();
        VBox itemsVertical = new VBox(20);
        try {
            String sqlStatement = "SELECT * FROM inventory;";
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(sqlStatement);

            // Adding column header descriptions
            HBox toAdd = new HBox(20);
            toAdd.setStyle("-fx-border-color: black");
            toAdd.setPrefWidth(630);
            toAdd.setPadding(new Insets(10, 10, 10, 10) );
            Label nameLabel = new Label("Name");
            Label idLabel = new Label("Id");
            Label quantLabel = new Label("Qty");
            Label holdSpace = new Label("");
            CheckBox c = new CheckBox();

            // Setting styles
            nameLabel.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 14));
            idLabel.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 14));
            quantLabel.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 14));
            holdSpace.setPrefWidth(30);
            nameLabel.setPrefWidth(430);
            idLabel.setPrefWidth(30);
            quantLabel.setPrefWidth(40);

            toAdd.getChildren().addAll(holdSpace, idLabel, nameLabel, quantLabel);
            itemsVertical.getChildren().add(toAdd);

            // Adding all xthe inventory items to the list
            while (result.next()) {
                name = result.getString("name");
                unit = result.getString("unit");
                quant = result.getInt("quantity");
                id = result.getInt("id");

                toAdd = new HBox(20);
                toAdd.setStyle("-fx-border-color: black");
                toAdd.setPrefWidth(500);
                toAdd.setPadding(new Insets(10, 10, 10, 10) );

                nameLabel = new Label(name);
                idLabel = new Label("" + id);
                quantLabel = new Label("" + quant);
                c = new CheckBox();

                // Creating new inventory items and adding them to the list to keep track
                Inventory newItem = new Inventory(id, name, quant, unit);
                checkInventory.add(c);
                checkInventoryName.add(newItem);

                // Stylizing items
                nameLabel.setFont(Font.font("verdana", FontWeight.NORMAL, FontPosture.REGULAR, 14));
                idLabel.setFont(Font.font("verdana", FontWeight.NORMAL, FontPosture.REGULAR, 14));
                quantLabel.setFont(Font.font("verdana", FontWeight.NORMAL, FontPosture.REGULAR, 14));
                c.setPrefWidth(30);
                nameLabel.setPrefWidth(400);
                idLabel.setPrefWidth(30);
                quantLabel.setPrefWidth(50);

                toAdd.getChildren().addAll(c, idLabel, nameLabel, quantLabel);
                itemsVertical.getChildren().add(toAdd);
            }
            managerScroll.setContent(itemsVertical);
           } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error accessing Database.");
        }
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        menu_close.setVisible(false);
        menu_open.setVisible(true);
        slide_menu.setTranslateX(-100);
        managerView = true;
        employeeView = false;

        // Assigning functions to buttons
        updateInventoryButton.setOnAction(this::handleUpdateInventoryButton);
        addInventoryButton.setOnAction(this::handleAddInventoryButton);
        updateMenuButton.setOnAction(this::handleUpdateMenuButton);
        managerScroll.setPadding(new Insets(20, 20, 20, 20));

        // Lists to keep track of inventory items and which ones are checked
        checkInventory = new ArrayList<>();
        checkInventoryName = new ArrayList<>();
        checkedItems = new ArrayList<>();

        // Populating the inventory and see which items are low stock
        setUpdateInventory();
        populateLowStockPane();
    }


}