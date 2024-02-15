import psycopg2

conn = psycopg2.connect(
    dbname='csce315_902_02_db',
    user='csce315_902_02_user',
    password='password123',
    host='csce-315-db.engr.tamu.edu',
    port='5432'
)
cursor = conn.cursor()

ingredients = [
    ('Bread', 100, 'Slices'),
    ('Lettuce', 50, 'Leaves'),
    ('Tomato', 75, 'Slices'),
    ('Cheese', 100, 'Slices'),
    ('Ham', 50, 'Slices'),
]

# parameter substitution with %s
for ingredient in ingredients:
    cursor.execute("""
        INSERT INTO Inventory (Name, Quantity, Unit) VALUES (%s, %s, %s)
    """, ingredient)

cursor.execute("""
    INSERT INTO Menu_Item (Name, Price, Calories) VALUES ('Classic Sandwich', 5.99, 350)
""")

# get the ID of Classic Sandwich
cursor.execute("SELECT ID FROM Menu_Item WHERE Name = %s", ('Classic Sandwich',))
menu_item_id = cursor.fetchone()[0]

# i am directly using the ingredient IDs from the Inventory table -- probably not a good idea
sandwich_ingredients = [
    (menu_item_id, 1, 2),
    (menu_item_id, 2, 1),
    (menu_item_id, 3, 2),
    (menu_item_id, 4, 1),
    (menu_item_id, 5, 2),
]

for item in sandwich_ingredients:
    cursor.execute("""
        INSERT INTO Recipe (Menu_item, Inventory_item, Qty) VALUES (%s, %s, %s)
    """, item)

conn.commit()

cursor.close()
conn.close()
