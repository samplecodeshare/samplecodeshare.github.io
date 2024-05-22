import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SharedExcelConfigReader extends ConfigTestElement {
    private static final ConcurrentHashMap<String, AtomicInteger> fileReaders = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Sheet> excelSheets = new ConcurrentHashMap<>();

    private String filePath;
    private String variableName;

    public SharedExcelConfigReader(String filePath, String variableName) {
        this.filePath = filePath;
        this.variableName = variableName;
    }

    public void execute() throws IOException, InvalidFormatException {
        JMeterVariables variables = JMeterContextService.getContext().getVariables();
        AtomicInteger currentRow = fileReaders.computeIfAbsent(filePath, k -> new AtomicInteger(0));
        Sheet sheet = excelSheets.computeIfAbsent(filePath, k -> {
            try {
                Workbook workbook = WorkbookFactory.create(new File(filePath));
                return workbook.getSheetAt(0);
            } catch (IOException | InvalidFormatException e) {
                throw new RuntimeException(e);
            }
        });

        synchronized (sheet) {
            Row row = sheet.getRow(currentRow.getAndIncrement());
            if (row != null) {
                String data = row.getCell(0).getStringCellValue();
                variables.put(variableName, data);
            }
        }
    }
}
