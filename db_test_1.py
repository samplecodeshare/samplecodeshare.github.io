import csv
import pyodbc
from locust import HttpUser, task, between

def read_sql_queries_from_csv(file_path):
    sql_queries = []
    with open(file_path, 'r') as csvfile:
        reader = csv.reader(csvfile)
        for row in reader:
            if len(row) > 0:
                sql_queries.append(row[0])  # Assuming the SQL queries are in the first column
    return sql_queries

class DatabaseUser(HttpUser):
    wait_time = between(1, 3)
    
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.connection = pyodbc.connect('DRIVER={SQL Server};SERVER=your_server;DATABASE=your_database;UID=your_username;PWD=your_password')
        self.sql_queries = read_sql_queries_from_csv('queries.csv')

    def on_start(self):
        pass

    @task
    def execute_query(self):
        for query in self.sql_queries:
            cursor = self.connection.cursor()
            cursor.execute(query)
            rows = cursor.fetchall()
            cursor.close()

    def on_stop(self):
        self.connection.close()
