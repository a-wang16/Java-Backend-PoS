import psycopg2
import csv

conn_params = {
    'dbname': 'csce315_902_02_db',
    'user': 'csce315_902_02_user',
    'password': 'password123',
    'host': 'csce-315-db.engr.tamu.edu',
    'port': '5432'
}

conn = psycopg2.connect(**conn_params)
cursor = conn.cursor()

csv_file_path = './csv/recipe.csv'

# Open the CSV file and insert each row into the Recipe table
with open(csv_file_path, newline='') as csvfile:
    reader = csv.DictReader(csvfile)
    for row in reader:
        cursor.execute("""
            INSERT INTO Recipe (ID, Menu_item, Inventory_item, Qty) 
            VALUES (%s, %s, %s, %s)
            ON CONFLICT (ID) DO UPDATE 
            SET Menu_item = EXCLUDED.Menu_item, 
                Inventory_item = EXCLUDED.Inventory_item, 
                Qty = EXCLUDED.Qty;
        """, (row['ID'], row['Menu_Item_ID'], row['Inventory_ID'], row['QTY']))

conn.commit()
cursor.close()
conn.close()

print("CSV data has been successfully imported into the Recipe table.")
