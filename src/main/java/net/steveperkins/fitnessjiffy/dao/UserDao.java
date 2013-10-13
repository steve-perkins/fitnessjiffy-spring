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
    	String sql = "select * from users where active = 'Y' and id = :id";
    	SqlParameterSource namedParameters = new MapSqlParameterSource("id", id);
    	return this.namedParameterJdbcTemplate.queryForObject(sql, namedParameters, new RowMapper<User>() {
			public User mapRow(ResultSet rs, int rowNum) throws SQLException {
				User user = new User();
				user.setId(rs.getInt("ID"));
				user.setActivityLevel(ActivityLevel.fromValue(rs.getFloat("ACTIVITY_LEVEL")));
				user.setAge(rs.getInt("AGE"));
				user.setFirstName(rs.getString("FIRST_NAME"));
				user.setGender(Gender.fromString(rs.getString("GENDER")));
				user.setHeightInInches(rs.getFloat("HEIGHT_IN_INCHES"));
				user.setLastName(rs.getString("LAST_NAME"));
				user.setPassword(rs.getString("PASSWORD"));
				user.setUsername(rs.getString("USERNAME"));
				user.setActive(YesNo.fromValue(rs.getString("ACTIVE").charAt(0)));
				user.setWeights(weightDao.findAllForUser(user.getId()));
				return user;
			}
		});
    }
    
    public List<User> findAll() {
    	String sql = "select * from users where active = 'Y'";
    	List<Map<String, Object>> userMaps = this.jdbcTemplate.queryForList(sql);
    	List<User> users = new ArrayList<User>();
    	for(Map<String, Object> userMap : userMaps) {
			User user = new User();
			user.setId((Integer)userMap.get("ID"));
			user.setActivityLevel(ActivityLevel.fromValue(((Double)userMap.get("ACTIVITY_LEVEL")).floatValue()));
			user.setAge((Integer)userMap.get("AGE"));
			user.setFirstName((String)userMap.get("FIRST_NAME"));
			user.setGender(Gender.fromString((String)userMap.get("GENDER")));
			user.setHeightInInches(((Double)userMap.get("HEIGHT_IN_INCHES")).floatValue());
			user.setLastName((String)userMap.get("LAST_NAME"));
			user.setPassword((String)userMap.get("PASSWORD"));
			user.setUsername((String)userMap.get("USERNAME"));
			user.setActive(YesNo.fromValue(((String)userMap.get("ACTIVE")).charAt(0)));
			user.setWeights(weightDao.findAllForUser(user.getId()));
			users.add(user);
    	}
    	return users;
    }
    
    public User save(User user) {
    	String sql = "";
    	if(user.getId() == 0 || findById(user.getId()) == null) {
    		// Insert
    		int maxId = this.jdbcTemplate.queryForObject("select max(id) from users", Integer.class).intValue();
    		user.setId(maxId + 1);
    		sql = "insert into users(id, gender, age, height_in_inches, activity_level, username, password, first_name, last_name, active) values (:id, :gender, :age, :height_in_inches, :activity_level, :username, :password, :first_name, :last_name, 'Y')";
    	} else {
    		// Update
    		sql = "update users set gender = :gender, age = :age, height_in_inches = :height_in_inches, activity_level = :activity_level, username = :username, password = :password, first_name = :first_name, last_name = :last_name, active = 'Y' where id = :id";
    	}
		Map<String, Object> namedParameters = new HashMap<String, Object>();
		namedParameters.put("id", user.getId());
		namedParameters.put("gender", user.getGender().toString());
		namedParameters.put("age", user.getAge());
		namedParameters.put("height_in_inches", user.getHeightInInches());
		namedParameters.put("activity_level", user.getActivityLevel().getValue());
		namedParameters.put("username", user.getUsername());
		// TODO: Implement a real change password function
		namedParameters.put("password", (user.getPassword() != null && !user.getPassword().trim().isEmpty()) ? user.getPassword() : "password");
		namedParameters.put("first_name", user.getFirstName());
		namedParameters.put("last_name", user.getLastName());
		this.namedParameterJdbcTemplate.update(sql, namedParameters);
		return user;
    }
    
    public void delete(User user) {
    	String sql = "update users set username = :username, active = 'N' where id = :id";
		Map<String, Object> namedParameters = new HashMap<String, Object>();
		namedParameters.put("id", user.getId());
		namedParameters.put("username", user.getUsername() + "_deactivated_" + (new Date().toString()));
		this.namedParameterJdbcTemplate.update(sql, namedParameters);
    }
    
}
