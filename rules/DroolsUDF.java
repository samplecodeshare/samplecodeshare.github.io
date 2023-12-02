import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.Row;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

public class DroolsUDF implements UDF1<Row, Row> {
    @Override
    public Row call(Row row) throws Exception {
        // Instantiate a Drools KieSession
        KieServices kieServices = KieServices.Factory.get();
        KieContainer kieContainer = kieServices.getKieClasspathContainer();
        KieBase kieBase = kieContainer.newKieBaseFromKieModule(kieContainer.getKieModuleForKBase("yourKBase"),
                kieServices.getKieBaseConfiguration());
        KieSession kieSession = kieBase.newKieSession();

        // Insert the data from the row into the KieSession as facts
        // For example, if your data is a Person class, insert it into the KieSession
        Person person = new Person(row.getString(0), row.getInt(1));
        kieSession.insert(person);

        // Fire the rules
        kieSession.fireAllRules();

        // Dispose of the KieSession
        kieSession.dispose();

        // Return the modified row
        return row;
    }
}
