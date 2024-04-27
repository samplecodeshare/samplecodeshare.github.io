import org.apache.poi.ss.usermodel.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelReader {

    public static void main(String[] args) {
        String excelFile = "example.xlsx"; // Replace "example.xlsx" with the path to your Excel file
        Map<String, List<String>> dataMap = readExcel(excelFile);
        
        // Print the data
        for (Map.Entry<String, List<String>> entry : dataMap.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    public static Map<String, List<String>> readExcel(String excelFile) {
        Map<String, List<String>> dataMap = new HashMap<>();

        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = WorkbookFactory.create(fis)) {

            // Assuming the data is in the first sheet
            Sheet sheet = workbook.getSheetAt(0);

            // Read headers
            Row headerRow = sheet.getRow(0);
            int columnCount = headerRow.getLastCellNum();
            for (int i = 0; i < columnCount; i++) {
                Cell headerCell = headerRow.getCell(i);
                String columnName = headerCell.getStringCellValue();
                dataMap.put(columnName, new ArrayList<>());
            }

            // Read data
            int rowCount = sheet.getLastRowNum();
            for (int i = 1; i <= rowCount; i++) {
                Row row = sheet.getRow(i);
                for (int j = 0; j < columnCount; j++) {
                    Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    String cellValue = formatCellValue(cell);
                    String columnName = headerRow.getCell(j).getStringCellValue();
                    dataMap.get(columnName).add(cellValue);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return dataMap;
    }

    private static String formatCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
}
