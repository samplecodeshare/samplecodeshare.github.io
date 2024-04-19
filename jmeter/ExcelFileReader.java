import org.apache.jmeter.threads.JMeterVariables;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

public class ExcelDataReader {
    private static ExcelDataReader instance;
    private Workbook workbook;
    private Sheet sheet;
    private Iterator<Row> rowIterator;
    private String[] columnNames;
    private boolean loopFromStart;
    private String currentFileName;

    private ExcelDataReader() {
        loopFromStart = false;
    }

    public static synchronized ExcelDataReader getInstance(String filename) throws IOException {
        if (instance == null || !instance.currentFileName.equals(filename)) {
            instance = new ExcelDataReader();
            instance.openFile(filename);
            instance.currentFileName = filename;
        }
        return instance;
    }

    public void openFile(String filePath) throws IOException {
        FileInputStream fis = new FileInputStream(filePath);
        workbook = new XSSFWorkbook(fis);
        sheet = workbook.getSheetAt(0); // Assuming the first sheet is used
        rowIterator = sheet.iterator();

        // Read column names from the first row
        Row firstRow = sheet.getRow(0);
        columnNames = new String[firstRow.getLastCellNum()];
        for (int i = 0; i < firstRow.getLastCellNum(); i++) {
            Cell cell = firstRow.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            columnNames[i] = cell.toString();
        }

        // Skip the header row
        rowIterator.next();
    }

    public void setLoopFromStart(boolean loopFromStart) {
        this.loopFromStart = loopFromStart;
    }

    public String[] getNextRow() {
        if (!rowIterator.hasNext()) {
            if (loopFromStart) {
                rowIterator = sheet.iterator();
                // Skip the header row
                rowIterator.next();
            } else {
                return null; // End of file and not looping from start
            }
        }

        Row row = rowIterator.next();
        String[] rowData = new String[row.getLastCellNum()];
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            rowData[i] = cell.toString();
        }

        // Set JMeter variables for the current row data
        JMeterVariables vars = new JMeterVariables();
        for (int i = 0; i < columnNames.length; i++) {
            vars.put(columnNames[i], rowData[i]);
        }

        return rowData;
    }

    public void closeFile() throws IOException {
        workbook.close();
    }
}
