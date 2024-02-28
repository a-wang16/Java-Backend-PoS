package com.example.frontend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseOperations {
    public static void createOrderAndUpdateInventory(int employeeId, List<OrderItem> orderItems, String orderCustomerName) {
        Connection conn = DatabaseConnectionManager.getConnection();
        try {
            conn.setAutoCommit(false);
            System.out.println("Successfully connected to the database.");
            System.out.println("Starting transaction for creating order and updating inventory.");

            // check if inventory is sufficient for all order items based on the recipe
            Map<Integer, Integer> totalRequiredInventory = new HashMap<>();
            for (OrderItem item : orderItems) {
                String recipeRequirementSQL =
                        "SELECT Inventory_item, SUM(Qty * ?) as TotalRequired " +
                                "FROM Recipe WHERE Menu_item = ? GROUP BY Inventory_item";
                PreparedStatement recipeRequirementStmt = conn.prepareStatement(recipeRequirementSQL);
                recipeRequirementStmt.setInt(1, item.getQuantity());
                recipeRequirementStmt.setInt(2, item.getMenuItemId());
                ResultSet recipeRs = recipeRequirementStmt.executeQuery();

                while (recipeRs.next()) {
                    int inventoryItemId = recipeRs.getInt("Inventory_item");
                    int totalRequired = recipeRs.getInt("TotalRequired");
                    totalRequiredInventory.put(inventoryItemId, totalRequiredInventory.getOrDefault(inventoryItemId, 0) + totalRequired);
                }
            }

            boolean inventorySufficient = true;
            for (Map.Entry<Integer, Integer> entry : totalRequiredInventory.entrySet()) {
                int inventoryItemId = entry.getKey();
                int requiredQuantity = entry.getValue();

                String checkInventorySQL = "SELECT Quantity FROM Inventory WHERE ID = ?";
                PreparedStatement checkInventoryStmt = conn.prepareStatement(checkInventorySQL);
                checkInventoryStmt.setInt(1, inventoryItemId);
                ResultSet inventoryRs = checkInventoryStmt.executeQuery();

                if (inventoryRs.next()) {
                    int availableQuantity = inventoryRs.getInt("Quantity");
                    if (requiredQuantity > availableQuantity) {
                        System.out.println("Insufficient inventory for Inventory Item ID: " + inventoryItemId);
                        inventorySufficient = false;
                        break;
                    }
                }
            }

            if (!inventorySufficient) {
                conn.rollback();
                System.out.println("Not enough inventory!");
                return;
            }

            String insertOrderSQL = "INSERT INTO Customer_Order (Employee_ID, Created_At, Status, Name) VALUES (?, ?, 'NEW', ?) RETURNING ID";
            PreparedStatement insertOrderStmt = conn.prepareStatement(insertOrderSQL);
            insertOrderStmt.setInt(1, employeeId);
            insertOrderStmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            insertOrderStmt.setString(3, orderCustomerName);
            ResultSet orderRs = insertOrderStmt.executeQuery();
            orderRs.next();
            int orderId = orderRs.getInt(1);
            System.out.println("Order created with ID: " + orderId);

            for (OrderItem item : orderItems) {
                String insertItemSQL = "INSERT INTO Order_Items (Order_ID, Menu_Item_ID, Quantity) VALUES (?, ?, ?)";
                PreparedStatement insertItemStmt = conn.prepareStatement(insertItemSQL);
                insertItemStmt.setInt(1, orderId);
                insertItemStmt.setInt(2, item.getMenuItemId());
                insertItemStmt.setInt(3, item.getQuantity());
                insertItemStmt.executeUpdate();
                System.out.println("Order item added. Menu Item ID: " + item.getMenuItemId() + ", Quantity: " + item.getQuantity());

                String updateInventorySQL = "UPDATE Inventory SET Quantity = GREATEST(0, Quantity - ?) WHERE ID IN (SELECT Inventory_item FROM Recipe WHERE Menu_item = ?)";
                PreparedStatement updateInventoryStmt = conn.prepareStatement(updateInventorySQL);
                updateInventoryStmt.setInt(1, item.getQuantity());
                updateInventoryStmt.setInt(2, item.getMenuItemId());
                updateInventoryStmt.executeUpdate();
                System.out.println("Inventory updated for Menu Item ID: " + item.getMenuItemId());
            }

            conn.commit();
            System.out.println("Order created and inventory updated.");

        } catch (SQLException e) {
            System.out.println("Exception during order creation and inventory update: " + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.out.println("Exception during rollback: " + ex.getMessage());
            }
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }


    public static class OrderItem {
        private final int menuItemId;
        private final int quantity;
        private final String name;
        private final Double price;


        public OrderItem(int menuItemId, int quantity, String name, Double price) {
            this.menuItemId = menuItemId;
            this.quantity = quantity;
            this.name = name;
            this.price = price;
        }

        public int getMenuItemId() {
            return menuItemId;
        }

        public int getQuantity() {
            return quantity;
        }

        public String getName() { return name; }

        public Double getPrice() {return price; }
    }

}
