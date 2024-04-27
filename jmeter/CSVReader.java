import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CSVReader {

    public static void main(String[] args) {
        String csvFile = "example.csv"; // Replace "example.csv" with the path to your CSV file
        List<Map<String, String>> dataList = readCSV(csvFile);
        
        // Print the data
        for (Map<String, String> row : dataList) {
            System.out.println(row);
        }

        String inputString = "The value of column1 is {column1} and the value of column2 is {column2}.";
        String replacedString = replaceValues(inputString, dataList);
        System.out.println("Replaced String:");
        System.out.println(replacedString);
    }

    public static List<Map<String, String>> readCSV(String csvFile) {
        List<Map<String, String>> dataList = new ArrayList<>();
        String line;
        String[] headers = null;

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            // Read headers
            if ((line = br.readLine()) != null) {
                headers = line.split(",");
            }

            // Read data
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                Map<String, String> dataMap = new HashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    dataMap.put(headers[i], values[i]);
                }
                dataList.add(dataMap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return dataList;
    }

    public static String replaceValues(String inputString, List<Map<String, String>> dataList) {
        for (Map<String, String> row : dataList) {
            for (Map.Entry<String, String> entry : row.entrySet()) {
                inputString = inputString.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        return inputString;
    }
}
