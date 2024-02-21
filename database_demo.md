This finds the day of most revenue from the customer_order table.
```postgresql
SELECT 
    DATE(Created_At) as OrderDate,
    SUM(mi.Price * oi.Quantity) as TotalRevenue
FROM 
    Customer_Order co
INNER JOIN 
    Order_Items oi ON co.ID = oi.Order_ID
INNER JOIN 
    Menu_Item mi ON oi.Menu_Item_ID = mi.ID
GROUP BY 
    OrderDate
ORDER BY 
    TotalRevenue DESC
LIMIT 1;

```