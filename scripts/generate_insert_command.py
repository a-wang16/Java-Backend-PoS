def generate_insert_command(order_id, customer_name, order_date, item_id, quantity, total_price):
    customer_name = customer_name.replace("'", "''")

    sql_command = f"INSERT INTO orders (order_id, customer_name, order_date, item_id, quantity, total_price) VALUES ({order_id}, '{customer_name}', '{order_date}', {item_id}, {quantity}, {total_price});"
    
    return sql_command

order_command = generate_insert_command(1, "John Doe", "2023-02-15", 101, 2, 19.99)
print(order_command)
