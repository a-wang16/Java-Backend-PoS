package com.example.frontend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class DatabaseOperations {
    public void createOrderAndUpdateInventory(int employeeId, List<OrderItem> orderItems, String orderCustomerName) {
        Connection conn = DatabaseConnectionManager.getConnection();
        try {
            conn.setAutoCommit(false);

            String insertOrderSQL = "INSERT INTO Customer_Order (Employee_ID, Created_At, Status, Name) VALUES (?, ?, 'NEW', ?) RETURNING ID";
            PreparedStatement insertOrderStmt = conn.prepareStatement(insertOrderSQL);
            insertOrderStmt.setInt(1, employeeId);
            insertOrderStmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            insertOrderStmt.setString(3, orderCustomerName);
            ResultSet orderRs = insertOrderStmt.executeQuery();
            orderRs.next();
            int orderId = orderRs.getInt(1);

            // Insert order items and update inventory
            for (OrderItem item : orderItems) {
                String insertItemSQL = "INSERT INTO Order_Items (Order_ID, Menu_Item_ID, Quantity) VALUES (?, ?, ?)";
                PreparedStatement insertItemStmt = conn.prepareStatement(insertItemSQL);
                insertItemStmt.setInt(1, orderId);
                insertItemStmt.setInt(2, item.getMenuItemId());
                insertItemStmt.setInt(3, item.getQuantity());
                insertItemStmt.executeUpdate();

                // Update Inventory based on Recipe requirements
                String updateInventorySQL = "UPDATE Inventory SET Quantity = Quantity - ? WHERE ID IN (SELECT Inventory_item FROM Recipe WHERE Menu_item = ?)";
                PreparedStatement updateInventoryStmt = conn.prepareStatement(updateInventorySQL);
                updateInventoryStmt.setInt(1, item.getQuantity());
                updateInventoryStmt.setInt(2, item.getMenuItemId());
                updateInventoryStmt.executeUpdate();
            }

            conn.commit();
            System.out.println("Order created and inventory updated successfully.");
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static class OrderItem {
        private final int menuItemId;
        private final int quantity;

        public OrderItem(int menuItemId, int quantity) {
            this.menuItemId = menuItemId;
            this.quantity = quantity;
        }

        public int getMenuItemId() {
            return menuItemId;
        }

        public int getQuantity() {
            return quantity;
        }
    }

}
