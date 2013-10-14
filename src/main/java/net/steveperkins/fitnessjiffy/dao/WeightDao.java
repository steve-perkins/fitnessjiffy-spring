package net.steveperkins.fitnessjiffy.dao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.sql.DataSource;

import net.steveperkins.fitnessjiffy.domain.Weight;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

public class WeightDao {

	private static final String COLUMN_ID = "ID"; 
	private static final String COLUMN_USER_ID = "USER_ID"; 
	private static final String COLUMN_DATE = "DATE"; 
	private static final String COLUMN_POUNDS = "POUNDS"; 
	
	private Log log = LogFactory.getLog(WeightDao.class);
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-mm-dd", Locale.ENGLISH);

	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }
    
    public List<Weight> findAllForUser(int userId) {
    	return findAllForUser(userId, null, null);
    }

    public List<Weight> findAllForUser(int userId, Date startDate, Date endDate) {
    	List<Map<String, Object>> weightMaps = null;
    	if(startDate == null || endDate == null) {
    		// Default query, all dates
        	String sql = String.format("select * from weight where %s = :%s order by %s desc", COLUMN_USER_ID, COLUMN_USER_ID, COLUMN_DATE);
        	SqlParameterSource namedParameters = new MapSqlParameterSource(COLUMN_USER_ID, userId);
        	weightMaps = this.namedParameterJdbcTemplate.queryForList(sql, namedParameters);
    	} else {
    		// Range query
    		String sql = String.format("select * from weight where %s = :%s and %s >= :startDate and %s <= :endDate order by %s desc", COLUMN_USER_ID, COLUMN_USER_ID, COLUMN_DATE, COLUMN_DATE, COLUMN_DATE);
    		MapSqlParameterSource namedParameters = new MapSqlParameterSource(COLUMN_USER_ID, userId);
        	namedParameters.addValue("startDate", dateFormatter.format(startDate));
        	namedParameters.addValue("endDate", dateFormatter.format(endDate));
        	weightMaps = this.namedParameterJdbcTemplate.queryForList(sql, namedParameters);
    	}
    	List<Weight> weights = new ArrayList<Weight>();
    	for(Map<String, Object> weightMap : weightMaps) {
			try {
				Weight weight = new Weight();
				weight.setId((Integer)weightMap.get(COLUMN_ID));
				weight.setUserId((Integer)weightMap.get(COLUMN_USER_ID));
				weight.setDate( dateFormatter.parse((String)weightMap.get(COLUMN_DATE)) );
				weight.setPounds(((Double)weightMap.get(COLUMN_POUNDS)).floatValue());
				weights.add(weight);
			} catch (ParseException e) {
				log.error("Could not parse date string [" 
						+ StringUtils.defaultString((String)weightMap.get(COLUMN_DATE), "null") 
						+ "] for Weight with ID [" 
						+ StringUtils.defaultString((String)weightMap.get(COLUMN_ID), "null")
						+ "]");
			}
    	}
    	return weights;
    }

}
