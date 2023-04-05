package com.solace.connector.db.config;

import com.solace.connector.db.pull.service.XmlTemplate;
import com.solace.connector.utility.ConnectorUtilities;
import lombok.Data;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class DbTableIndicatorConfig {

    public static final String DOT = ".";
    public static final String LEFT_BRACE = "{";
    public static final String RIGHT_BRACE = "}";
    public static final String SLASH = "/";

    public static final String MAPPER_FILE = "MAPPER_FILE";
    public static final String BINDING = "BINDING";
    public static final String TOPIC = "TOPIC" ;
    public static final String LVQ_TOPIC = "LVQ_TOPIC" ;
    //parent-child relation
    public static final String DB_CHILD_TABLE = "DB_CHILD_TABLE";
    public static final String DB_CHILD_KEY = "DB_CHILD_KEY";
    public static final String DB_PARENT_KEY = "DB_PARENT_KEY";
    public static final String DB_PARENT_COLLECTION = "DB_PARENT_COLLECTION";

    //timestamp format
    public static final String DB_CONNECTOR_TIMESTAMP_FORMAT = "DB_CONNECTOR_TIMESTAMP_FORMAT";
    public static final String DB_MSG_HEADER_TIMESTAMP_FORMAT = "DB_MSG_HEADER_TIMESTAMP_FORMAT";
    //readIndicator
    public static final String DB_READ_INDICATOR_COLUMN = "DB_READ_INDICATOR_COLUMN";
    //indicator values
    public static final String DB_READ_INDICATOR_COLUMN_UPDATE_VALUE_INPROGRESS = "DB_READ_INDICATOR_COLUMN_UPDATE_VALUE_INPROGRESS";
    public static final String DB_READ_INDICATOR_COLUMN_UPDATE_VALUE_PROCESSED = "DB_READ_INDICATOR_COLUMN_UPDATE_VALUE_PROCESSED";
    public static final String DB_READ_INDICATOR_COLUMN_DEFAULT_VALUE = "DB_READ_INDICATOR_COLUMN_DEFAULT_VALUE";
    public static final String DB_READ_INDICATOR_COLUMN_FAILED_VALUE = "DB_READ_INDICATOR_COLUMN_FAILED_VALUE";
    //sequentialIndicator
    public static final String DB_READ_INDICATOR_ORDERBY = "DB_READ_RECORD_SEQUENTIAL_INDICATOR";
    public static final String DB_READ_RECORD_SEQUENTIAL_ORDER = "DB_READ_RECORD_SEQUENTIAL_ORDER";
    //if read from failed row
    public static final String DB_READ_INDICATOR_BRIDGE_FAILED_VALUE = "DB_READ_INDICATOR_BRIDGE_FAILED_VALUE";
    //tracking values
    public static final String DB_TRACKING_ID = "DB_TRACKING_ID";
    public static final String DB_READ_TIME = "DB_READ_TIME";

    private String tableKey =null;
    private String childTableKey = null;

    private String parentKeyName = null;
    private String childKeyName = null;
    private String parentCollectionName = null;

    private String tableName = null;
    private byte[] mapperFileStream = null;
    //    private String mapperFilePath = null;
    private String childTableName = null;
    private String tableKeyColumn = null;
    private String childKeyColumn = null;

    private String binding = null;
    private String topic = null;
    private String lvqTopic = null;
    private String flagIndicator = null;
    private String trackingIdIndicator = null;
    private String readTimeIndicator = null;
    private String flagIndicatorProcessedValue = null;
    private String flagIndicatorProcessingValue = null;
    private String flagIndicatorDefaultValue = null;
    private String flagIndicatorFailedValue = null;
    private String readIndicatorOrderBy = null;
    private String readIndicatorOrder = null;
    private String readIndicatorOrderByColumn = null;

    private boolean readIndicatorBridgeFailed = false;

    private String timeFormat="yyyy/MM/dd HH:mm:ss"; //default datetime format
    private String msgHeaderTimeFormat="yyyy/MM/dd HH:mm:ss.SSS" ;

    public final Map<String, String> scaledConfig = new LinkedHashMap<>();
//    public final Map<String, String> trgtMapping = new LinkedHashMap<>();
//    public final Map<String, Map<String, String>> subTrgtMapping = new LinkedHashMap<>();

    private Map<String, XmlTemplate> xmlTemplateMap = new HashMap<>();
    private Map<String, String> xmlSubTemplateMap = new HashMap<>();

    public DbTableIndicatorConfig(Map<String ,String > connectorConfig, String tableKey){

        for(String key : connectorConfig.keySet()){
            if(key.startsWith(tableKey+DOT)){
                scaledConfig.put(key.split("\\.")[1],connectorConfig.get(key));
            }else{
                scaledConfig.put(key,connectorConfig.get(key));
            }
        }

        this.tableKey = tableKey;
        this.mapperFileStream = ConnectorUtilities.getPropertiesStream("/mapper/" + tableKey + "_trgt_schema_mapper.yml");
//        this.mapperFilePath = scaledConfig.get(MAPPER_FILE);
        this.binding = scaledConfig.get(BINDING);
        this.topic=scaledConfig.get(TOPIC);
        this.lvqTopic=scaledConfig.get(LVQ_TOPIC);
        this.childTableKey = scaledConfig.get(DB_CHILD_TABLE);
        this.childKeyName = scaledConfig.get(DB_CHILD_KEY);
        this.parentKeyName = scaledConfig.get(DB_PARENT_KEY);
        this.parentCollectionName = scaledConfig.get(DB_PARENT_COLLECTION);

        this.flagIndicator = scaledConfig.get(DB_READ_INDICATOR_COLUMN);
        this.trackingIdIndicator = scaledConfig.get(DB_TRACKING_ID);
        this.readTimeIndicator = scaledConfig.get(DB_READ_TIME);
        this.flagIndicatorProcessingValue = scaledConfig.get(DB_READ_INDICATOR_COLUMN_UPDATE_VALUE_INPROGRESS);
        this.flagIndicatorProcessedValue = scaledConfig.get(DB_READ_INDICATOR_COLUMN_UPDATE_VALUE_PROCESSED);
        this.flagIndicatorDefaultValue = scaledConfig.get(DB_READ_INDICATOR_COLUMN_DEFAULT_VALUE);
        this.flagIndicatorFailedValue = scaledConfig.get(DB_READ_INDICATOR_COLUMN_FAILED_VALUE);
        this.readIndicatorOrderBy = scaledConfig.get(DB_READ_INDICATOR_ORDERBY);
        this.readIndicatorOrder = scaledConfig.get(DB_READ_RECORD_SEQUENTIAL_ORDER);
        this.readIndicatorBridgeFailed = Boolean.parseBoolean(scaledConfig.get(DB_READ_INDICATOR_BRIDGE_FAILED_VALUE));

        if (scaledConfig.containsKey(DB_CONNECTOR_TIMESTAMP_FORMAT)) {
            timeFormat =scaledConfig.get(DB_CONNECTOR_TIMESTAMP_FORMAT);
        }
        if (scaledConfig.containsKey(DB_MSG_HEADER_TIMESTAMP_FORMAT)) {
            msgHeaderTimeFormat = scaledConfig.get(DB_MSG_HEADER_TIMESTAMP_FORMAT);
        }
    }
}
