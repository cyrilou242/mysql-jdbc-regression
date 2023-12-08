import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import org.testcontainers.containers.MySQLContainer;

public class Main {

  // https://java.testcontainers.org/modules/databases/mysql/#mysql-root-user-password
  //jdbc:tc:mysql:5.7.34:///databasename
  private final static List<String> MYSQL_VERSIONS = List.of("8.0.28", "8.0.29");

  public static void main(String[] args) throws ClassNotFoundException, SQLException {
    final String mysqlConnectorVersion = Class.forName("com.mysql.cj.jdbc.Driver")
        .getPackage()
        .getImplementationVersion();

    for (final String mysqlVersion : MYSQL_VERSIONS) {
      try (final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:" + mysqlVersion)) {
        mysql.start();

        try (Connection c = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(),
            mysql.getPassword())) {
          // create table
          c.createStatement().execute("CREATE TABLE demo_table (col1 VARCHAR(255) NOT NULL)");

          // FIXME ISSUE IS HERE insert an enum instance as VARCHAR
          final PreparedStatement preparedStatement = c.prepareStatement(
              "INSERT INTO demo_table (col1) VALUES (?)");
          try {
            preparedStatement.setObject(1, MyEnum.VAL1, Types.VARCHAR);
            preparedStatement.execute();
            System.out.printf(
                "Successfully inserted Enum object as VARCHAR. MySql version: %s. mysql-connector-j version: %s%n",
                mysqlVersion, mysqlConnectorVersion);
          } catch (SQLException e) {
            System.out.printf(
                "FAILED to insert Enum object as VARCHAR. MySql version: %s. mysql-connector-j version: %s%n. Error: %s%n",
                mysqlVersion, mysqlConnectorVersion, e);
          }
        }
      }
    }
    // avoid some bugs at teardown in some maven version
    System.exit(0);
  }

  public enum MyEnum {
    VAL1
  }
}
