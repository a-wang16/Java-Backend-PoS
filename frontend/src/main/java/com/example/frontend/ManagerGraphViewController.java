package com.example.frontend;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import javafx.geometry.Pos;
import javafx.scene.control.DatePicker;

import java.time.LocalDate;
import java.util.Properties;
import java.util.ResourceBundle;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;

import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
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
    @FXML
    private Button productUsageButton;
    private Stage primaryStage;
    private Boolean employeeView;
    @FXML
    private DatePicker productEndDate;
    @FXML
    private DatePicker productStartDate;
    Connection conn;

    private Properties readProperties() {
        Properties prop = new Properties();
        try (InputStream input = StartApplication.class.getResourceAsStream("com/example/frontend/config.properties")) {
            prop.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return prop;
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

    public void showSalesTrend(ActionEvent event){
        graphContainer.getChildren().clear();
        ScrollPane graphScroll = new ScrollPane();
        graphScroll.setPrefWidth(750);
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
            graphScroll.setContent(linechart);
            graphContainer.getChildren().add(graphScroll);

        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Error accessing Database.");
        }



    }

    public void showProductUsage(ActionEvent event){
        graphContainer.getChildren().clear();
        ScrollPane graphScroll = new ScrollPane();
        graphContainer.getChildren().add(graphScroll);
        String name = "";
        int qty = 0;
        try{
            LocalDate startDate = productStartDate.getValue();
            String start = startDate.getMonthValue() + "/" + startDate.getDayOfMonth() + "/" + startDate.getYear();
            LocalDate endDate = productEndDate.getValue();
            String end = endDate.getMonthValue() + "/" + endDate.getDayOfMonth() + "/" + endDate.getYear();

            String sqlStatement = "SELECT i.Name, SUM(r.qty) AS inventory_used FROM Customer_Order co, Menu_Item mi JOIN Recipe r ON mi.ID = r.Menu_item JOIN Inventory i ON r.Inventory_item = i.ID WHERE Created_At >= '" + start +"' AND Created_At < '" + end +"' GROUP BY i.Name ORDER BY inventory_used DESC;";
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(sqlStatement);

            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

            while(result.next()) {
                name = result.getString("name");
                qty = result.getInt("inventory_used");
                pieChartData.add(new PieChart.Data(name, qty));
            }
            PieChart chart = new PieChart(pieChartData);
            chart.setTitle("Product Usage From: " + start + " - " + end);
            chart.setPrefSize(600, 400);
            graphScroll.setPadding(new Insets(20, 75, 20,75));
            graphScroll.setContent(chart);

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
        productUsageButton.setOnAction((this::showProductUsage));

        conn = DatabaseConnectionManager.getConnection();


//       try{
//
//        } catch (Exception e){
//            e.printStackTrace();
//            System.out.println("Error accessing Database.");
//        }
    }
    
}