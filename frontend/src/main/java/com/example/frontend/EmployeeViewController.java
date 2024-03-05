package com.example.frontend;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.ResourceBundle;
//
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import com.example.frontend.DatabaseOperations.OrderItem;


public class EmployeeViewController implements Initializable{

    @FXML
    private ImageView menu_close;
    @FXML
    private ImageView menu_open;
    @FXML
    private AnchorPane slide_menu;
    @FXML
    private AnchorPane menu_pane;
    @FXML
    private ScrollPane menu_scroll;
    @FXML
    private ScrollPane checkoutScroll;
    @FXML
    private Label totalWithTax;
    @FXML
    private Label orderTotal;
    private double orderTotalPrice;
    private VBox checkoutVbox;
    @FXML
    private ImageView switchBtn;
    private Stage primaryStage;

    @FXML
    private Label userProfile;
    private Boolean employeeView;
    ArrayList<Label> orderQuantList;
    Connection conn;

    private ObservableList<OrderItem> currentOrder = FXCollections.observableArrayList();

    // adding an item to an order
    public void addItemToOrder(int menuItemId, int quantity, String name, double price) {
        for (int i = 0; i < currentOrder.size(); i++) {
            OrderItem item = currentOrder.get(i);

            // the item already exists, just update the quantity
            if (item.getMenuItemId() == menuItemId) {
                int newQuantity = item.getQuantity() + quantity;
                orderQuantList.get(i).setText("" + newQuantity);
                currentOrder.set(currentOrder.indexOf(item), new OrderItem(menuItemId, newQuantity, name, price));
                orderTotalPrice += price;

                // Updating the GUI
                String subTotal = String.format("$%.2f", orderTotalPrice);
                String total = String.format("$%.2f", orderTotalPrice * 1.0825);
                orderTotal.setText(subTotal);
                totalWithTax.setText(total);
                return;
            }
        }

        // the item doesn't exist, add to the order total and summary page
        currentOrder.add(new OrderItem(menuItemId, quantity, name, price));
        Label orderName = new Label(name);
        orderName.setPrefWidth(170);
        orderName.wrapTextProperty().setValue(true);
        Label orderQuant = new Label("" + quantity);
        orderQuant.setPrefWidth(20);
        String itemPrice = String.format("$%.2f", price);
        Label orderPrice = new Label(itemPrice);
        orderPrice.setPrefWidth(50);
        HBox container = new HBox(10);
        container.setPadding(new Insets(5, 2, 5, 2));

        // setting the new item as a child pane so that it appears in the summary
        container.getChildren().addAll(orderName, orderQuant, orderPrice);
        checkoutVbox.getChildren().add(container);

        orderTotalPrice += price;
        String subTotal = String.format("$%.2f", orderTotalPrice);
        String total = String.format("$%.2f", orderTotalPrice * 1.0825);
        orderTotal.setText(subTotal);
        totalWithTax.setText(total);
        orderQuantList.add(orderQuant);
    }

    // clear out the order summary
    @FXML
    void cancelOrder(){

        // Resetting all necessary values to clear the order summary
        currentOrder.clear();
        orderTotalPrice = 0.0;
        orderQuantList.clear();
        totalWithTax.setText("$0.00");
        orderTotal.setText("$0.00");
        checkoutVbox.getChildren().clear();
    }

    public void removeItemFromOrder(int menuItemId) {
        currentOrder.removeIf(item -> item.getMenuItemId() == menuItemId);
    }

    public void updateItemQuantityInOrder(int menuItemId, int newQuantity, String name, double price) {
        currentOrder.removeIf(item -> item.getMenuItemId() == menuItemId);
        if (newQuantity > 0) {
            currentOrder.add(new OrderItem(menuItemId, newQuantity, name, price));
        }
    }

   // Function to switch pages
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


    @FXML
    void close_menu(MouseEvent event) {
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

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    // Submitting an order and notifying the database
    @FXML
    void complete_order(MouseEvent event) {
        // Create a new Stage for the dialog
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(primaryStage);

        // Create the layout for the dialog
        VBox dialogVBox = new VBox(20);
        dialogVBox.setAlignment(Pos.CENTER);

        // Add components to the layout
        Label nameLabel = new Label("Name on Order");
        TextField nameField = new TextField();
        String customerName = nameField.getText();

        Button continueButton = new Button("Continue to Payment");
        continueButton.setOnAction(e -> {
            // Handle the continue action
            dialogStage.close();
            DatabaseOperations.createOrderAndUpdateInventory(DatabaseOperations.currentEmployee.getId(), currentOrder, customerName);
            currentOrder.clear();
            checkoutVbox.getChildren().clear();
            orderQuantList.clear();

            orderTotalPrice = 0.0;
            totalWithTax.setText("$0.00");
            orderTotal.setText("$0.00");
            checkoutVbox.getChildren().clear();
            orderQuantList.clear();
        });
        dialogVBox.getChildren().addAll(nameLabel, nameField, continueButton);

        // Set the scene and show the stage
        Scene dialogScene = new Scene(dialogVBox, 300, 200);
        dialogStage.setScene(dialogScene);
        dialogStage.showAndWait();
    }

    // Setting variables
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {

        // Setting switch screen to say that we are on employee view
        employeeView = true;

        // Initializing the vertical menu
        menu_close.setVisible(false);
        menu_open.setVisible(true);
        slide_menu.setTranslateX(-100);

        // Setting properties for the scroll panes
        checkoutVbox = new VBox();
        checkoutScroll.setContent(checkoutVbox);
        checkoutScroll.setStyle("-fx-background-color:transparent;");
        menu_scroll.setStyle("-fx-background-color:transparent;");


        orderQuantList = new ArrayList<>();


        // Setting the order total
        orderTotalPrice = 0.0;
        orderTotal.setText("$0.00");
        totalWithTax.setText("$0.00");

        // connecting to the database
        conn = DatabaseConnectionManager.getConnection();

        // Variables to set from what was received from database
        String name = "";
        String category = "";
        int id = 0;
        double price = 0.0;

        // Array lists to keep track of elements in menu items
        ArrayList<String> categories = new ArrayList<String>();
        ArrayList<VBox> menu_categories = new ArrayList<VBox>();
        ArrayList<TilePane> menu_tilePanes = new ArrayList<TilePane>();
        VBox menu_layout = new VBox();
        try{
            //Asking for all the menu items from the database
            Statement stmt = conn.createStatement();
            String sqlStatement = "SELECT * FROM menu_item;";
            ResultSet result = stmt.executeQuery(sqlStatement);

            while (result.next()) {
                name = result.getString("name");
                category = result.getString("category");
                id = result.getInt("id");
                price = result.getDouble("price");

                // seeing if we already have the category that this menu item is a part of
                int index = categories.indexOf(category);

                // we do not have the category made, make a new one and add it to the list
                if (index == -1){
                    categories.add(category);

                    // New Vbox to hold the ti
                    VBox add_VBox = new VBox();
                    add_VBox.setPrefWidth(650);// prefWidth
                    add_VBox.setSpacing(10);

                    // New label displaying which category it is
                    Label add_Label = new Label(category);
                    add_Label.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 16));

                    // New tile pane to hold food items within
                    TilePane new_pane = new TilePane();
                    new_pane.setPrefColumns(4);
                    new_pane.setHgap(40);
                    new_pane.setVgap(20);

                    // adding the children to the container
                    add_VBox.getChildren().add(add_Label);
                    add_VBox.getChildren().add(new_pane);

                    // adding the container to the page
                    menu_categories.add(add_VBox);
                    menu_tilePanes.add(new_pane);
                    index = categories.indexOf(category);
                    menu_layout.getChildren().add(add_VBox);
                }

                // creating a new button with the menu item and adding it to the appropriate category
                String priceMenu = String.format("$%.2f", price);
                Button btn = new Button(name + "\n" + priceMenu);
                btn.setPrefHeight(120);
                btn.setPrefWidth(120);
                btn.wrapTextProperty().setValue(true);
                btn.setTextAlignment(TextAlignment.CENTER);
                btn.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 12));

                int addID = id;
                String finalName = name;
                double finalPrice = price;
                btn.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        addItemToOrder(addID, 1, finalName, finalPrice);
                    }
                });
                menu_tilePanes.get(index).getChildren().add(btn);
            }

            userProfile.setText("Profile: " + DatabaseOperations.currentEmployee.getName());

            // adding the children
            menu_scroll.setContent(menu_layout);
            menu_layout.setSpacing(70);
            menu_layout.setPadding(new Insets(60, 60, 60, 60));
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Error accessing Database.");
        }
    }
}