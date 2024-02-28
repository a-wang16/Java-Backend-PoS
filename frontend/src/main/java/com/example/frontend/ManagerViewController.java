package com.example.frontend;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.ResourceBundle;
//
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.*;
import javafx.scene.control.Label;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import com.example.frontend.DatabaseConnectionManager;

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
    private Button updateInventoryButton;

    @FXML
    private Button updateMenuButton;

    @FXML
    private Button switchSceneBtn;
    @FXML
    private Boolean managerView;
    
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
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource(name));
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
            Stage stage = (Stage) switchBtn.getScene().getWindow();

            String name = "";
            if (employeeView){
                name = "manager-view.fxml";
                employeeView = false;
            }
            else{
                name = "gemma.fxml";
                employeeView = true;
            }
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource(name));
            Parent root = loader.load();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @FXML
    void handleUpdateInventoryButton(ActionEvent event) {
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL); // Block interaction with other windows
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

        modalVBox.getChildren().addAll(instructionLabel, inventoryFieldName, inventoryFieldQuantity, inventoryFieldUnit, saveButton);

        // Show the modal window
        Scene modalScene = new Scene(modalVBox, 300, 200);
        modalStage.setScene(modalScene);
        modalStage.showAndWait();
    }

    @FXML
    void handleUpdateMenuButton(ActionEvent event) {
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL); // Block interaction with other windows
        modalStage.setTitle("Update Menu");

        // Define the modal content
        VBox modalVBox = new VBox(10);
        modalVBox.setPadding(new Insets(10));
        Label instructionLabel = new Label("Enter Menu Details:");
        TextField name = new TextField();
        TextField price = new TextField();
        TextField calories = new TextField();
        TextField category = new TextField();

        name.setPromptText("Enter Item Name");
        price.setPromptText("Enter Item Price");
        calories.setPromptText("Enter Item Calories");
        category.setPromptText("Enter Item Category");

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> handleMenuSaveAction(name.getText(), price.getText(), calories.getText(), category.getText(), modalStage));

        modalVBox.getChildren().addAll(instructionLabel, name, price, calories, category, saveButton);

        // Show the modal window
        Scene modalScene = new Scene(modalVBox, 300, 300);
        modalStage.setScene(modalScene);
        modalStage.showAndWait();
    }

    private void handleInventorySaveAction(String name, String quantity, String unit, Stage modalStage) {
        System.out.println("Updating Inventory Item:");
        System.out.println("Name: " + name);
        System.out.println("Quantity: " + quantity);
        System.out.println("Unit: " + unit);
    
        Connection conn = DatabaseConnectionManager.getConnection();
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
    }
    

    private void handleMenuSaveAction(String name, String price, String calories, String category, Stage modalStage) {
        System.out.println("Updating Menu Item:");
        System.out.println("Name: " + name);
        System.out.println("Price: " + price);
        System.out.println("Calories: " + calories);
        System.out.println("Category: " + category);
    
        Connection conn = DatabaseConnectionManager.getConnection();
    
        try {
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
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error accessing Database.");
        } catch (NumberFormatException e) {
            System.out.println("Error in number format: " + e.getMessage());
        } finally {
            modalStage.close();
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


    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        menu_close.setVisible(false);
        menu_open.setVisible(true);
        slide_menu.setTranslateX(-100);
        employeeView = false;
        updateInventoryButton.setOnAction(this::handleUpdateInventoryButton);
        updateMenuButton.setOnAction(this::handleUpdateMenuButton);



        // Variables to set from what was received from database
        String name = "";
        String quant = "";

        Connection conn = DatabaseConnectionManager.getConnection();
        // Array lists to keep track of elements in menu items
        VBox itemsVertical = new VBox(20);
        try {
            String sqlStatement = "SELECT * FROM inventory;";
            Statement stmt = conn.createStatement();

            ResultSet result = stmt.executeQuery(sqlStatement);

            while (result.next()) {
                name = result.getString("name");
                quant = result.getString("quantity");

                HBox toAdd = new HBox(20);
                Label nameLabel = new Label(name);
                Label quantLabel = new Label(quant);
                toAdd.getChildren().addAll(nameLabel, quantLabel);
                itemsVertical.getChildren().add(toAdd);
            }
//            testPane.getChildren().add(itemsVertical);
            managerScroll.setContent(itemsVertical);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error accessing Database.");
        }

        managerView = true;
    }


}