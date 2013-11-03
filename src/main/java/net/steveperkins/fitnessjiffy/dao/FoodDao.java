package net.steveperkins.fitnessjiffy.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.steveperkins.fitnessjiffy.domain.Food;
import net.steveperkins.fitnessjiffy.domain.Food.ServingType;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

public class FoodDao extends BaseDao<Food> {
		
	static final String FOOD_TABLE = "FOODS";
	static final String ID = "ID";
	static final String USER_ID = "USER_ID";
	static final String NAME = "NAME";
	static final String DEFAULT_SERVING_TYPE = "DEFAULT_SERVING_TYPE";
	static final String SERVING_TYPE_QTY = "SERVING_TYPE_QTY";
	static final String CALORIES = "CALORIES";
	static final String FAT = "FAT";
	static final String SATURATED_FAT = "SATURATED_FAT";
	static final String CARBS = "CARBS";
	static final String FIBER = "FIBER";
	static final String SUGAR = "SUGAR";
	static final String PROTEIN = "PROTEIN";
	static final String SODIUM = "SODIUM";
	    
    public Food findById(int id) {
    	String sql = "select * from "+FOOD_TABLE+" where "+ID+" = :"+ID;
    	SqlParameterSource namedParameters = new MapSqlParameterSource(ID, id);
    	return this.namedParameterJdbcTemplate.queryForObject(sql, namedParameters, new RowMapper<Food>() {
			public Food mapRow(ResultSet rs, int rowNum) throws SQLException {
				Food food = new Food();
				food.setId(rs.getInt(ID));
	    		food.setUserId(rs.getInt(USER_ID));
	    		food.setName(rs.getString(NAME));
	    		food.setDefaultServingType(ServingType.fromString(rs.getString(DEFAULT_SERVING_TYPE)));
	    		food.setServingTypeQty(rs.getFloat(SERVING_TYPE_QTY));
	    		food.setCalories(rs.getInt(CALORIES));
	    		food.setFat(rs.getFloat(FAT));
	    		food.setSaturatedFat(rs.getFloat(SATURATED_FAT));
	    		food.setCarbs(rs.getFloat(CARBS));
	    		food.setFiber(rs.getFloat(FIBER));
	    		food.setSugar(rs.getFloat(SUGAR));
	    		food.setProtein(rs.getFloat(PROTEIN));
	    		food.setSodium(rs.getFloat(SODIUM));
				return food;
			}
		});
    }
    
    public List<Food> findByUser(int userId) {
    	String sql = 
    			"select * from "+FOOD_TABLE
    			+" where "+USER_ID+" is null"
    			+" and "+NAME+" not in (select "+NAME+" from foods where "+USER_ID+" = :"+USER_ID+") "
    			+" union select * from "+FOOD_TABLE+" where "+USER_ID+" = :"+USER_ID;
    	SqlParameterSource namedParameters = new MapSqlParameterSource(USER_ID, userId);
    	return processFoodMaps(this.namedParameterJdbcTemplate.queryForList(sql, namedParameters));   	
    }
    
    public List<Food> findByNameLike(int userId, String searchString) {
    	String sql = 
    			"select * from "+FOOD_TABLE
    			+" where "+USER_ID+" is null"
    			+" and upper("+NAME+") like '%:"+searchString+"%' "
    			+" and upper("+NAME+") not in (select upper("+NAME+") from foods where "+USER_ID+" = :"+USER_ID+" and upper("+NAME+") like '%:"+searchString+"%') "
    			+" union select * from "+FOOD_TABLE+" where "+USER_ID+" = :"+USER_ID+" and upper("+NAME+") like '%:"+searchString+"%'";
    	MapSqlParameterSource namedParameters = new MapSqlParameterSource(USER_ID, userId);
    	namedParameters.addValue("searchString", searchString.trim().toUpperCase());
    	return processFoodMaps(this.namedParameterJdbcTemplate.queryForList(sql, namedParameters));   	
    }
    
    List<Food> processFoodMaps(List<Map<String, Object>> foodMaps) {
    	List<Food> foods = new ArrayList<Food>();
    	for(Map<String, Object> foodMap : foodMaps) {
    		Food food = new Food();
    		food.setId((Integer)foodMap.get(ID));
    		food.setUserId( foodMap.get(USER_ID) != null ? (Integer)foodMap.get(USER_ID) : 0 );
    		food.setName((String)foodMap.get(NAME));
    		food.setDefaultServingType(ServingType.fromString((String)foodMap.get(DEFAULT_SERVING_TYPE)));
    		food.setServingTypeQty( ((Double)foodMap.get(SERVING_TYPE_QTY)).floatValue() );
    		food.setCalories( (int) Double.parseDouble(foodMap.get(CALORIES).toString()) );
    		food.setFat( ((Double)foodMap.get(FAT)).intValue() );
    		food.setSaturatedFat( ((Double)foodMap.get(SATURATED_FAT)).intValue() );
    		food.setCarbs( ((Double)foodMap.get(CARBS)).intValue() );
    		food.setFiber( ((Double)foodMap.get(FIBER)).intValue() );
    		food.setSugar( ((Double)foodMap.get(SUGAR)).intValue() );
    		food.setProtein( ((Double)foodMap.get(PROTEIN)).intValue() );
    		food.setSodium( ((Double)foodMap.get(SODIUM)).intValue() );
    		foods.add(food);
    	}
    	return foods;    	
    }

	@Override
	public List<Food> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Food save(Food entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(Food entity) {
		// TODO Auto-generated method stub
		
	}
    
}
