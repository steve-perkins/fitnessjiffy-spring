package net.steveperkins.fitnessjiffy.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.sql.DataSource;

import net.steveperkins.fitnessjiffy.domain.Food;
import net.steveperkins.fitnessjiffy.domain.Food.ServingType;
import net.steveperkins.fitnessjiffy.domain.FoodEaten;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

public class FoodDao {
		
	private class FoodColumns {
		public static final String ID = "ID";
		public static final String USER_ID = "USER_ID"; 
		public static final String NAME = "NAME"; 
		public static final String DEFAULT_SERVING_TYPE = "DEFAULT_SERVING_TYPE"; 
		public static final String SERVING_TYPE_QTY = "SERVING_TYPE_QTY"; 
		public static final String CALORIES = "CALORIES"; 
		public static final String FAT = "FAT"; 
		public static final String SATURATED_FAT = "SATURATED_FAT"; 
		public static final String CARBS = "CARBS"; 
		public static final String FIBER = "FIBER"; 
		public static final String SUGAR = "SUGAR"; 
		public static final String PROTEIN = "PROTEIN"; 
		public static final String SODIUM = "SODIUM"; 
	}
	
	private class FoodEatenColumns {
		private static final String ID = "ID";
		private static final String USER_ID = "USER_ID";
		private static final String FOOD_ID = "FOOD_ID";
		private static final String DATE = "DATE";
		private static final String SERVING_TYPE = "SERVING_TYPE";
		private static final String SERVING_QTY = "SERVING_QTY";
	}
	
	private Log log = LogFactory.getLog(FoodDao.class);
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-mm-dd", Locale.ENGLISH);
	
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	
    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }
    
    public Food findById(int id) {
    	String sql = String.format("select * from foods where %s = :%s", FoodColumns.ID, FoodColumns.ID);
    	SqlParameterSource namedParameters = new MapSqlParameterSource(FoodColumns.ID, id);
    	return this.namedParameterJdbcTemplate.queryForObject(sql, namedParameters, new RowMapper<Food>() {
			public Food mapRow(ResultSet rs, int rowNum) throws SQLException {
				Food food = new Food();
				food.setId(rs.getInt(FoodColumns.ID));
	    		food.setUserId( rs.getInt(FoodColumns.USER_ID) == 0 ? null : (Integer)rs.getInt(FoodColumns.USER_ID) );
	    		food.setName(rs.getString(FoodColumns.NAME));
	    		food.setDefaultServingType(ServingType.fromString(rs.getString(FoodColumns.DEFAULT_SERVING_TYPE)));
	    		food.setServingTypeQty(rs.getFloat(FoodColumns.SERVING_TYPE_QTY));
	    		food.setCalories(rs.getInt(FoodColumns.CALORIES));
	    		food.setFat(rs.getFloat(FoodColumns.FAT));
	    		food.setSaturatedFat(rs.getFloat(FoodColumns.SATURATED_FAT));
	    		food.setCarbs(rs.getFloat(FoodColumns.CARBS));
	    		food.setFiber(rs.getFloat(FoodColumns.FIBER));
	    		food.setSugar(rs.getFloat(FoodColumns.SUGAR));
	    		food.setProtein(rs.getFloat(FoodColumns.PROTEIN));
	    		food.setSodium(rs.getFloat(FoodColumns.SODIUM));
				return food;
			}
		});
    }
    
    public List<Food> findByUser(int userId) {
    	String sql = String.format("select * from foods where %s is null and %s not in (select %s from foods where %s = :%s) union select * from foods where %s = :%s",
    			FoodColumns.USER_ID, FoodColumns.NAME, FoodColumns.NAME, FoodColumns.USER_ID, FoodColumns.USER_ID, FoodColumns.USER_ID, FoodColumns.USER_ID);
    	SqlParameterSource namedParameters = new MapSqlParameterSource(FoodColumns.USER_ID, userId);
    	return processFoodMaps(this.namedParameterJdbcTemplate.queryForList(sql, namedParameters));   	
    }
    
    public List<Food> findByNameLike(int userId, String searchString) {
    	String sql = String.format(
    			"select * from foods where %s is null and upper(%s) like '%:searchString%' and upper(%s) not in "
    			+ "(select upper(%s) from foods where %s = :%s and upper(%s) like '%:searchString%') "
    			+ "union select * from fooods where %s = :%s and upper(%s) like '%:searchString%'",
    			FoodColumns.USER_ID, FoodColumns.NAME, FoodColumns.NAME, FoodColumns.NAME, FoodColumns.USER_ID, FoodColumns.USER_ID, FoodColumns.NAME, FoodColumns.USER_ID, FoodColumns.USER_ID, FoodColumns.NAME);
    	MapSqlParameterSource namedParameters = new MapSqlParameterSource(FoodColumns.USER_ID, userId);
    	namedParameters.addValue("searchString", searchString.trim().toUpperCase());
    	return processFoodMaps(this.namedParameterJdbcTemplate.queryForList(sql, namedParameters));   	
    }
    
    private List<Food> processFoodMaps(List<Map<String, Object>> foodMaps) {
    	List<Food> foods = new ArrayList<Food>();
    	for(Map<String, Object> foodMap : foodMaps) {
    		Food food = new Food();
    		food.setId((Integer)foodMap.get(FoodColumns.ID));
    		food.setUserId( foodMap.get(FoodColumns.USER_ID) == null ? 0 : (Integer)foodMap.get(FoodColumns.USER_ID) );
    		food.setName((String)foodMap.get(FoodColumns.NAME));
    		food.setDefaultServingType(ServingType.fromString((String)foodMap.get(FoodColumns.DEFAULT_SERVING_TYPE)));
    		food.setServingTypeQty( ((Double)foodMap.get(FoodColumns.SERVING_TYPE_QTY)).floatValue() );
    		food.setCalories( (int) Double.parseDouble(foodMap.get(FoodColumns.CALORIES).toString()) );
    		food.setFat( ((Double)foodMap.get(FoodColumns.FAT)).intValue() );
    		food.setSaturatedFat( ((Double)foodMap.get(FoodColumns.SATURATED_FAT)).intValue() );
    		food.setCarbs( ((Double)foodMap.get(FoodColumns.CARBS)).intValue() );
    		food.setFiber( ((Double)foodMap.get(FoodColumns.FIBER)).intValue() );
    		food.setSugar( ((Double)foodMap.get(FoodColumns.SUGAR)).intValue() );
    		food.setProtein( ((Double)foodMap.get(FoodColumns.PROTEIN)).intValue() );
    		food.setSodium( ((Double)foodMap.get(FoodColumns.SODIUM)).intValue() );
    		foods.add(food);
    	}
    	return foods;    	
    }
    
    public List<FoodEaten> findEatenByDate(int userId, Date date) {
    	String sql = String.format("select * from foods_eaten where %s = :%s and %s = :%s", FoodEatenColumns.USER_ID, FoodEatenColumns.USER_ID, FoodEatenColumns.DATE, FoodEatenColumns.DATE);
    	MapSqlParameterSource namedParameters = new MapSqlParameterSource(FoodEatenColumns.USER_ID, userId);
    	namedParameters.addValue(FoodEatenColumns.DATE, dateFormatter.format(date));
    	List<FoodEaten> foodsEaten = new ArrayList<FoodEaten>();
    	List<Map<String, Object>> foodEatenMaps = this.namedParameterJdbcTemplate.queryForList(sql, namedParameters);
    	for(Map<String, Object> foodEatenMap : foodEatenMaps) {
    		try {
        		FoodEaten foodEaten = new FoodEaten();
        		foodEaten.setId((Integer)foodEatenMap.get(FoodEatenColumns.ID));
        		foodEaten.setUserId((Integer)foodEatenMap.get(FoodEatenColumns.USER_ID));
				foodEaten.setFoodId((Integer)foodEatenMap.get(FoodEatenColumns.FOOD_ID));
				foodEaten.setDate( dateFormatter.parse((String)foodEatenMap.get(FoodEatenColumns.DATE)) );
				foodEaten.setServingType(ServingType.fromString((String)foodEatenMap.get(FoodEatenColumns.SERVING_TYPE)));
				foodEaten.setServingQty( (float) Double.parseDouble(foodEatenMap.get(FoodEatenColumns.SERVING_QTY).toString()) );
				foodsEaten.add(foodEaten);
			} catch (ParseException e) {
				log.error("Could not parse date string [" 
						+ StringUtils.defaultString((String)foodEatenMap.get(FoodEatenColumns.DATE), "null") 
						+ "] for Weight with ID [" 
						+ StringUtils.defaultString((String)foodEatenMap.get(FoodEatenColumns.ID), "null")
						+ "]");
			}
    	}
    	return foodsEaten;    	
    }
    
}
