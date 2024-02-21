from faker import Faker
import psycopg2
from datetime import datetime, timedelta
import random

fake = Faker()

conn_params = {
    'dbname': 'csce315_902_02_db',
    'user': 'csce315_902_02_user',
    'password': 'password123',
    'host': 'csce-315-db.engr.tamu.edu',
    'port': '5432'
}

conn = psycopg2.connect(**conn_params)
cursor = conn.cursor()

cursor.execute("SELECT ID FROM Menu_Item;")
menu_items = cursor.fetchall()

cursor.execute("SELECT ID FROM Employee;")
employees = cursor.fetchall()

end_date = datetime.now()
start_date = end_date - timedelta(days=365)

for single_date in (start_date + timedelta(n) for n in range((end_date - start_date).days)):
    num_orders = random.randint(350, 550)
    for _ in range(num_orders):
        customer_name = fake.name()

        employee_id = random.choice(employees)[0]

        order_timestamp = datetime.combine(single_date, datetime.min.time()) + timedelta(hours=random.randint(8, 17),
                                                                                         minutes=random.randint(0, 59))

        cursor.execute("""
            INSERT INTO Customer_Order (Employee_ID, Created_At, Status, Name) 
            VALUES (%s, %s, %s, %s)
            RETURNING ID;
        """, (employee_id, order_timestamp, 'Completed', customer_name))

        order_id = cursor.fetchone()[0]

        num_items = random.randint(1, 6)
        for _ in range(num_items):
            menu_item_id = random.choice(menu_items)[0]

            quantity = random.randint(1, 3)

            cursor.execute("""
                INSERT INTO Order_Items (Order_ID, Menu_Item_ID, Quantity) 
                VALUES (%s, %s, %s);
            """, (order_id, menu_item_id, quantity))

    conn.commit()

cursor.close()
conn.close()

print("Fake orders have been generated and inserted into the database.")
