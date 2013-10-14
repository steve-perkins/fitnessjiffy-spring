package net.steveperkins.fitnessjiffy.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import net.steveperkins.fitnessjiffy.domain.User;
import net.steveperkins.fitnessjiffy.domain.User.ActivityLevel;
import net.steveperkins.fitnessjiffy.domain.User.Gender;


import net.steveperkins.fitnessjiffy.domain.User.YesNo;




//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

@Repository
public class UserDao {

	private static final String COLUMN_ID = "ID"; 
	private static final String COLUMN_ACTIVITY_LEVEL = "ACTIVITY_LEVEL"; 
	private static final String COLUMN_AGE = "AGE"; 
	private static final String COLUMN_FIRST_NAME = "FIRST_NAME"; 
	private static final String COLUMN_GENDER = "GENDER"; 
	private static final String COLUMN_HEIGHT_IN_INCHES = "HEIGHT_IN_INCHES"; 
	private static final String COLUMN_LAST_NAME = "LAST_NAME"; 
	private static final String COLUMN_PASSWORD = "PASSWORD"; 
	private static final String COLUMN_USERNAME = "USERNAME"; 
	private static final String COLUMN_ACTIVE = "ACTIVE"; 
	
	//private Log log = LogFactory.getLog(UserDao.class);

	private JdbcTemplate jdbcTemplate;
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	
	@Autowired
	private WeightDao weightDao;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }
    
    public User findById(int id) {
    	String sql = String.format("select * from users where %s = 'Y' and %s = :%s", COLUMN_ACTIVE, COLUMN_ID, COLUMN_ID);
    	SqlParameterSource namedParameters = new MapSqlParameterSource(COLUMN_ID, id);
    	return this.namedParameterJdbcTemplate.queryForObject(sql, namedParameters, new RowMapper<User>() {
			public User mapRow(ResultSet rs, int rowNum) throws SQLException {
				User user = new User();
				user.setId(rs.getInt(COLUMN_ID));
				user.setActivityLevel(ActivityLevel.fromValue(rs.getFloat(COLUMN_ACTIVITY_LEVEL)));
				user.setAge(rs.getInt(COLUMN_AGE));
				user.setFirstName(rs.getString(COLUMN_FIRST_NAME));
				user.setGender(Gender.fromString(rs.getString(COLUMN_GENDER)));
				user.setHeightInInches(rs.getFloat(COLUMN_HEIGHT_IN_INCHES));
				user.setLastName(rs.getString(COLUMN_LAST_NAME));
				user.setPassword(rs.getString(COLUMN_PASSWORD));
				user.setUsername(rs.getString(COLUMN_USERNAME));
				user.setActive(YesNo.fromValue(rs.getString(COLUMN_ACTIVE).charAt(0)));
				user.setWeights(weightDao.findAllForUser(user.getId()));
				return user;
			}
		});
    }
    
    public List<User> findAll() {
    	String sql = String.format("select * from users where %s = 'Y'", COLUMN_ACTIVE);
    	List<Map<String, Object>> userMaps = this.jdbcTemplate.queryForList(sql);
    	List<User> users = new ArrayList<User>();
    	for(Map<String, Object> userMap : userMaps) {
			User user = new User();
			user.setId((Integer)userMap.get(COLUMN_ID));
			user.setActivityLevel(ActivityLevel.fromValue(((Double)userMap.get(COLUMN_ACTIVITY_LEVEL)).floatValue()));
			user.setAge((Integer)userMap.get(COLUMN_AGE));
			user.setFirstName((String)userMap.get(COLUMN_FIRST_NAME));
			user.setGender(Gender.fromString((String)userMap.get(COLUMN_GENDER)));
			user.setHeightInInches(((Double)userMap.get(COLUMN_HEIGHT_IN_INCHES)).floatValue());
			user.setLastName((String)userMap.get(COLUMN_LAST_NAME));
			user.setPassword((String)userMap.get(COLUMN_PASSWORD));
			user.setUsername((String)userMap.get(COLUMN_USERNAME));
			user.setActive(YesNo.fromValue(((String)userMap.get(COLUMN_ACTIVE)).charAt(0)));
			user.setWeights(weightDao.findAllForUser(user.getId()));
			users.add(user);
    	}
    	return users;
    }
    
    public User save(User user) {
    	String sql = "";
    	if(user.getId() == 0 || findById(user.getId()) == null) {
    		// Insert
    		int maxId = this.jdbcTemplate.queryForObject(String.format("select max(%s) from users", COLUMN_ID), Integer.class).intValue();
    		user.setId(maxId + 1);
    		sql = String.format("insert into users(%s, %s, %s, %s, %s, %s, %s, %s, $s, %s) values (:%s, :%s, :%s, :%s, :%s, :%s, :%s, :%s, :%s, 'Y')", 
    				COLUMN_ID, COLUMN_GENDER, COLUMN_AGE, COLUMN_HEIGHT_IN_INCHES, COLUMN_ACTIVITY_LEVEL, COLUMN_USERNAME, COLUMN_PASSWORD, COLUMN_FIRST_NAME, COLUMN_LAST_NAME, COLUMN_ACTIVE,
    				COLUMN_ID, COLUMN_GENDER, COLUMN_AGE, COLUMN_HEIGHT_IN_INCHES, COLUMN_ACTIVITY_LEVEL, COLUMN_USERNAME, COLUMN_PASSWORD, COLUMN_FIRST_NAME, COLUMN_LAST_NAME);
    	} else {
    		// Update
    		sql = String.format("update users set %s = :%s, %s = :%s, %s = :%s, %s = :%s, %s = :%s, %s = :%s, %s = :%s, %s = :%s, %s = 'Y' where %s = :%s",
    				COLUMN_GENDER, COLUMN_GENDER, COLUMN_AGE, COLUMN_AGE, COLUMN_HEIGHT_IN_INCHES, COLUMN_HEIGHT_IN_INCHES, COLUMN_ACTIVITY_LEVEL, COLUMN_ACTIVITY_LEVEL, 
    				COLUMN_USERNAME, COLUMN_USERNAME, COLUMN_PASSWORD, COLUMN_PASSWORD, COLUMN_FIRST_NAME, COLUMN_FIRST_NAME, COLUMN_LAST_NAME, COLUMN_LAST_NAME, COLUMN_ACTIVE, COLUMN_ID, COLUMN_ID);
    	}
		Map<String, Object> namedParameters = new HashMap<String, Object>();
		namedParameters.put(COLUMN_ID, user.getId());
		namedParameters.put(COLUMN_GENDER, user.getGender().toString());
		namedParameters.put(COLUMN_AGE, user.getAge());
		namedParameters.put(COLUMN_HEIGHT_IN_INCHES, user.getHeightInInches());
		namedParameters.put(COLUMN_ACTIVITY_LEVEL, user.getActivityLevel().getValue());
		namedParameters.put(COLUMN_USERNAME, user.getUsername());
		// TODO: Implement a real change password function
		namedParameters.put(COLUMN_PASSWORD, (user.getPassword() != null && !user.getPassword().trim().isEmpty()) ? user.getPassword() : "password");
		namedParameters.put(COLUMN_FIRST_NAME, user.getFirstName());
		namedParameters.put(COLUMN_LAST_NAME, user.getLastName());
		this.namedParameterJdbcTemplate.update(sql, namedParameters);
		return user;
    }
    
    public void delete(User user) {
    	String sql = String.format("update users set %s = :%s, %s = 'N' where %s = :%s", COLUMN_USERNAME, COLUMN_USERNAME, COLUMN_ACTIVE, COLUMN_ID, COLUMN_ID);
		Map<String, Object> namedParameters = new HashMap<String, Object>();
		namedParameters.put(COLUMN_ID, user.getId());
		namedParameters.put(COLUMN_USERNAME, user.getUsername() + "_deactivated_" + (new Date().toString()));
		this.namedParameterJdbcTemplate.update(sql, namedParameters);
    }
    
}
