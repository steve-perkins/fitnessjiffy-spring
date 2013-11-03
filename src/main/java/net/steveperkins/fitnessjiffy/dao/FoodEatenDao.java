package net.steveperkins.fitnessjiffy.dao;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import net.steveperkins.fitnessjiffy.domain.Food;
import net.steveperkins.fitnessjiffy.domain.Food.ServingType;
import net.steveperkins.fitnessjiffy.domain.FoodEaten;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public class FoodEatenDao extends BaseDao<FoodEaten> {
	
	static final String FOOD_EATEN_TABLE = "FOODS_EATEN";
	static final String ID = "ID";
	static final String USER_ID = "USER_ID";
	static final String FOOD_ID = "FOOD_ID";
	static final String DATE = "DATE";
	static final String SERVING_TYPE = "SERVING_TYPE";
	static final String SERVING_QTY = "SERVING_QTY";
	
	@Autowired
	private FoodDao foodDao;
	
	private Log log = LogFactory.getLog(FoodEatenDao.class);

	public List<FoodEaten> findEatenOnDate(int userId, Date date) {
		String sql = "select * from "+FOOD_EATEN_TABLE+" where "+USER_ID+" = :"+USER_ID+" and "+DATE+" = :"+DATE;
		MapSqlParameterSource namedParameters = new MapSqlParameterSource(USER_ID, userId);
		namedParameters.addValue(DATE, dateFormatter.format(date));
		return processFoodEatenMaps( this.namedParameterJdbcTemplate.queryForList(sql, namedParameters) );
	}

	public List<Food> findEatenRecently(int userId) {
		String sql = 
				"select * from "+FoodDao.FOOD_TABLE
				+" where "+FoodDao.ID+" in "
				+" (select distinct "+FoodDao.FOOD_TABLE+"."+FoodDao.ID+" from "+FoodDao.FOOD_TABLE+", "+FOOD_EATEN_TABLE
				+" where "+FoodDao.FOOD_TABLE+"."+FoodDao.ID+" = "+FOOD_EATEN_TABLE+"."+FOOD_ID
				+" and "+FOOD_EATEN_TABLE+"."+USER_ID+" = :"+USER_ID
				+" and "+FOOD_EATEN_TABLE+"."+DATE+" >= date('now', '-7 day'))"
				+" order by "+FoodDao.NAME+" asc";
	    	MapSqlParameterSource namedParameters = new MapSqlParameterSource(USER_ID, userId);
	    	return foodDao.processFoodMaps( this.namedParameterJdbcTemplate.queryForList(sql, namedParameters) );  
	    }

	List<FoodEaten> processFoodEatenMaps(List<Map<String, Object>> foodEatenMaps) {
		List<FoodEaten> foodsEaten = new ArrayList<>();
		for(Map<String, Object> foodEatenMap : foodEatenMaps) {
			try {
	    		FoodEaten foodEaten = new FoodEaten();
	    		foodEaten.setId((Integer)foodEatenMap.get(ID));
	    		foodEaten.setUserId((Integer)foodEatenMap.get(USER_ID));
	    		foodEaten.setFood(foodDao.findById((Integer)foodEatenMap.get(FOOD_ID)));
				foodEaten.setDate( dateFormatter.parse((String)foodEatenMap.get(DATE)) );
				foodEaten.setServingType(ServingType.fromString((String)foodEatenMap.get(SERVING_TYPE)));
				foodEaten.setServingQty( (float) Double.parseDouble(foodEatenMap.get(SERVING_QTY).toString()) );
				foodsEaten.add(foodEaten);
			} catch (ParseException e) {
				log.error("Could not parse date string [" 
						+ StringUtils.defaultString((String)foodEatenMap.get(DATE), "null") 
						+ "] for Weight with ID [" 
						+ StringUtils.defaultString((String)foodEatenMap.get(ID), "null")
						+ "]");
			}
		}
		return foodsEaten;
	}

	@Override
	public FoodEaten findById(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<FoodEaten> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FoodEaten save(FoodEaten entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(FoodEaten entity) {
		// TODO Auto-generated method stub
		
	}

}
