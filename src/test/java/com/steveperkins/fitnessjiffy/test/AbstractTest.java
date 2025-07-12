package com.steveperkins.fitnessjiffy.test;

import com.steveperkins.fitnessjiffy.Application;
import com.steveperkins.fitnessjiffy.service.ReportDataService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {Application.class})
public abstract class AbstractTest {

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    DataSource dataSource;

    @Autowired
    ReportDataService reportDataService;

    final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @BeforeEach
    public void before() throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DROP ALL OBJECTS");
            statement.execute("RUNSCRIPT FROM 'classpath:/backup.sql'");
        }
    }

    @AfterEach
    public void after() throws InterruptedException {
        while (!reportDataService.isIdle()) {
            Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        }
    }

}
