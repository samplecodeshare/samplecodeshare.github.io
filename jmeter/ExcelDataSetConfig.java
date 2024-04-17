import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.poi.ss.usermodel.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ExcelDataSetConfig extends ConfigTestElement implements TestBean, ThreadListener {
    private static final long serialVersionUID = 1L;

    private String filename;
    private String sheetName;
    private String[] columnNames;

    @Override
    public void threadStarted() {
        FileInputStream fis = null;
        Workbook workbook = null;
        try {
            fis = new FileInputStream(filename);
            workbook = WorkbookFactory.create(fis);
            Sheet sheet = workbook.getSheet(sheetName);
            Row headerRow = sheet.getRow(0);
            int colCount = headerRow.getLastCellNum();
            columnNames = new String[colCount];
            for (int i = 0; i < colCount; i++) {
                Cell cell = headerRow.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                columnNames[i] = cell.getStringCellValue();
            }
            // Assuming data starts from the second row
            int rowCount = sheet.getLastRowNum();
            for (int i = 1; i <= rowCount; i++) {
                Row dataRow = sheet.getRow(i);
                for (int j = 0; j < colCount; j++) {
                    Cell cell = dataRow.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    String value = cell.getStringCellValue();
                    // Set JMeter variables with column name as prefix
                    getThreadContext().getVariables().put(columnNames[j] + "_" + i, value);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void threadFinished() {
        // Clean up resources if necessary
    }

    // Getters and setters for properties
    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public String[] getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(String[] columnNames) {
        this.columnNames = columnNames;
    }
}
