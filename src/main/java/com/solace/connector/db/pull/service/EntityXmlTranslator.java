package com.solace.connector.db.pull.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ClassUtil;
import com.google.gson.internal.LinkedTreeMap;
import com.solace.connector.core.transformation.engine.service.TransformationEngine;
import com.solace.connector.core.transformation.engine.utils.TransformationMode;
import com.solace.connector.db.config.DbTableIndicatorConfig;
import com.solace.connector.db.config.PullConfig;
import com.solace.connector.exception.InvalidPropertyException;
import com.solace.connector.utility.JpaBeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class EntityXmlTranslator {

    @Resource
    @Lazy
    DataCollecttingService service;

    @Resource
    PullConfig config;

    List<Object> entityList;

    static AtomicInteger currentHead = new AtomicInteger(0);

    TransformationEngine transformationEngine = new TransformationEngine();

    //    @Async("translatorPool")
    public void translate(DataCollecttingContext context, MessageLoad messageLoad) {

        DbTableIndicatorConfig tableConfig = context.getTableConfig();
        String tableName = tableConfig.getTableKey();
        int index = messageLoad.getIndex();
        log.debug("Thread {} start for beginIndex:{} ,size: {} ", Thread.currentThread(), messageLoad.getStart(), messageLoad.getEnd());

        List<Map<String, Object>> rowMaps = new ArrayList<>();

        String uuid = UUID.randomUUID().toString();
        try {

            Map<String ,String > staticValues = context.getStaticValues();
            Map<String ,String > msgHeaderValues = context.getMsgHeaderValues();
            //in most case, messageLoad carries one row data
            for (Object row : messageLoad.getObjectList()) {
                //set staticValues
                if (row == null) {
                    log.error("\n============\nERROR REASON:   Query return null object ," +
                            " maybe caused by table row with null value in entity id Object , Check the table data and Key config .\n============");
                } else {
                    for (String k : staticValues.keySet()) {
                        JpaBeanUtil.invokeSetter(row, k, staticValues.get(k));
                    }
                    JpaBeanUtil.invokeSetter(row, tableConfig.getTrackingIdIndicator(), uuid);
                    Map<String,Object> beanMap = BeanUtil.beanToMap(row);
                    Field idf = ClassUtil.getDeclaredField(row.getClass(),"id");
                    if(idf !=null){
                        Object id = JpaBeanUtil.invokeGetter(row,"id");
                        if(id != null && id.getClass().getTypeName().equals(row.getClass().getTypeName()+"Id"))
                            beanMap.putAll(BeanUtil.beanToMap(id));
                    }
                    Map<String,Object> treeBeanMap = new LinkedTreeMap<>();
                    treeBeanMap.putAll(beanMap);
                    parseObject2String(tableConfig, treeBeanMap ,false);
                    rowMaps.add(treeBeanMap);
//                    rowMaps.add(context.parseEntity2Map(row, tableConfig.trgtMapping));
                    for (String key : msgHeaderValues.keySet()) {
                        Object value = null;

                        value = JpaBeanUtil.invokeGetter(row, msgHeaderValues.get(key));
                        if (value instanceof Date) {
                            //parse Date  to String
                            messageLoad.getHeaderMap().put(key, DateUtil.format((Date) value, tableConfig.getTimeFormat()));
                        } else {
                            messageLoad.getHeaderMap().put(key, value == null ? "" : value.toString());
                        }
                    }
                    messageLoad.getHeaderMap().put(DbTableIndicatorConfig.DB_TRACKING_ID, uuid);
                }
            }
        } catch (InvalidPropertyException e) {
            service.exit(3);
        }
        long time7 = Calendar.getInstance().getTimeInMillis();
        String xml = "";

        Map<String ,Object> payload = new HashMap<>();
        payload.put(tableConfig.getTableKey(),rowMaps.get(0));
        xml = transformationEngine.transformWithInputStream(tableConfig.getMapperFileStream(),payload
                ,TransformationMode.CUSTOMENTITY_TO_XML);
//        xml = transformationEngine.transform(tableConfig.getMapperFilePath(),payload, TransformationMode.CUSTOMENTITY_TO_XML);

//            xml = ConnectorUtilities.buildXmlDocWithTemplate(tableName, tableConfig, rowMaps, false);
//        } else {
//            xml = ConnectorUtilities.buildXmlDoc(tableName, tableConfig.trgtMapping, tableConfig.subTrgtMapping, rowMaps);
//        }

        String destinationPattern = tableConfig.getTopic();
        messageLoad.setXml(xml);
        messageLoad.setTopic(service.buildTargetTopic(tableName ,destinationPattern, rowMaps));

        messageLoad.setReady(true);
        service.reportTranslatedXml(index, messageLoad);

    }

    private void parseObject2String(DbTableIndicatorConfig tableConfig, Map<String, Object> beanMap , boolean isSub) {
        for(String k: beanMap.keySet()){
            Object o = beanMap.get(k);
            if(o instanceof Date){
                beanMap.put(k, DateUtil.format((Date) o, tableConfig.getTimeFormat()));
            }else if(o instanceof List && !isSub){
                List subs = (List) o;
                List subMapList  = new ArrayList<>();
                for(Object s:subs){
                    Map<String ,Object> subMap = BeanUtil.beanToMap(s);
                    Map<String,Object> treeBeanMap = new LinkedTreeMap<>();
                    treeBeanMap.putAll(subMap);
                    Map<String ,Object> subMapBox = new HashMap<>();
                    parseObject2String(tableConfig,treeBeanMap,true);
                    subMapBox.put(tableConfig.getChildTableKey(),treeBeanMap);
                    subMapList.add(subMapBox);
                }
                beanMap.put(k,subMapList);
            }else if(!(o instanceof String) && o !=null){
                beanMap.put(k,o.toString());
            }
        }
    }

    public void reset(List<Object> entityList, String destinationPattern) {
        this.entityList = entityList;
        currentHead.set(0);
    }
}
