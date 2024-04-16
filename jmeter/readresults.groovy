import java.sql.ResultSet

// Get the result set from the previous sampler
ResultSet resultSet = prev.getResultSet()

// Check if the result set is not null
if (resultSet != null) {
    // Iterate through the result set
    while (resultSet.next()) {
        // Extract values from the result set
        String value1 = resultSet.getString("column_name_1")
        String value2 = resultSet.getString("column_name_2")
        // Store extracted values in JMeter variables
        vars.put("value1", value1)
        vars.put("value2", value2)
        // Print extracted values to the JMeter log
        log.info("Value 1: " + value1)
        log.info("Value 2: " + value2)
    }
}
