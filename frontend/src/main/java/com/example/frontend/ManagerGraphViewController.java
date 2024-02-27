package com.example.frontend;

import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.ResourceBundle;
//
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ManagerGraphViewController implements Initializable{

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

    private Stage primaryStage;

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
        Button continueButton = new Button("Continue to Payment");
        continueButton.setOnAction(e -> {
            // Handle the continue action
            dialogStage.close();
        });

        dialogVBox.getChildren().addAll(nameLabel, nameField, continueButton);

        // Set the scene and show the stage
        Scene dialogScene = new Scene(dialogVBox, 300, 200);
        dialogStage.setScene(dialogScene);
        dialogStage.showAndWait();
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        // initializing the vertical menu
        menu_close.setVisible(false);
        menu_open.setVisible(true);
        slide_menu.setTranslateX(-100);

        // connecting to the database
        Connection conn = null;
        String database_name = "csce315_902_02_db";
        String database_user = "csce315_902_02_user";
        String database_password = "password123";
        String database_url = String.format("jdbc:postgresql://csce-315-db.engr.tamu.edu/%s", database_name);
        try {
            conn = DriverManager.getConnection(database_url, database_user, database_password);
            System.out.println("Successfully connected to database.");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
            System.out.println("Could not connect to database.");
        }


        String name = "";
        String category = "";
        ArrayList<String> categories = new ArrayList<String>();
        ArrayList<VBox> menu_categories = new ArrayList<VBox>();
        ArrayList<TilePane> menu_tilePanes = new ArrayList<TilePane>();
        VBox menu_layout = new VBox();
        try{

        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Error accessing Database.");
        }

        try {
            conn.close();
            System.out.println("Database closed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in closing database.");
        }
    }
}