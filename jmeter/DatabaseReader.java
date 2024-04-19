import org.apache.jmeter.threads.JMeterVariables;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseReader {
    private static DatabaseReader instance;
    private List<String[]> data;
    private int currentIndex;

    private DatabaseReader(String jdbcUrl, String username, String password, String sql) {
        data = new ArrayList<>();
        currentIndex = 0;
        loadFromDatabase(jdbcUrl, username, password, sql);
    }

    public static synchronized DatabaseReader getInstance(String jdbcUrl, String username, String password, String sql) {
        if (instance == null) {
            instance = new DatabaseReader(jdbcUrl, username, password, sql);
        }
        return instance;
    }

    private void loadFromDatabase(String jdbcUrl, String username, String password, String sql) {
        try {
            // Establish the connection
            Connection connection = DriverManager.getConnection(jdbcUrl, username, password);

            // Create a statement
            Statement statement = connection.createStatement();

            // Execute the query
            ResultSet resultSet = statement.executeQuery(sql);

            // Iterate through the result set and load data into memory
            while (resultSet.next()) {
                String[] row = new String[resultSet.getMetaData().getColumnCount()];
                for (int i = 0; i < row.length; i++) {
                    row[i] = resultSet.getString(i + 1);
                }
                data.add(row);
            }

            // Close resources
            resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void getNext(JMeterVariables vars) {
        if (currentIndex < data.size()) {
            String[] rowData = data.get(currentIndex++);
            for (int i = 0; i < rowData.length; i++) {
                vars.put("param" + (i + 1), rowData[i]);
            }
        }
    }
}
