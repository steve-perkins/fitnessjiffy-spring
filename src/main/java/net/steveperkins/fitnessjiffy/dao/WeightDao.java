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

	private Log log = LogFactory.getLog(WeightDao.class);
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-mm-dd", Locale.ENGLISH);

//	private JdbcTemplate jdbcTemplate;
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
//        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }
    
    public List<Weight> findAllForUser(int userId) {
    	return findAllForUser(userId, null, null);
    }

    public List<Weight> findAllForUser(int userId, Date startDate, Date endDate) {
    	List<Map<String, Object>> weightMaps = null;
    	if(startDate == null || endDate == null) {
    		// Default query, all dates
        	String sql = "select * from weight where user_id = :userId order by date desc";
        	SqlParameterSource namedParameters = new MapSqlParameterSource("userId", userId);
        	weightMaps = this.namedParameterJdbcTemplate.queryForList(sql, namedParameters);
    	} else {
    		// Range query
    		String sql = "select * from weight where user_id = :userId and date >= :startDate and date <= :endDate order by date desc";
    		MapSqlParameterSource namedParameters = new MapSqlParameterSource("userId", userId);
        	namedParameters.addValue("startDate", dateFormatter.format(startDate));
        	namedParameters.addValue("endDate", dateFormatter.format(endDate));
        	weightMaps = this.namedParameterJdbcTemplate.queryForList(sql, namedParameters);
    	}
    	List<Weight> weights = new ArrayList<Weight>();
    	for(Map<String, Object> weightMap : weightMaps) {
			try {
				Weight weight = new Weight();
				weight.setId((Integer)weightMap.get("ID"));
				weight.setUserId((Integer)weightMap.get("USER_ID"));
				weight.setDate( dateFormatter.parse((String)weightMap.get("DATE")) );
				weight.setPounds(((Double)weightMap.get("POUNDS")).floatValue());
				weights.add(weight);
			} catch (ParseException e) {
				log.error("Could not parse date string [" 
						+ StringUtils.defaultString((String)weightMap.get("DATE"), "null") 
						+ "] for Weight with ID [" 
						+ StringUtils.defaultString((String)weightMap.get("ID"), "null")
						+ "]");
			}
    	}
    	return weights;
    }

}
