import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.kie.api.runtime.KieBase;
import org.kie.api.runtime.KieSession;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SparkRulesBroadcast implements Serializable {

    private static final String DRL_FILE_PATH = "path/to/your/rules.drl";

    private transient SparkSession spark;
    private transient KieBase kieBase;

    public SparkRulesBroadcast(SparkSession spark, KieBase kieBase) {
        this.spark = spark;
        this.kieBase = kieBase;
    }

    public void runSparkJob(JavaRDD<Row> data) {
        // Broadcast the KieBase
        final Broadcast<KieBase> broadcastKieBase = spark.sparkContext().broadcast(this.kieBase);

        // Perform Spark transformations using the broadcasted KieBase
        JavaRDD<ResultClass> result = data.mapPartitions(iterator -> {
            // Access the broadcasted KieBase
            KieBase kieBase = broadcastKieBase.value();

            // Create a new stateful KieSession for each partition
            KieSession kieSession = kieBase.newKieSession();

            // Process data using the KieSession
            Iterator<ResultClass> resultsIterator = processRows(iterator, kieSession);

            // Dispose of the KieSession
            kieSession.dispose();

            return resultsIterator;
        });

        // Perform actions or save the result as needed
        result.collect().forEach(System.out::println);
    }

    private Iterator<ResultClass> processRows(Iterator<Row> rowIterator, KieSession kieSession) {
        // Create a list to store the results
        List<ResultClass> results = new ArrayList<>();

        // Iterate over rows and apply rules
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            // Extract data from the Row (replace with your actual extraction logic)
            String dataField = row.getString(0); // Adjust the index based on your data structure

            // Insert data into the KieSession
            YourDataClass dataObject = new YourDataClass(dataField); // Replace with your actual data class
            kieSession.insert(dataObject);

            // Fire rules
            kieSession.fireAllRules();

            // Extract results from the KieSession (replace with your actual extraction logic)
            ResultClass result = new ResultClass(); // Replace with your actual result class
            results.add(result);
        }

        return results.iterator();
    }

    public static void main(String[] args) {
        // Initialize SparkSession
        SparkSession spark = SparkSession.builder()
                .appName("SparkRulesBroadcast")
                .master("local[*]")
                .getOrCreate();

        // Create and initialize the KieBase from DRL file
        KieBase kieBase = createAndInitializeKieBase(spark);

        // Create the SparkRulesBroadcast object
        SparkRulesBroadcast sparkRulesBroadcast = new SparkRulesBroadcast(spark, kieBase);

        // Load your data as a JavaRDD<Row> (replace with your data loading logic)
        JavaRDD<Row> data = loadDataAsRDD(spark);

        // Run the Spark job using the broadcasted KieBase
        sparkRulesBroadcast.runSparkJob(data);

        // Stop SparkSession
        spark.stop();
    }

    private static KieBase createAndInitializeKieBase(SparkSession spark) {
        // Initialize KieServices
        KieServicesImpl kieServices = (KieServicesImpl) KieServices.Factory.get();

        // Load DRL file into KieModule
        KieFileSystemImpl kieFileSystem = (KieFileSystemImpl) kieServices.newKieFileSystem();
        kieFileSystem.write("src/main/resources/rules.drl", kieServices.getResources().newFileSystemResource(DRL_FILE_PATH));
        KieBuilderImpl kieBuilder = (KieBuilderImpl) kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll(KieBuilderImpl.class); // Build KieModule
        KieModule kieModule = kieBuilder.getKieModule();

        // Create KieBase from KieModule
        KieRepositoryImpl kieRepository = (KieRepositoryImpl) kieServices.getRepository();
        InternalKieModule internalKieModule = (InternalKieModule) kieModule;
        KieProject kieProject = new KieModuleKieProject(internalKieModule, kieRepository);
        return kieServices.newKieContainer(kieProject).getKieBase();
    }

    private static JavaRDD<Row> loadDataAsRDD(SparkSession spark) {
        // Implement your logic to load data as a JavaRDD<Row>
        // For example, you can load a DataFrame and convert it to RDD
        Dataset<Row> dataFrame = spark.read().parquet("path/to/your/parquet/file");
        return dataFrame.javaRDD();
    }
}
