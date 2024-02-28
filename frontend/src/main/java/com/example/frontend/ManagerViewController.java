package com.example.frontend;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
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

public class ManagerViewController implements Initializable{

    @FXML
    private ImageView menu_close;

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
        saveButton.setOnAction(e -> handleSaveAction(inventoryFieldName.getText(), inventoryFieldQuantity.getText(), inventoryFieldUnit.getText(), modalStage));

        modalVBox.getChildren().addAll(instructionLabel, inventoryFieldName, inventoryFieldQuantity, inventoryFieldUnit, saveButton);

        // Show the modal window
        Scene modalScene = new Scene(modalVBox, 300, 200);
        modalStage.setScene(modalScene);
        modalStage.showAndWait();
    }

    private void handleSaveAction(String name, String quantity, String unit, Stage modalStage) {
        // Handle the save action (e.g., print inventory details)
        System.out.println("Updating Inventory Item:");
        System.out.println("Name: " + name);
        System.out.println("Quantity: " + quantity);
        System.out.println("Unit: " + unit);

        // Establishing a connection to the database
        Connection conn = DatabaseConnectionManager.getConnection();

        try {
            // SQL statement to update inventory item
            String sqlStatement = "UPDATE inventory SET quantity = ?, unit = ? WHERE name = ?;";

            // Preparing the SQL statement
            PreparedStatement pstmt = conn.prepareStatement(sqlStatement);

            // Setting the parameters for the SQL statement
            pstmt.setInt(1, Integer.parseInt(quantity));       
            pstmt.setString(2, unit);
            pstmt.setString(3, name);

            // Executing the SQL update
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Inventory updated successfully.");
            } else {
                System.out.println("Inventory update failed. No item found with the specified name.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error accessing Database.");
        } finally {
            // Close the connection if necessary
            try {
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
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
    }

}