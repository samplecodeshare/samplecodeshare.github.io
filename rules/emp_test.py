from pyspark.sql import SparkSession
from py4j.java_gateway import JavaGateway

class Employee:
    def __init__(self, name, salary):
        self.name = name
        self.salary = salary
        self.result = None

    def get_name(self):
        return self.name

    def set_name(self, name):
        self.name = name

    def get_salary(self):
        return self.salary

    def set_salary(self, salary):
        self.salary = salary

    def get_result(self):
        return self.result

    def set_result(self, result):
        self.result = result

def apply_drools_rule(employee, ksession):
    # Insert Employee facts into the Drools session
    drools_employee = gateway.jvm.your.package.Employee()  # Replace with your actual Employee class
    drools_employee.set_name(employee.get_name())
    drools_employee.set_salary(employee.get_salary())

    ksession.insert(drools_employee)

    # Fire rules
    ksession.fireAllRules()

    # Update the result in the Employee object
    employee.set_result(drools_employee.get_result())

    return employee

if __name__ == "__main__":
    # Initialize Spark session
    spark = SparkSession.builder.appName("DroolsExample").getOrCreate()

    # Connect to the Drools rule engine using py4j
    gateway = JavaGateway()

    # Provide rules in memory
    rules = """
    package your.package

    import your.package.Employee;

    rule "LowSalaryRule"
    when
        $employee: Employee(salary < 60000)
    then
        $employee.setResult("Low");
    end
    """

    # Create a Drools session with decision table
    drools = gateway.jvm.org.drools.builder.impl.KnowledgeBuilderFactory.newKnowledgeBuilder().newKnowledgeBase()
    kbuilder = gateway.jvm.org.drools.builder.impl.KnowledgeBuilderFactory.newKnowledgeBuilder()
    kbuilder.add(gateway.jvm.org.drools.io.ResourceFactory.newByteArrayResource(rules), gateway.jvm.org.drools.io.ResourceType.DRL)
    
    if kbuilder.hasErrors():
        print("Drools rule compilation errors:")
        for error in kbuilder.getErrors():
            print(error)
        exit(1)

    kbase = kbuilder.newKnowledgeBase()
    ksession = kbase.newStatefulKnowledgeSession()

    # Create an in-memory DataFrame with sample data
    data = [("John", 50000.0), ("Alice", 75000.0), ("Bob", 60000.0)]
    schema = ["name", "salary"]
    data_df = spark.createDataFrame(data, schema)

    # Apply Drools rule to each row
    employee_rdd = data_df.rdd.map(lambda row: Employee(row["name"], float(row["salary"])))
    result_rdd = employee_rdd.map(lambda emp: apply_drools_rule(emp, ksession))

    # Convert the RDD back to DataFrame
    result_df = spark.createDataFrame(result_rdd, ["name", "salary", "result"])

    # Show the result DataFrame
    result_df.show()

    # Close the session
    ksession.dispose()

    # Stop the Spark session
    spark.stop()
