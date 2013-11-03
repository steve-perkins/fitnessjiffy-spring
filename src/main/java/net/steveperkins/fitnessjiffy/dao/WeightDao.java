package net.steveperkins.fitnessjiffy.dao;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import net.steveperkins.fitnessjiffy.domain.Weight;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

public class WeightDao extends BaseDao<Weight>{

	static final String WEIGHT_TABLE = "WEIGHT";
	static final String ID = "ID"; 
	static final String USER_ID = "USER_ID"; 
	static final String DATE = "DATE"; 
	static final String POUNDS = "POUNDS"; 
	
	private Log log = LogFactory.getLog(WeightDao.class);
    
    public List<Weight> findAllForUser(int userId) {
    	return findAllForUser(userId, null, null);
    }

    public List<Weight> findAllForUser(int userId, Date startDate, Date endDate) {
    	List<Map<String, Object>> weightMaps = null;
    	if(startDate == null || endDate == null) {
    		// Default query, all dates
        	String sql = "select * from "+WEIGHT_TABLE+" where "+USER_ID+" = :"+USER_ID+" order by "+DATE+" desc";
        	SqlParameterSource namedParameters = new MapSqlParameterSource(USER_ID, userId);
        	weightMaps = this.namedParameterJdbcTemplate.queryForList(sql, namedParameters);
    	} else {
    		// Range query
    		String sql = "select * from "+WEIGHT_TABLE+" where "+USER_ID+" = :"+USER_ID+" and "+DATE+" >= :startDate and "+DATE+" <= :endDate order by "+DATE+" desc";
    		MapSqlParameterSource namedParameters = new MapSqlParameterSource(USER_ID, userId);
        	namedParameters.addValue("startDate", dateFormatter.format(startDate));
        	namedParameters.addValue("endDate", dateFormatter.format(endDate));
        	weightMaps = this.namedParameterJdbcTemplate.queryForList(sql, namedParameters);
    	}
    	List<Weight> weights = new ArrayList<>();
    	for(Map<String, Object> weightMap : weightMaps) {
			try {
				Weight weight = new Weight();
				weight.setId((Integer)weightMap.get(ID));
				weight.setUserId((Integer)weightMap.get(USER_ID));
				weight.setDate( dateFormatter.parse((String)weightMap.get(DATE)) );
				weight.setPounds(((Double)weightMap.get(POUNDS)).floatValue());
				weights.add(weight);
			} catch (ParseException e) {
				log.error("Could not parse date string [" 
						+ StringUtils.defaultString((String)weightMap.get(DATE), "null") 
						+ "] for Weight with ID [" 
						+ StringUtils.defaultString((String)weightMap.get(ID), "null")
						+ "]");
			}
    	}
    	return weights;
    }

	@Override
	public Weight findById(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Weight> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Weight save(Weight entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(Weight entity) {
		// TODO Auto-generated method stub
		
	}

}
