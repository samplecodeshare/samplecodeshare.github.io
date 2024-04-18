try {
    ExcelFileIterator iterator = new ExcelFileIterator("path/to/your/excel/file.xlsx");
    while (iterator.hasNext()) {
        String[] rowData = iterator.next();
        // Process rowData
        for (String cellValue : rowData) {
            System.out.print(cellValue + "\t");
        }
        System.out.println();
    }
    iterator.close();
} catch (IOException e) {
    e.printStackTrace();
}
