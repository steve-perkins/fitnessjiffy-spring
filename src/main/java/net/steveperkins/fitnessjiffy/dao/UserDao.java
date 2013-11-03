package net.steveperkins.fitnessjiffy.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.steveperkins.fitnessjiffy.domain.User;
import net.steveperkins.fitnessjiffy.domain.User.ActivityLevel;
import net.steveperkins.fitnessjiffy.domain.User.Gender;

import net.steveperkins.fitnessjiffy.domain.User.YesNo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

@Repository
public class UserDao extends BaseDao<User> {

	static final String USER_TABLE = "USERS";
	static final String ID = "ID"; 
	static final String ACTIVITY_LEVEL = "ACTIVITY_LEVEL"; 
	static final String AGE = "AGE"; 
	static final String FIRST_NAME = "FIRST_NAME"; 
	static final String GENDER = "GENDER"; 
	static final String HEIGHT_IN_INCHES = "HEIGHT_IN_INCHES"; 
	static final String LAST_NAME = "LAST_NAME"; 
	static final String PASSWORD = "PASSWORD"; 
	static final String USERNAME = "USERNAME"; 
	static final String ACTIVE = "ACTIVE"; 
	
	@Autowired
	private WeightDao weightDao;
    
	@Override
    public User findById(int id) {
    	String sql = "select * from "+USER_TABLE+" where "+ACTIVE+" = 'Y' and "+ID+" = :"+ID;
    	SqlParameterSource namedParameters = new MapSqlParameterSource(ID, id);
    	return this.namedParameterJdbcTemplate.queryForObject(sql, namedParameters, new RowMapper<User>() {
			public User mapRow(ResultSet rs, int rowNum) throws SQLException {
				User user = new User();
				user.setId(rs.getInt(ID));
				user.setActivityLevel(ActivityLevel.fromValue(rs.getFloat(ACTIVITY_LEVEL)));
				user.setAge(rs.getInt(AGE));
				user.setFirstName(rs.getString(FIRST_NAME));
				user.setGender(Gender.fromString(rs.getString(GENDER)));
				user.setHeightInInches(rs.getFloat(HEIGHT_IN_INCHES));
				user.setLastName(rs.getString(LAST_NAME));
				user.setPassword(rs.getString(PASSWORD));
				user.setUsername(rs.getString(USERNAME));
				user.setActive(YesNo.fromValue(rs.getString(ACTIVE).charAt(0)));
				user.setWeights(weightDao.findAllForUser(user.getId()));
				return user;
			}
		});
    }
    
	@Override
    public List<User> findAll() {
    	String sql = "select * from "+USER_TABLE+" where "+ACTIVE+" = 'Y'";
    	List<Map<String, Object>> userMaps = this.jdbcTemplate.queryForList(sql);
    	List<User> users = new ArrayList<User>();
    	for(Map<String, Object> userMap : userMaps) {
			User user = new User();
			user.setId((Integer)userMap.get(ID));
			user.setActivityLevel(ActivityLevel.fromValue(((Double)userMap.get(ACTIVITY_LEVEL)).floatValue()));
			user.setAge((Integer)userMap.get(AGE));
			user.setFirstName((String)userMap.get(FIRST_NAME));
			user.setGender(Gender.fromString((String)userMap.get(GENDER)));
			user.setHeightInInches(((Double)userMap.get(HEIGHT_IN_INCHES)).floatValue());
			user.setLastName((String)userMap.get(LAST_NAME));
			user.setPassword((String)userMap.get(PASSWORD));
			user.setUsername((String)userMap.get(USERNAME));
			user.setActive(YesNo.fromValue(((String)userMap.get(ACTIVE)).charAt(0)));
			user.setWeights(weightDao.findAllForUser(user.getId()));
			users.add(user);
    	}
    	return users;
    }
    
	@Override
    public User save(User user) {
    	String sql = "";
    	if(user.getId() == 0 || findById(user.getId()) == null) {
    		// Insert
    		int maxId = this.jdbcTemplate.queryForObject("select max("+ID+") from "+USER_TABLE, Integer.class).intValue();
    		user.setId(maxId + 1);
    		sql = "insert into "+USER_TABLE+"("+ID+", "+GENDER+", "+AGE+", "+HEIGHT_IN_INCHES+", "+ACTIVITY_LEVEL+", "+USERNAME+", "+PASSWORD+", "+FIRST_NAME+", "+LAST_NAME+", "+ACTIVE+")"
    				+" values (:"+ID+", :"+GENDER+", :"+AGE+", :"+HEIGHT_IN_INCHES+", :"+ACTIVITY_LEVEL+", :"+USERNAME+", :"+PASSWORD+", :"+FIRST_NAME+", :"+LAST_NAME+", 'Y')";
    	} else {
    		// Update
    		sql = "update "+USER_TABLE+" set "+GENDER+" = :"+GENDER+", "+AGE+" = :"+AGE+", "+HEIGHT_IN_INCHES+" = :"+HEIGHT_IN_INCHES+", "+ACTIVITY_LEVEL+" = :"+ACTIVITY_LEVEL+
    				", "+USERNAME+" = :"+USERNAME+", "+PASSWORD+" = :"+PASSWORD+", "+FIRST_NAME+" = :"+FIRST_NAME+", "+LAST_NAME+" = :"+LAST_NAME+", "+ACTIVE+" = 'Y' where "+ID+" = :"+ID;
    	}
		Map<String, Object> namedParameters = new HashMap<String, Object>();
		namedParameters.put(ID, user.getId());
		namedParameters.put(GENDER, user.getGender().toString());
		namedParameters.put(AGE, user.getAge());
		namedParameters.put(HEIGHT_IN_INCHES, user.getHeightInInches());
		namedParameters.put(ACTIVITY_LEVEL, user.getActivityLevel().getValue());
		namedParameters.put(USERNAME, user.getUsername());
		// TODO: Implement a real change password function
		namedParameters.put(PASSWORD, (user.getPassword() != null && !user.getPassword().trim().isEmpty()) ? user.getPassword() : "password");
		namedParameters.put(FIRST_NAME, user.getFirstName());
		namedParameters.put(LAST_NAME, user.getLastName());
		this.namedParameterJdbcTemplate.update(sql, namedParameters);
		return user;
    }
    
	@Override
    public void delete(User user) {
    	String sql = "update "+USER_TABLE+" set "+USERNAME+" = :"+USERNAME+", "+ACTIVE+" = 'N' where "+ID+" = :"+ID;
		Map<String, Object> namedParameters = new HashMap<String, Object>();
		namedParameters.put(ID, user.getId());
		namedParameters.put(USERNAME, user.getUsername() + "_deactivated_" + (new Date().toString()));
		this.namedParameterJdbcTemplate.update(sql, namedParameters);
    }
    
}
