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

csv_file_path = './csv/menu_item.csv'

with open(csv_file_path, newline='') as csvfile:
    reader = csv.DictReader(csvfile)
    for row in reader:
        cursor.execute("""
            INSERT INTO Menu_Item (ID, Name, Price, Calories) 
            VALUES (%s, %s, %s, %s)
            ON CONFLICT (ID) DO UPDATE 
            SET Name = EXCLUDED.Name, 
                Price = EXCLUDED.Price, 
                Calories = EXCLUDED.Calories;
        """, (row['ID'], row['Name'], row['Price'], row['Calories']))

conn.commit()
cursor.close()
conn.close()

print("CSV data has been successfully imported into the Menu_Item table.")
