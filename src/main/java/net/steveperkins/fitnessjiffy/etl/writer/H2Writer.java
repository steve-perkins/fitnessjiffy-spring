package net.steveperkins.fitnessjiffy.etl.writer;

import net.steveperkins.fitnessjiffy.etl.model.Datastore;
import net.steveperkins.fitnessjiffy.etl.model.Exercise;
import net.steveperkins.fitnessjiffy.etl.model.ExercisePerformed;
import net.steveperkins.fitnessjiffy.etl.model.Food;
import net.steveperkins.fitnessjiffy.etl.model.FoodEaten;
import net.steveperkins.fitnessjiffy.etl.model.User;
import net.steveperkins.fitnessjiffy.etl.model.Weight;
import net.steveperkins.fitnessjiffy.etl.reader.JDBCReader.TABLES;
import net.steveperkins.fitnessjiffy.etl.reader.JDBCReader.EXERCISE;
import net.steveperkins.fitnessjiffy.etl.reader.JDBCReader.USER;
import net.steveperkins.fitnessjiffy.etl.reader.JDBCReader.WEIGHT;
import net.steveperkins.fitnessjiffy.etl.reader.JDBCReader.FOOD_EATEN;
import net.steveperkins.fitnessjiffy.etl.reader.JDBCReader.EXERCISE_PERFORMED;
import net.steveperkins.fitnessjiffy.etl.reader.JDBCReader.FOOD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.UUID;

public class H2Writer extends JDBCWriter {

    public H2Writer(Connection connection, Datastore datastore) {
        super(connection, datastore);
    }

    @Override
    public void write() throws Exception {
        if(connection.isClosed()) throw new IllegalStateException();
        connection.setAutoCommit(false);

        writeSchema();
        writeExercises();
        writeGlobalFoods();
        writeUsers();

        connection.commit();
    }

    private void writeSchema() throws SQLException {
        String ddl = "CREATE USER IF NOT EXISTS \"\" SALT '' HASH '' ADMIN;\n" +
                "\n" +
                "DROP TABLE IF EXISTS PUBLIC.EXERCISE_PERFORMED;\n" +
                "DROP TABLE IF EXISTS PUBLIC.EXERCISE;\n" +
                "DROP TABLE IF EXISTS PUBLIC.FOOD_EATEN;\n" +
                "DROP TABLE IF EXISTS PUBLIC.FOOD;\n" +
                "DROP TABLE IF EXISTS PUBLIC.USER;\n" +
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
                "CREATE CACHED TABLE PUBLIC.USER(\n" +
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
                "ALTER TABLE PUBLIC.USER ADD CONSTRAINT PUBLIC.CONSTRAINT_27 PRIMARY KEY(ID);\n" +
                "\n" +
                "CREATE CACHED TABLE PUBLIC.WEIGHT(\n" +
                "    ID BINARY(16) NOT NULL,\n" +
                "    DATE TIMESTAMP NOT NULL,\n" +
                "    POUNDS DOUBLE NOT NULL,\n" +
                "    USER_ID BINARY(16) NOT NULL\n" +
                ");\n" +
                "ALTER TABLE PUBLIC.WEIGHT ADD CONSTRAINT PUBLIC.CONSTRAINT_9 PRIMARY KEY(ID);\n" +
                "\n" +
                "ALTER TABLE PUBLIC.FOOD ADD CONSTRAINT PUBLIC.FK_2DF49BA45A374495AB00B60AA42 FOREIGN KEY(OWNER_ID) REFERENCES PUBLIC.USER(ID) NOCHECK;\n" +
                "ALTER TABLE PUBLIC.EXERCISE_PERFORMED ADD CONSTRAINT PUBLIC.FK_34D3A2631F174DDAAD300D87C47 FOREIGN KEY(EXERCISE_ID) REFERENCES PUBLIC.EXERCISE(ID) NOCHECK;\n" +
                "ALTER TABLE PUBLIC.WEIGHT ADD CONSTRAINT PUBLIC.FK_D77EE9B9A8C84C48B1AAC56A402 FOREIGN KEY(USER_ID) REFERENCES PUBLIC.USER(ID) NOCHECK;\n" +
                "ALTER TABLE PUBLIC.FOOD_EATEN ADD CONSTRAINT PUBLIC.FK_F834F66989F94B85A36898261FC FOREIGN KEY(FOOD_ID) REFERENCES PUBLIC.FOOD(ID) NOCHECK;\n" +
                "ALTER TABLE PUBLIC.EXERCISE_PERFORMED ADD CONSTRAINT PUBLIC.FK_1BD968025714454ABACE3A6C5E2 FOREIGN KEY(USER_ID) REFERENCES PUBLIC.USER(ID) NOCHECK;\n" +
                "ALTER TABLE PUBLIC.FOOD_EATEN ADD CONSTRAINT PUBLIC.FK_B2EBA9C847514F7FB0D623FDB1B FOREIGN KEY(USER_ID) REFERENCES PUBLIC.USER(ID) NOCHECK;\n";
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(ddl);
        }

    }

    private void writeExercises() throws SQLException {
        for(Exercise exercise : datastore.getExercises()) {
            String sql = "INSERT INTO "+ TABLES.EXERCISE+" ("+ EXERCISE.ID+", "+EXERCISE.CATEGORY+", "
                    +EXERCISE.CODE+", "+EXERCISE.DESCRIPTION+", "+EXERCISE.METABOLIC_EQUIVALENT
                    +") VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setObject(1, exercise.getId(), Types.BINARY);
                statement.setString(2, exercise.getCategory());
                statement.setString(3, exercise.getCode());
                statement.setString(4, exercise.getDescription());
                statement.setDouble(5, exercise.getMetabolicEquivalent());
                statement.executeUpdate();
            }
        }
    }

    private void writeGlobalFoods() throws SQLException {
        for(Food food : datastore.getGlobalFoods()) {
            writeFood(food, null);
        }
    }

    private void writeUsers() throws Exception {
        for(User user : datastore.getUsers()) {
            String userSql = "INSERT INTO "+ TABLES.USER +" ("+USER.ID+", "+ USER.GENDER+", "+USER.AGE+", "+USER.HEIGHT_IN_INCHES
                    +", "+USER.ACTIVITY_LEVEL+", "+USER.USERNAME+", "+USER.PASSWORD+", "+USER.FIRST_NAME+", "
                    +USER.LAST_NAME+", "+USER.IS_ACTIVE+") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(userSql)) {
                statement.setObject(1, user.getId(), Types.BINARY);
                statement.setString(2, user.getGender().toString());
                statement.setInt(3, user.getAge());
                statement.setDouble(4, user.getHeightInInches());
                statement.setDouble(5, user.getActivityLevel().getValue());
                statement.setString(6, user.getUsername());
                statement.setString(7, user.getPassword());
                statement.setString(8, user.getFirstName());
                statement.setString(9, user.getLastName());
                statement.setBoolean(10, user.isActive());
                statement.executeUpdate();
            }

            for(Weight weight : user.getWeights()) {
                String sql = "INSERT INTO "+TABLES.WEIGHT+" ("+ WEIGHT.ID+", "+WEIGHT.USER_ID+", "+WEIGHT.DATE+", "
                        +WEIGHT.POUNDS+") VALUES (?, ?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setObject(1, weight.getId(), Types.BINARY);
                    statement.setObject(2, user.getId(), Types.BINARY);
                    statement.setDate(3, new java.sql.Date(weight.getDate().getTime()));
                    statement.setDouble(4, weight.getPounds());
                    statement.executeUpdate();
                }
            }

            for(Food food : user.getFoods()) {
                writeFood(food, user.getId());
            }

            for(FoodEaten foodEaten : user.getFoodsEaten()) {
                String sql = "INSERT INTO "+TABLES.FOOD_EATEN+" ("+FOOD_EATEN.ID+", "+FOOD_EATEN.USER_ID+", "
                        +FOOD_EATEN.FOOD_ID+", "+FOOD_EATEN.DATE+", "+FOOD_EATEN.SERVING_TYPE+", "
                        +FOOD_EATEN.SERVING_QTY+") VALUES (?, ?, ?, ?, ? ,?)";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setObject(1, foodEaten.getId(), Types.BINARY);
                    statement.setObject(2, user.getId(), Types.BINARY);
                    statement.setObject(3, foodEaten.getFoodId(), Types.BINARY);
                    statement.setDate(4, new java.sql.Date(foodEaten.getDate().getTime()));
                    statement.setString(5, foodEaten.getServingType().toString());
                    statement.setDouble(6, foodEaten.getServingQty());
                    statement.executeUpdate();
                }
            }

            for(ExercisePerformed exercisePerformed : user.getExercisesPerformed()) {
                String sql = "INSERT INTO "+TABLES.EXERCISE_PERFORMED+" ("+EXERCISE_PERFORMED.ID+", "
                        +EXERCISE_PERFORMED.USER_ID+", "+EXERCISE_PERFORMED.EXERCISE_ID+", "
                        +EXERCISE_PERFORMED.DATE+", "+EXERCISE_PERFORMED.MINUTES+") VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setObject(1, exercisePerformed.getId(), Types.BINARY);
                    statement.setObject(2, user.getId(), Types.BINARY);
                    statement.setObject(3, exercisePerformed.getExerciseId(), Types.BINARY);
                    statement.setDate(4, new java.sql.Date(exercisePerformed.getDate().getTime()));
                    statement.setInt(5, exercisePerformed.getMinutes());
                    statement.executeUpdate();
                }
            }
        }
    }

    private void writeFood(Food food, UUID ownerId) throws SQLException {
        String sql = "INSERT INTO "+TABLES.FOOD+" ("+FOOD.ID+", "+FOOD.NAME+", "+FOOD.DEFAULT_SERVING_TYPE+", "
                +FOOD.SERVING_TYPE_QTY+", "+FOOD.CALORIES+", "+FOOD.FAT+", "+FOOD.SATURATED_FAT+", "
                +FOOD.CARBS+", "+FOOD.FIBER+", "+FOOD.SUGAR+", "+FOOD.PROTEIN+", "+FOOD.SODIUM;
        sql += (ownerId != null)
                ? ", "+FOOD.USER_ID+") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                : ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try(PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, food.getId(), Types.BINARY);
            statement.setString(2, food.getName());
            statement.setString(3, food.getDefaultServingType().toString());
            statement.setFloat(4, food.getServingTypeQty().floatValue());
            statement.setInt(5, food.getCalories());
            statement.setFloat(6, food.getFat().floatValue());
            statement.setFloat(7, food.getSaturatedFat().floatValue());
            statement.setFloat(8, food.getCarbs().floatValue());
            statement.setFloat(9, food.getFiber().floatValue());
            statement.setFloat(10, food.getSugar().floatValue());
            statement.setFloat(11, food.getProtein().floatValue());
            statement.setFloat(12, food.getSodium().floatValue());
            if(ownerId != null) {
                statement.setObject(13, ownerId, Types.BINARY);
            }
            statement.executeUpdate();
        }
    }

}
