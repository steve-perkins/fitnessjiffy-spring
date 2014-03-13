package net.steveperkins.fitnessjiffy.etl.writer;

import net.steveperkins.fitnessjiffy.etl.model.Datastore;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class H2Writer extends JDBCWriter {

    public H2Writer(Connection connection, Datastore datastore) {
        super(connection, datastore);
    }

    @Override
    protected void writeSchema() throws SQLException {
        String ddl = "CREATE USER IF NOT EXISTS \"\" SALT '' HASH '' ADMIN;\n" +
                "\n" +
                "DROP TABLE IF EXISTS PUBLIC.EXERCISE_PERFORMED;\n" +
                "DROP TABLE IF EXISTS PUBLIC.EXERCISE;\n" +
                "DROP TABLE IF EXISTS PUBLIC.FOOD_EATEN;\n" +
                "DROP TABLE IF EXISTS PUBLIC.FOOD;\n" +
                "DROP TABLE IF EXISTS PUBLIC.FITNESSJIFFY_USER;\n" +
                "DROP TABLE IF EXISTS PUBLIC.WEIGHT;\n" +
                "\n" +
                "CREATE CACHED TABLE PUBLIC.EXERCISE(\n" +
                "    ID BINARY(16) NOT NULL,\n" +
                "    CATEGORY VARCHAR(25) NOT NULL,\n" +
                "    CODE VARCHAR(5) NOT NULL,\n" +
                "    DESCRIPTION VARCHAR(250) NOT NULL,\n" +
                "    METABOLIC_EQUIVALENT DOUBLE NOT NULL\n" +
                ");\n" +
                "ALTER TABLE PUBLIC.EXERCISE ADD CONSTRAINT PUBLIC.CONSTRAINT_A PRIMARY KEY(ID);\n" +
                "\n" +
                "CREATE CACHED TABLE PUBLIC.EXERCISE_PERFORMED(\n" +
                "    ID BINARY(16) NOT NULL,\n" +
                "    DATE TIMESTAMP NOT NULL,\n" +
                "    MINUTES INTEGER NOT NULL,\n" +
                "    EXERCISE_ID BINARY NOT NULL,\n" +
                "    USER_ID BINARY(16) NOT NULL\n" +
                ");\n" +
                "ALTER TABLE PUBLIC.EXERCISE_PERFORMED ADD CONSTRAINT PUBLIC.CONSTRAINT_7 PRIMARY KEY(ID);\n" +
                "\n" +
                "CREATE CACHED TABLE PUBLIC.FOOD(\n" +
                "    ID BINARY(16) NOT NULL,\n" +
                "    CALORIES INTEGER NOT NULL,\n" +
                "    CARBS DOUBLE NOT NULL,\n" +
                "    DEFAULT_SERVING_TYPE VARCHAR(10) NOT NULL,\n" +
                "    FAT DOUBLE NOT NULL,\n" +
                "    FIBER DOUBLE NOT NULL,\n" +
                "    NAME VARCHAR(50) NOT NULL,\n" +
                "    PROTEIN DOUBLE NOT NULL,\n" +
                "    SATURATED_FAT DOUBLE NOT NULL,\n" +
                "    SERVING_TYPE_QTY DOUBLE NOT NULL,\n" +
                "    SODIUM DOUBLE NOT NULL,\n" +
                "    SUGAR DOUBLE NOT NULL,\n" +
                "    OWNER_ID BINARY(16)\n" +
                ");\n" +
                "ALTER TABLE PUBLIC.FOOD ADD CONSTRAINT PUBLIC.CONSTRAINT_2 PRIMARY KEY(ID);\n" +
                "\n" +
                "CREATE CACHED TABLE PUBLIC.FOOD_EATEN(\n" +
                "    ID BINARY(16) NOT NULL,\n" +
                "    DATE TIMESTAMP NOT NULL,\n" +
                "    SERVING_QTY DOUBLE NOT NULL,\n" +
                "    SERVING_TYPE VARCHAR(10) NOT NULL,\n" +
                "    FOOD_ID BINARY(16) NOT NULL,\n" +
                "    USER_ID BINARY(16) NOT NULL\n" +
                ");\n" +
                "ALTER TABLE PUBLIC.FOOD_EATEN ADD CONSTRAINT PUBLIC.CONSTRAINT_4 PRIMARY KEY(ID);\n" +
                "\n" +
                "CREATE CACHED TABLE PUBLIC.FITNESSJIFFY_USER(\n" +
                "    ID BINARY(16) NOT NULL,\n" +
                "    ACTIVITY_LEVEL VARCHAR(17) NOT NULL,\n" +
                "    AGE INTEGER NOT NULL,\n" +
                "    FIRST_NAME VARCHAR(20) NOT NULL,\n" +
                "    GENDER VARCHAR(6) NOT NULL,\n" +
                "    HEIGHT_IN_INCHES DOUBLE NOT NULL,\n" +
                "    IS_ACTIVE BOOLEAN NOT NULL,\n" +
                "    LAST_NAME VARCHAR(20) NOT NULL,\n" +
                "    PASSWORD VARCHAR(50) NOT NULL,\n" +
                "    USERNAME VARCHAR(50) NOT NULL\n" +
                ");\n" +
                "ALTER TABLE PUBLIC.FITNESSJIFFY_USER ADD CONSTRAINT PUBLIC.CONSTRAINT_27 PRIMARY KEY(ID);\n" +
                "\n" +
                "CREATE CACHED TABLE PUBLIC.WEIGHT(\n" +
                "    ID BINARY(16) NOT NULL,\n" +
                "    DATE TIMESTAMP NOT NULL,\n" +
                "    POUNDS DOUBLE NOT NULL,\n" +
                "    USER_ID BINARY(16) NOT NULL\n" +
                ");\n" +
                "ALTER TABLE PUBLIC.WEIGHT ADD CONSTRAINT PUBLIC.CONSTRAINT_9 PRIMARY KEY(ID);\n" +
                "\n" +
                "ALTER TABLE PUBLIC.FOOD ADD CONSTRAINT PUBLIC.FK_2DF49BA45A374495AB00B60AA42 FOREIGN KEY(OWNER_ID) REFERENCES PUBLIC.FITNESSJIFFY_USER(ID) NOCHECK;\n" +
                "ALTER TABLE PUBLIC.EXERCISE_PERFORMED ADD CONSTRAINT PUBLIC.FK_34D3A2631F174DDAAD300D87C47 FOREIGN KEY(EXERCISE_ID) REFERENCES PUBLIC.EXERCISE(ID) NOCHECK;\n" +
                "ALTER TABLE PUBLIC.WEIGHT ADD CONSTRAINT PUBLIC.FK_D77EE9B9A8C84C48B1AAC56A402 FOREIGN KEY(USER_ID) REFERENCES PUBLIC.FITNESSJIFFY_USER(ID) NOCHECK;\n" +
                "ALTER TABLE PUBLIC.FOOD_EATEN ADD CONSTRAINT PUBLIC.FK_F834F66989F94B85A36898261FC FOREIGN KEY(FOOD_ID) REFERENCES PUBLIC.FOOD(ID) NOCHECK;\n" +
                "ALTER TABLE PUBLIC.EXERCISE_PERFORMED ADD CONSTRAINT PUBLIC.FK_1BD968025714454ABACE3A6C5E2 FOREIGN KEY(USER_ID) REFERENCES PUBLIC.FITNESSJIFFY_USER(ID) NOCHECK;\n" +
                "ALTER TABLE PUBLIC.FOOD_EATEN ADD CONSTRAINT PUBLIC.FK_B2EBA9C847514F7FB0D623FDB1B FOREIGN KEY(USER_ID) REFERENCES PUBLIC.FITNESSJIFFY_USER(ID) NOCHECK;\n";
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(ddl);
        }
    }

}
