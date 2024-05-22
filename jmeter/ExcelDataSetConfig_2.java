import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.poi.ss.usermodel.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExcelDataSetConfig extends ConfigTestElement implements TestBean, LoopIterationListener {

    private static final Logger logger = LogManager.getLogger(ExcelDataSetConfig.class);

    private String filename;
    private transient Workbook workbook;
    private transient Sheet sheet;
    private transient Iterator<Row> rowIterator;
    private transient String[] columnNames;
    private static final AtomicInteger currentRow = new AtomicInteger(0);
    private static final Object lock = new Object();

    public void setFilename(String filename) throws IOException {
        this.filename = filename;
        FileInputStream fis = new FileInputStream(filename);
        workbook = WorkbookFactory.create(fis);
        sheet = workbook.getSheetAt(0); // Assuming the first sheet is used
        rowIterator = sheet.iterator();

        // Read column names from the first row
        Row firstRow = rowIterator.next();
        columnNames = new String[firstRow.getLastCellNum()];
        for (int i = 0; i < firstRow.getLastCellNum(); i++) {
            Cell cell = firstRow.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            columnNames[i] = cell.toString();
        }
    }

    @Override
    public void iterationStart(LoopIterationEvent event) {
        getNextRow();
    }

    public String getNextRow() {
        JMeterVariables vars = JMeterContextService.getContext().getVariables();
        synchronized (lock) {
            if (currentRow.get() < sheet.getLastRowNum()) {
                Row row = sheet.getRow(currentRow.getAndIncrement());
                for (int i = 0; i < row.getLastCellNum(); i++) {
                    Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    vars.put(columnNames[i], cell.toString());
                }
                // Log the row data
                logger.info("Row data: {}", vars);
                // Return the row data as a comma-separated string if needed
                StringBuilder rowData = new StringBuilder();
                for (int i = 0; i < row.getLastCellNum(); i++) {
                    rowData.append(vars.get(columnNames[i])).append(",");
                }
                return rowData.toString();
            } else {
                currentRow.set(1); // Reset to the first row after reaching the end, assuming row 0 is header
                return getNextRow();
            }
        }
    }
}
