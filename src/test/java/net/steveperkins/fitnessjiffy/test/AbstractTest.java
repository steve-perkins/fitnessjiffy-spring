package net.steveperkins.fitnessjiffy.test;

import net.steveperkins.fitnessjiffy.Application;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.text.SimpleDateFormat;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {Application.class})
public abstract class AbstractTest {

    @Autowired
    DataSource dataSource;

    final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Before
    public void before() throws Exception {
        final Connection connection = dataSource.getConnection();
        final Statement statement = connection.createStatement();
        statement.execute("DROP ALL OBJECTS");
        statement.execute("RUNSCRIPT FROM 'classpath:/testdata.sql'");
    }


}
