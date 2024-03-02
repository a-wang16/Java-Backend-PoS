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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ManagerGraphViewController implements Initializable{
    @FXML
    private Pane graphButtonContainer;
    @FXML
    private Pane graphContainer;
    @FXML
    private ImageView menu_close;
    @FXML
    private ImageView menu_open;
    @FXML
    private Button salesTrendButton;
    @FXML
    private AnchorPane slide_menu;
    @FXML
    private ImageView switchBtn;
    private Stage primaryStage;
    private Boolean employeeView;
    Connection conn;

    private Properties readProperties() {
        Properties prop = new Properties();
        try (InputStream input = StartApplication.class.getResourceAsStream("config.properties")) {
            prop.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return prop;
    }

    @FXML
    void switchButton(MouseEvent event) {
        try {
            Stage stage = (Stage) switchBtn.getScene().getWindow();

            String name = "";
            if (employeeView){
                System.out.println("Switching to employee");
                name = "employee-entry-view.fxml";
                employeeView = false;
            }
            else{
                System.out.println("Switching to manager");
                name = "manager-view.fxml";
                employeeView = true;
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

    public void showSalesTrend(ActionEvent event){
        try{
            String sqlStatement = "SELECT EXTRACT(WEEK FROM co.Created_At) AS OrderWeek, SUM(oi.Quantity * mi.Price) AS TotalRevenue FROM Order_Items oi JOIN Menu_Item mi ON oi.Menu_Item_ID = mi.ID JOIN Customer_Order co ON oi.Order_ID = co.ID GROUP BY OrderWeek ORDER BY OrderWeek;";
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(sqlStatement);


            XYChart.Series series = new XYChart.Series();
            int week = 0;
            Double revenue = 0.0;

            Double maxRevenue = 0.0;

            while(result.next()){
                week = result.getInt("orderweek");
                revenue = result.getDouble("totalrevenue");

                if (revenue > maxRevenue){
                    maxRevenue = revenue;
                }

                series.getData().add(new XYChart.Data(week, revenue));
            }
            NumberAxis xAxis = new NumberAxis(0, 52, 1);
            NumberAxis yAxis = new NumberAxis(0, (maxRevenue * 1.10), ((maxRevenue * 1.10) / 50));
            LineChart linechart = new LineChart(xAxis, yAxis);
            linechart.getData().add(series);
            linechart.setPrefWidth(900);
            graphContainer.getChildren().add(linechart);


        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Error accessing Database.");
        }



    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        menu_close.setVisible(false);
        menu_open.setVisible(true);
        slide_menu.setTranslateX(-100);

        salesTrendButton.setOnAction(this::showSalesTrend);

        conn = DatabaseConnectionManager.getConnection();

//       try{
//
//        } catch (Exception e){
//            e.printStackTrace();
//            System.out.println("Error accessing Database.");
//        }
    }
    
}