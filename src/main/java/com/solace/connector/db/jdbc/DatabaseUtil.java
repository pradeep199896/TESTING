package com.solace.connector.db.jdbc;

import com.solace.connector.db.config.DbTableIndicatorConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
@Slf4j
public class DatabaseUtil {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Value("${spring.jpa.database}")
    String sqlType;

    private final Map emptyMap = new HashMap<>() ;

    @Transactional
    public List<Map<String, Object>> queryForChange(String table ,String flag){
        Map<String ,Object> params = new HashMap<>();

        List<Map<String, Object>> list  = jdbcTemplate.queryForList("select * from "+table +" where " +flag  + " ='0'",params) ;
        jdbcTemplate.update("update "+table+" set "+ flag +" ='1'" , params) ;

        return list ;
    }

    public int[] execBatchSql(String sql , Map<String ,Object>[] paramsArray){
        log.info("exec batch size[{}] sql===> {}",paramsArray.length,sql);
        return jdbcTemplate.batchUpdate(sql,paramsArray);
    }

    public int execSql(String sql , Map<String ,Object> paramsArray){
        log.info("exec single sql===> {}", sql);
        return jdbcTemplate.update(sql,paramsArray);
    }

    public Date queryDatabaseTime(){
        if("mysql".equals(sqlType)){
            return jdbcTemplate.queryForObject("SELECT CURRENT_TIMESTAMP(3) FROM DUAL",emptyMap, Date.class);
        }
        else if("oracle".equals(sqlType)){
            return jdbcTemplate.queryForObject("select LOCALTIMESTAMP(3) from dual ",emptyMap, Date.class);
        }
        return Calendar.getInstance().getTime();
    }


    public int updateBatchIndicator(String tableName ,String indicator , String orderby ,String readTime ,Map<String ,Object> paramsArray){
        synchronized (jdbcTemplate) {
            String sql = "update " + tableName + " set " + indicator + " = :indicatorValue ," + readTime + "=:readTime where " + orderby + ">= :minValue and " + orderby + " <= :maxValue";
            log.debug("exec Batch sql===> {}", sql);
            return jdbcTemplate.update(sql, paramsArray);
        }
    }

    public int updateSingleIndicator(String tableName ,String indicator , String orderby ,String trackingIndicator ,Map<String ,Object> paramsArray){
        synchronized (jdbcTemplate) {
            String sql = "update " + tableName + " set " + indicator + " = :indicatorValue ," + trackingIndicator + "=:trackingValue where " + orderby + " = :orderValue ";
//        log.debug("exec single sql===> {}", sql);
            return jdbcTemplate.update(sql, paramsArray);
        }
    }

    public int[] batchUpdateSingleIndicator(String tableName ,String indicator , String orderby ,String trackingIndicator ,Map<String ,Object>[] paramsArray){
        synchronized (jdbcTemplate){
            String sql = "update " + tableName +" set " + indicator + " = :indicatorValue ," + trackingIndicator + "=:trackingValue where " +orderby +" = :orderValue " ;
            log.debug("exec Batch sql===> {}", sql);

            return jdbcTemplate.batchUpdate(sql,paramsArray);
        }
    }

    public int[] batchUpdateSingleIndicator(String tableName ,String indicator , String orderby, String trackingIndicator , String readTime,Map<String ,Object>[] paramsArray){
        synchronized (jdbcTemplate) {
            String sql = "update " + tableName + " set " + indicator + " = :indicatorValue ," + trackingIndicator + " =:trackingValue, " + readTime + "=:readTime where " + orderby + " = :orderValue ";
            log.debug("exec Batch sql===> {}", sql);

            return jdbcTemplate.batchUpdate(sql, paramsArray);
        }
    }


    @Transactional
    public List<Map<String, Object>> queryForChidlren(DbTableIndicatorConfig localConfig, Object start , Object end){
        Map<String ,Object> params = new HashMap<>();
        //TODO handle order type
        if(localConfig.getReadIndicatorOrder().equals("desc")){
            params.put("start",end);
            params.put("end",start);
        }else{
            params.put("start",start);
            params.put("end",end);
        }

        List<Map<String, Object>> list  = jdbcTemplate.queryForList("select c.* from "+localConfig.getChildTableName()
                +" c join "+ localConfig.getTableName() +" p on c."+ localConfig.getChildKeyColumn()+" = p."+ localConfig.getTableKeyColumn()+
                " where p."+localConfig.getReadIndicatorOrderByColumn()+" >= :start and p."+localConfig.getReadIndicatorOrderByColumn()+" <=:end ",params) ;

        return list ;
    }

}