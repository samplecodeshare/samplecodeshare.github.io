from locust import HttpUser, task, between
import pyodbc

class DatabaseUser(HttpUser):
    wait_time = between(1, 3)

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.connection = pyodbc.connect('DRIVER={SQL Server};SERVER=your_server;DATABASE=your_database;UID=your_username;PWD=your_password')

    def on_start(self):
        pass

    @task
    def execute_query(self):
        cursor = self.connection.cursor()
        cursor.execute("SELECT * FROM your_table")
        rows = cursor.fetchall()
        cursor.close()

    def on_stop(self):
        self.connection.close()
