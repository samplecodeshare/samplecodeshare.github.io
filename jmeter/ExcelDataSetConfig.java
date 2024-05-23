import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.poi.ss.usermodel.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExcelDataSetConfig extends ConfigTestElement implements TestBean, LoopIterationListener {

    private static final Logger logger = LogManager.getLogger(ExcelDataSetConfig.class);

    private String filename;
    private transient Workbook workbook;
    private transient Sheet sheet;
    private transient String[] columnNames;
    private static final AtomicInteger globalRowCounter = new AtomicInteger(1);
    private static ThreadLocal<Integer> threadLocalRowCounter = ThreadLocal.withInitial(() -> 0);

    @Override
    public void iterationStart(LoopIterationEvent event) {
        try {
            setNextRow();
        } catch (IOException e) {
            logger.error("Error reading next row from Excel file", e);
        }
    }

    public void setFilename(String filename) throws IOException {
        this.filename = filename;
        initialize();
    }

    private void initialize() throws IOException {
        FileInputStream fis = new FileInputStream(filename);
        workbook = WorkbookFactory.create(fis);
        sheet = workbook.getSheetAt(0); // Assuming the first sheet is used

        // Read column names from the first row
        Row firstRow = sheet.getRow(0);
        columnNames = new String[firstRow.getLastCellNum()];
        for (int i = 0; i < firstRow.getLastCellNum(); i++) {
            Cell cell = firstRow.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            columnNames[i] = cell.toString();
        }
    }

    public void setNextRow() throws IOException {
        if (sheet == null) {
            initialize();
        }

        JMeterVariables vars = JMeterContextService.getContext().getVariables();
        int rowIndex = threadLocalRowCounter.get();

        synchronized (globalRowCounter) {
            if (rowIndex == 0 || rowIndex > sheet.getLastRowNum()) {
                rowIndex = globalRowCounter.getAndIncrement();
                threadLocalRowCounter.set(rowIndex);
            }
        }

        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            return;
        }

        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            vars.put(columnNames[i], cell.toString());
        }

        // Log the row data
        logger.info("Row data for thread {}: {}", Thread.currentThread().getName(), vars);

        // Optionally return row data as comma-separated string for debugging
        StringBuilder rowData = new StringBuilder();
        for (int i = 0; i < row.getLastCellNum(); i++) {
            rowData.append(vars.get(columnNames[i])).append(",");
        }
    }

    public String getFilename() {
        return filename;
    }
}
