package net.steveperkins.fitnessjiffy.config;

import javax.sql.DataSource;

import net.steveperkins.fitnessjiffy.dao.UserDao;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

@Configuration
@ComponentScan("net.steveperkins.fitnessjiffy")
@EnableWebMvc
public class WebConfig {
	
	private Log log = LogFactory.getLog(WebConfig.class);

	@Bean
	public ViewResolver viewResolver() {
		InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
		viewResolver.setViewClass(JstlView.class);
		viewResolver.setPrefix("/WEB-INF/view/");
		viewResolver.setSuffix(".jsp");
		return viewResolver;
	}
	
	@Bean
	public DataSource dataSource() {
		log.info("Inside dataSource()");
		BasicDataSource dataSource = new BasicDataSource();
		try {
			dataSource.setDriverClassName("org.sqlite.JDBC");
			dataSource.setUrl("jdbc:sqlite:sqlite.db");
		} catch(Exception e) {
			log.error(e);
		}
		return dataSource;
	}
	
	@Bean
	public UserDao userDao() {
		UserDao userDao = new UserDao();
		userDao.setDataSource(dataSource());
		return userDao;
	}
	
}
