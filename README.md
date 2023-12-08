# Mysql jdbc issue

It seems there was a breaking behaviour change in version `8.0.29` when using prepared 
statement. The behaviour when passing an Enum instance to a VARCHAR column has changed.

For such table:
```
CREATE TABLE demo_table (col1 VARCHAR(255) NOT NULL)
```
And such prepared statement:
```
INSERT INTO demo_table (col1) VALUES (?)
```

When doing
```
preparedStatement.setObject(1, MyEnum.VAL1, Types.VARCHAR);
```

In `8.0.28`: works fine, `MyEnum.VAL1` is converted to a String.  
In `8.0.29`: fails with `WrongArgumentException - Conversion from MyEnum to VARCHAR is not supported`  

The issue happens for SELECT statements too.

It may be related to this line in the [changelog of 8.0.29](https://dev.mysql.com/doc/relnotes/connector-j/8.0/en/news-8-0-29.html)
> The code for prepared statements has been refactored to make the code simpler and the logic for binding more consistent between ServerPreparedStatement and ClientPreparedStatement. (WL #14750)

The issue is still present in the most recent version `8.2.0`.

## To reproduce:
Requirements: docker (to spin-up mysql instances), java >=11.  
Pull this project. 

Run the script with the java connector in version `8.0.28`.
```
./mvnw clean compile -P=old-group -Dmysql-connector-j.version=8.0.28 exec:java
```
You will see successes in the logs:
```
Successfully inserted Enum object as VARCHAR. MySql version: 8.0.28. mysql-connector-java version: 8.0.28
Successfully inserted Enum object as VARCHAR. MySql version: 8.0.29. mysql-connector-java version: 8.0.28
```

Run the script with the java connector in version `8.0.29`
```
./mvnw clean compile -P=old-group -Dmysql-connector-j.version=8.0.29 exec:java
```

You will see failures in logs:
```
FAILED to insert Enum object as VARCHAR. MySql version: 8.0.28. mysql-connector-j version: 8.0.29
. Error: java.sql.SQLException: Cannot convert class Main$MyEnum to SQL type requested due to com.mysql.cj.exceptions.WrongArgumentException - Conversion from Main$MyEnum to VARCHAR is not supported.
FAILED to insert Enum object as VARCHAR. MySql version: 8.0.29. mysql-connector-j version: 8.0.29
. Error: java.sql.SQLException: Cannot convert class Main$MyEnum to SQL type requested due to com.mysql.cj.exceptions.WrongArgumentException - Conversion from Main$MyEnum to VARCHAR is not supported.
```
The problem is still present in the most recent GA version 8.1.0 and in 8.2.0:
```
./mvnw clean compile -P=new-group -Dmysql-connector-j.version=8.1.0 exec:java
```
Logs:
```
FAILED to insert Enum object as VARCHAR. MySql version: 8.0.28. mysql-connector-j version: 8.1.0
. Error: java.sql.SQLException: Cannot convert class Main$MyEnum to SQL type requested due to com.mysql.cj.exceptions.WrongArgumentException - Conversion from Main$MyEnum to VARCHAR is not supported.
FAILED to insert Enum object as VARCHAR. MySql version: 8.0.29. mysql-connector-j version: 8.1.0
. Error: java.sql.SQLException: Cannot convert class Main$MyEnum to SQL type requested due to com.mysql.cj.exceptions.WrongArgumentException - Conversion from Main$MyEnum to VARCHAR is not supported.
```
(same for 8.2.0)
