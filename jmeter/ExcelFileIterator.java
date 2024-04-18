import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.*;

public class ExcelFileIterator implements Iterator<String[]> {
    private Workbook workbook;
    private Sheet sheet;
    private Iterator<Row> rowIterator;
    private int currentRow;

    public ExcelFileIterator(String filePath) throws IOException {
        FileInputStream fis = new FileInputStream(filePath);
        workbook = WorkbookFactory.create(fis);
        sheet = workbook.getSheetAt(0); // Assuming the first sheet is used
        rowIterator = sheet.iterator();
        currentRow = -1;
    }

    @Override
    public boolean hasNext() {
        return true; // Always return true to iterate infinitely
    }

    @Override
    public String[] next() {
        if (!rowIterator.hasNext()) {
            // If end of sheet is reached, go back to the first row
            rowIterator = sheet.iterator();
            currentRow = -1;
        }

        Row row = rowIterator.next();
        currentRow++;
        // Read data from the current row and convert it to a String array
        String[] rowData = new String[row.getLastCellNum()];
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            rowData[i] = cell.toString();
        }
        return rowData;
    }

    public void close() {
        try {
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
