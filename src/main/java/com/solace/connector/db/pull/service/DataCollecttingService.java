package com.solace.connector.db.pull.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.solace.connector.db.config.DbTableIndicatorConfig;
import com.solace.connector.db.config.PullConfig;
import com.solace.connector.db.jdbc.DatabaseUtil;
import com.solace.connector.db.pull.client.ClientPushService;
import com.solace.connector.db.pull.lock.SynLock;
import com.solace.connector.exception.InvalidEntityException;
import com.solace.connector.exception.InvalidPropertyException;
import com.solace.connector.utility.ConnectorUtilities;
import com.solace.connector.utility.JpaBeanUtil;
import com.solace.spring.cloud.stream.binder.messaging.SolaceBinderHeaders;
import com.solace.spring.cloud.stream.binder.util.CorrelationData;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.stream.binder.BinderHeaders;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.integration.acks.AcknowledgmentCallback;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import reactor.util.annotation.Nullable;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;


import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.solace.connector.db.config.DbTableIndicatorConfig.*;

@SuppressWarnings("rawtypes")
@Service
@Slf4j
public class DataCollecttingService {

    public static final String MSG_HEADER_DATABASE_TIMESTAMP = "MSG_Database_Timestamp";
    public static final String MSG_HEADER_CONNECTOR_TIMESTAMP = "MSG_Connector_Timestamp";
    public static AtomicBoolean ready = null;
    public static AtomicBoolean isBlock = null;
    public static AtomicBoolean globalShutdown = new AtomicBoolean(false);

    static ObjectMapper mapper = new ObjectMapper();
    private final List<Object> emptyList = new ArrayList<>();

    public PullConfig getConfig() {
        return config;
    }

    @Resource
    PullConfig config;

    @Autowired
    StreamBridge streamBrideg;

    @Autowired
    DatabaseUtil databaseUtil;

    //    @Value("${spring.cloud.stream.bindings.dataCollectting-out-0.destination}")
//    String outputDestination;
//
//    @Value("${spring.cloud.stream.bindings.dataCollectting-out-0.content-type}")
    String outputContentType="application/x-java-serialized-object";

    @Autowired
    private ApplicationContext context;

    private static ApplicationContext staticContext;

    @Resource
    @Qualifier("watchDogPool")
    ThreadPoolTaskExecutor watchDogPool;

    @Resource
    @Qualifier("translatorPool")
    ThreadPoolTaskExecutor translatorExecutor;

    @Resource
    @Qualifier("correlationPool")
    ThreadPoolTaskExecutor correlationExecutor;

    private Map<String , String> taskMap = new ConcurrentHashMap<>();

    final static private Map<String, String> connectorConfig = new LinkedHashMap<>();

    public static AtomicBoolean isTest = null;
    public static AtomicBoolean currentLoopAllDone = new AtomicBoolean(false);
    public final static Map<String , DbTableIndicatorConfig> tableConfigMap = new ConcurrentHashMap<>();
    public final static Map<String , DataCollecttingContext> contextMap = new ConcurrentHashMap<>();

    private ThreadLocal<String> localTableName = new ThreadLocal<>();
    public ThreadLocal<DbTableIndicatorConfig> localTableConfig =new ThreadLocal<>();
    public ThreadLocal<DataCollecttingContext> localContext =new ThreadLocal<>();
    public ThreadLocal<SynLock> localSynLock =new ThreadLocal<>();

    public static final String DB_TABLE = "DB_TABLE";

    public static final String IS_TEST = "IS_TEST";

    //static load and check
    static {
        init("");
    }

    /**
     * Initialize Configuration and Check
     *
     * @param configPath Profile root directory
     */
    public static void init(String configPath) {
        connectorConfig.clear();
        connectorConfig.putAll(ConnectorUtilities.getPropertiesMapping(configPath + "/connector_config.properties"));
        if (isTest == null) {
            String isTestStr = connectorConfig.get(IS_TEST);
            if (!StrUtil.isEmpty(isTestStr)) {
                isTest = new AtomicBoolean(Boolean.parseBoolean(isTestStr));
                if (isTest.get()) {
                    ready = null;
                    return;
                }
            }
        }
        String tableName = connectorConfig.get(DB_TABLE);
        String[] tableKeys =tableName.split(",");
        for(String tableKey : tableKeys){
            DbTableIndicatorConfig tableConfig = new DbTableIndicatorConfig(connectorConfig,tableKey);
            ConnectorUtilities.checkIndicators(tableConfig);
            tableConfigMap.put(tableKey,tableConfig);

            ConnectorUtilities.checkEntity(connectorConfig.get(tableKey));
            ConnectorUtilities.checkEntityIndicators(connectorConfig.get(tableKey),tableConfig);

//            tableConfig.trgtMapping.putAll(ConnectorUtilities.getPropertiesMapping(configPath + SLASH +"mapper" + SLASH + tableKey + "_trgt_schema_mapper.properties"));
//
//            ConnectorUtilities.checkPropertiesMap(tableKey, connectorConfig.get(tableKey), tableConfig.trgtMapping);
//
//            for (String value : tableConfig.trgtMapping.values()) {
//                String[] vs = value.split("/");
//                value = vs[vs.length - 1];
//                if (value.matches(".*\\[.*\\]")) {
//                    String subEntityName = value.replace("[", "").replace("]", "");
//
//                    ConnectorUtilities.checkEntity(connectorConfig.get(subEntityName));
//                    tableConfig.setChildTableKey(subEntityName);
//                    Map<String, String> subMapping = new LinkedHashMap<>();
//                    subMapping.putAll(ConnectorUtilities.getPropertiesMapping(configPath + SLASH +"mapper" + SLASH + subEntityName + "_trgt_schema_mapper.properties"));
//                    ConnectorUtilities.checkPropertiesMap(subEntityName, connectorConfig.get(subEntityName), subMapping);
//
//                    tableConfig.subTrgtMapping.put(subEntityName, subMapping);
//                }
//            }
        }

        if (ready == null) {
            ready = new AtomicBoolean(true);
        }
        if (isBlock == null) {
            isBlock = new AtomicBoolean(false);
        }
    }

    private void prepareToExit(DataCollecttingContext localContext) throws InterruptedException {

        synchronized (contextMap){
            for(DataCollecttingContext context: contextMap.values()){
                //try to prevent all thread stop send and flush events
                context.getSendLock().set(true);
            }
            if(localContext.getExitPerforming().get()){
                return ;
            }else{
                localContext.getExitPerforming().set(true);
                while (localContext.getSuccesedMessageCount() != localContext.getFinishedList().size()){
                    Thread.sleep(1000l);
                    log.info("getSuccesedMessageCount : {} aginst getFinishedList : {}",localContext.getSuccesedMessageCount(),localContext.getFinishedList().size());
                }
                exit(localContext);

            }
        }

    }

    private void exit(DataCollecttingContext localContext) {
        synchronized (localContext.getFinishedList()){
            forceFlushEvents(localContext);
            reportThreadTaskDone(localContext.getTableConfig());
        }
        log.error("Exiting...");
        localContext.getDone().set(true);
        exit(2);
    }

    protected static void exit(int code) {

        if(!ready.get()){
            //block to wait for exit
            while (true){
                try {
                    Thread.sleep(1000l);
                    log.info("waiting for quit force");
                    System.exit(SpringApplication.exit(staticContext, () -> code));
                } catch (InterruptedException e) {
                }
            }
        }else{
            ready.set(false);
            globalShutdown.set(true);
            /*while(!currentLoopAllDone.get()){
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }*/
            log.info("Connector Quit Finished . All Threads shutdown. Thread ready :{}",ready.get());

            System.exit(SpringApplication.exit(staticContext, () -> code));
        }

    }

    public DbTableIndicatorConfig getLocalTableConfig() {
        return localTableConfig.get();
    }

    public DataCollecttingContext getLocalContext(){
        return localContext.get();
    }

    @Resource
    WatchDog dog;

    @Resource
    EntityXmlTranslator translator;

    @Bean
    public Consumer<ErrorMessage> myErrorHandler() {
        return v -> {
            log.info("error here");
            // send SMS notification code
        };
    }

    @Bean
    public Function<Message<SynLock>,Message<?>> lastValueQueueHandler() {
        return v -> {
            //disable auto ack
            AcknowledgmentCallback ackCallback = v.getHeaders().get("acknowledgmentCallback", AcknowledgmentCallback.class);
//            ackCallback.noAutoAck();
            log.info("Received lastValueQ Lock Status : {} " , v.getPayload().statusString());
            SynLock remoteSynLock = v.getPayload();
            String tableKey = remoteSynLock.getTableKey();

            if(remoteSynLock.getSynStatus().equals(SynLock.SynStatus.Trigger)){
                //normal trigger next step
                log.info("Trigged from Synlock [{}]...",remoteSynLock.getTableKey());
                localSynLock.set(remoteSynLock);

            }else if(remoteSynLock.getSynStatus().equals(SynLock.SynStatus.Recover)){
                log.warn("Restore from the last status . ");
                if(!contextMap.containsKey(tableKey)){
                    //restore the processingStatus to default status
                    DataCollecttingContext context = initLocalContext(tableKey);
                    DbTableIndicatorConfig localConfig = context.getTableConfig();

                    writeIndicator(context,remoteSynLock.getObjs());
                    remoteSynLock.setConnectorId(Integer.valueOf(this.hashCode()).toString());
                    remoteSynLock.getObjs().clear();
//                    remoteSynLock.setLastkey(Integer.toString(remoteSynLock.getObjs().hashCode()));
                    remoteSynLock.setSynStatus(SynLock.SynStatus.Trigger);
                    context.setSynLock(remoteSynLock);
                }else{
                    //DataCollecting is working
                    DataCollecttingContext localContext = DataCollecttingService.contextMap.get(tableKey);
                    SynLock localSynLock = localContext.getSynLock();
                    String localKey = localSynLock.getLastkey();
                    log.info("Lvq key : {}, local key : {}", remoteSynLock.getLastkey(), localKey);
//                    if (remoteSynLock.getLastkey().equals(localKey)) {
//                        log.info("Lvq lastKey same, bypass");
//                    } else {
//                        //not TODO 远程状态与本地运行时状态不同，增加处理
//                        log.error("SynLock status conflict");
//                        exit(localContext);
//                    }
                }
            }

//            ackCallback.acknowledge();

            MessageHeaderAccessor mha = new MessageHeaderAccessor();
//            mha.setHeader(BinderHeaders.TARGET_DESTINATION,tableConfig.getTriggerTopic());
            Message<?> message = MessageBuilder
                    .withPayload(tableKey)
                    .setHeaders(mha)
                    .build();

            return message;
        };
    }

    @Bean
    public Function<Message<?>,Message<?>> sendCurrentContext() {
        return v -> {

            log.info("tramsformed payload: {}" ,v.getPayload());

            MessageHeaderAccessor mha = new MessageHeaderAccessor();
            mha.setContentType(MimeType.valueOf(outputContentType));
            SynLock lock = localSynLock.get();
            Message<SynLock> message = MessageBuilder
                    .withPayload(lock)
                    .setHeaders(mha)
                    .build();
            return message;
        };
    }

    /**
     * Each table reading task works on a single thread
     * @return
     * @throws Exception
     */
    @Bean
    @NotNull
    public Function<Message<?>,Message<SynLock>> dataCollectingParellel() throws Exception {
        staticContext = context;
        if (enableBuffer()) {
            //start the watch dog
            dog.checkBufferedEvents();
        }

        return (m) -> {
            if(tableConfigMap.values().size()==1 && localTableConfig.get()==null)  {
                for(String k:tableConfigMap.keySet()){
                    localTableConfig.set(tableConfigMap.get(k));
                    break;
                }
            }
            if (ready == null) {
                return null;
            } else if (!ready.get()) {
                exit(3);
            }

            //prepare to start a database pull and push operation
            //check if message sending is being blocked
            if (isBlock.get()) {
                while (isBlock.get()) {
                    //stop start work and wait for working thread to exit
                    try {
                        Thread.sleep(1000l);
                        log.info("Connector is blocking here, will exit soon... ");
                    } catch (InterruptedException e) {
//                        e.printStackTrace();
                        exit(5);
                    }
                }
                return null;
            }
            String tableKey = localTableConfig.get().getTableKey();
            prepaerToPull(tableKey);

//          log.info("main Thread {} done",Thread.currentThread().toString());

            //cleaning works after pulling data finished
//            contextMap.remove(tableKey);
            try {
                Thread.sleep(config.getTriggerInterval());
            } catch (InterruptedException e) {
            }
            SynLock synLock = localSynLock.get();
//            synLock.setSynStatus(SynLock.SynStatus.Trigger);
//            synLock.getObjs().clear();
//            synLock.setLastkey(Integer.toString(synLock.getObjs().hashCode()));
            Message<SynLock> message = MessageBuilder
                    .withPayload(synLock)
                    .build();
            synchronized (ready){
                if(!ready.get()){
                    return null;
                }
            }
            log.info("Regular sending default synLock {}",synLock.statusString());
            return message;
//            return null;
        };
    }

    public void reportThreadTaskDone(DbTableIndicatorConfig tableConfig){
        log.info("pulling for [{}] done.",tableConfig.getTableKey());
        synchronized (contextMap){
            boolean done = true;
            for(DataCollecttingContext context : contextMap.values()){
                done = done && context.getDone().get();
            }
            currentLoopAllDone.set(done);
        }
    }

    public boolean prepaerToPull(String tableKey) {
        if(!contextMap.containsKey(tableKey)){
            initLocalContext(tableKey);
        }

        log.info("Thread {} working for {}...",Thread.currentThread().toString(),localTableName.get());
        getLocalContext().initBenchmarkTime();
        getLocalContext().getDone().set(false);
        queryNsend(tableKey);
        getLocalContext().getForceFlushFlag().set(false);

        while (!getLocalContext().getDone().get()){
            //wait for CorrelationData threads come back
            try {
                Thread.sleep(10l);
//                log.info("blocking in prepaerToPull");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if(!ready.get()){
                break;
            }
        }
        return true;
    }

    public DataCollecttingContext initLocalContext(String tableKey) {
        localTableName.set(tableKey);
        DbTableIndicatorConfig config = tableConfigMap.get(tableKey);
        localTableConfig.set(config);
        DataCollecttingContext context = new DataCollecttingContext(config);
        localContext.set(context);
        contextMap.put(tableKey,context);
//        log.info("contextMap@{} loading [{}]",contextMap.keySet(),tableKey);
        if (localSynLock.get() == null) {
            SynLock synLock = new SynLock(Integer.valueOf(this.hashCode()).toString());
            synLock.setConnectorId(Integer.valueOf(this.hashCode()).toString());
            synLock.setSynStatus(SynLock.SynStatus.Recover);
            synLock.setLastkey("none");
            synLock.setTableKey(tableKey);
            log.info("Init a new Synlock");
            localSynLock.set(synLock);
            context.setSynLock(synLock);
        }else{
            context.setSynLock(localSynLock.get());
        }

        return context;
    }

    public void queryNsend(String tableName) {
        //for more header message
        MessageHeaderAccessor mha = new MessageHeaderAccessor();

        Date dbReadDate = databaseUtil.queryDatabaseTime();
        Date connectorDate = Calendar.getInstance().getTime();


        if (config.isEnableDatabaseTimestamp()) {
            mha.setHeader(MSG_HEADER_DATABASE_TIMESTAMP, DateUtil.format(dbReadDate, getLocalTableConfig().getMsgHeaderTimeFormat()));
        }
        if (config.isEnableConnectorTimestamp()) {
            mha.setHeader(MSG_HEADER_CONNECTOR_TIMESTAMP, DateUtil.format(connectorDate, getLocalTableConfig().getMsgHeaderTimeFormat()));
        }

        List<Object> list = queryChanges(tableName, dbReadDate);
        if (list.size() == 0) {
            log.info("No Events to pull , thread for [{}] done ",tableName);
            getLocalContext().getDone().set(true);
            reportThreadTaskDone(getLocalTableConfig());
            return;
        }

        getLocalContext().getTotalList().addAll(list);

        if (getLocalTableConfig().getTopic().contains(LEFT_BRACE) && getLocalTableConfig().getTopic().contains(RIGHT_BRACE)) {
            getLocalContext().setDynamicDestination( true);
            if (config.getSendBatchSize() > 1) {
                log.error("Both DynamicDestination and Multiple Message Queue (sendBatchSize > 1) was configured ,Check the application.yml file !");
                exit(getLocalContext());
            }
        }

        try {
            getLocalContext().setTranslatorStartTime();
            translator.reset(list, getLocalTableConfig().getTopic());
            List<MessageLoad> xmlResult= getLocalContext().getXmlResult();
            int index = 0;
            for (int i = 0, j = config.getSendBatchSize(); i < list.size(); i = i + config.getSendBatchSize(), j = j + config.getSendBatchSize()) {
                if (j > list.size()) {
                    j = list.size();
                }
                MessageLoad m = new MessageLoad(list.subList(i, j));
                m.setIndex(index++);
                m.setStart(i);
                m.setEnd(j);
                xmlResult.add(m);
            }


            if (xmlResult.size() > 0) {
                // generate template as soon as possible
                final DataCollecttingContext localContext = getLocalContext();
                for(MessageLoad ml :xmlResult){
                    Runnable task = () ->{
                        try {
                            translator.translate(localContext, ml);
                        }catch (Exception e){
                            log.error("ERROR in translator Thread.",e);
                            exit(localContext);
                        }
                    };

                    translatorExecutor.execute(task);
                }

                log.info("Start sending to Solace topic... ");
                getLocalContext().setSendStartTime();
//        sendMessages(outputDestination, list, mha);
                sendMessages(tableName,mha);
                localSynLock.get().setTestPayload(xmlResult);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected void reportTranslatedXml(int index, MessageLoad messageLoad) {
        log.debug("Finished Xml Index Number : {}", index);
    }

    /**
     * query failed records first before default logic
     *
     * @param tableName
     * @param dbReadDate
     * @return
     */
    private List<Object> queryChanges(String tableName, Date dbReadDate) {
        List<Object> ret = new CopyOnWriteArrayList<>();
        if(globalShutdown.get()){
            return ret;
        }
        getLocalContext().reset();
        DbTableIndicatorConfig localConfig = getLocalTableConfig();
        //Query data with fail indicators first if configured
        if (getLocalTableConfig().isReadIndicatorBridgeFailed()) {
            ret = queryChanges(tableName, localConfig.getFlagIndicatorFailedValue(), dbReadDate);
        }
        //query default if no fail data , when fail data is not empty query default indicator next round
        if (ret.size() == 0) {
            ret = queryChanges(tableName, localConfig.getFlagIndicatorDefaultValue(), dbReadDate);
        }
        getLocalContext().setHandleMessageCount( ret.size());

        return ret;
    }

    private List<Object> queryChanges(String tableName, String indicatorValue, Date dbReadDate) {

        List<Object> objects = new ArrayList<>();
        try {
            String jpaEntity = connectorConfig.get(tableName);
            Object obj = JpaBeanUtil.buildEntityObj(jpaEntity);
            DbTableIndicatorConfig localConfig = getLocalTableConfig();
            JpaBeanUtil.invokeSetter(obj, localConfig.getFlagIndicator(), indicatorValue);

            Example<Object> example = Example.of(obj);

            JpaRepository repo = (JpaRepository) context.getBean(Class.forName(jpaEntity + "Repo"));

            //order by multi keywords
            Sort.Direction order = Sort.Direction.ASC;
            if (localConfig.getReadIndicatorOrder().toLowerCase(Locale.ROOT).equals("desc")) {
                order = Sort.Direction.DESC;
            }
            objects.addAll(repo.findAll(example,
                    PageRequest.of(0, config.getQueryMax(),
                            Sort.by(order, localConfig.getReadIndicatorOrderBy().split(",")))).getContent());

            if(StrUtil.isNotEmpty(localConfig.getChildTableKey()) && objects.size()>0){
                String childEntity = connectorConfig.get(localConfig.getChildTableKey());
                Object child = JpaBeanUtil.buildEntityObj(childEntity);

                Table entityConfig = (Table) obj.getClass().getAnnotation(Table.class);
                String dbTableName = entityConfig.name();

                Table childEntityConfig = (Table) child.getClass().getAnnotation(Table.class);
                String dbChildTableName = childEntityConfig.name();
                localConfig.setTableName(dbTableName);
                localConfig.setChildTableName(dbChildTableName);

                localConfig.setTableKeyColumn(JpaBeanUtil.getPropertyColumnName(obj.getClass(), localConfig.getParentKeyName()));
                localConfig.setChildKeyColumn(JpaBeanUtil.getPropertyColumnName(child.getClass(), localConfig.getChildKeyName()));
                String orderBy = localConfig.getReadIndicatorOrderBy().contains(".")?localConfig.getReadIndicatorOrderBy().split("\\.")[1]:localConfig.getReadIndicatorOrderBy();
                localConfig.setReadIndicatorOrderByColumn(JpaBeanUtil.getPropertyColumnName(obj.getClass(), orderBy));

                List<Map<String,Object>> children =  databaseUtil.queryForChidlren(localConfig,JpaBeanUtil.invokeGetter(objects.get(0),orderBy),
                        JpaBeanUtil.invokeGetter(objects.get(objects.size()-1),orderBy));
                log.info("children size: [{}]",children.size());
                Map <String,Object > parentMap = new HashMap<>();
                for(Object p:objects){
                    parentMap.put(JpaBeanUtil.invokeGetter(p,localConfig.getParentKeyName()).toString(),p);
                    JpaBeanUtil.invokeSetter(p,localConfig.getParentCollectionName(),new ArrayList<>());
                }
                for(Map<String,Object> c:children){
                    String key = c.get(localConfig.getChildKeyColumn().toUpperCase(Locale.ROOT)).toString();
                    Object p=parentMap.get(key);
                    if(p==null){
                        log.error("Parent entity with join key [{}] NOT Found, check the query detail!.",key);
                        exit(2);
                    }
                    ((List)(JpaBeanUtil.invokeGetter(p,localConfig.getParentCollectionName()))).add(c);
                }
            }

            log.info("===========query max : [{}] =============",config.getQueryMax());
            if(objects.size()>0){
                getLocalContext().setSetpStartTime();
            }
            writeBackProcessingIndicator(tableName,dbReadDate, objects);

            //temporarily mark objets as defaultValue
            for (Object o : objects) {
                JpaBeanUtil.invokeSetter(o, localConfig.getFlagIndicator(), localConfig.getFlagIndicatorDefaultValue());
            }
            if (objects.size() > 0) {
                log.info("{} source data rows marked as being processed ", objects.size());
            }
        } catch (ClassNotFoundException e) {
            log.error("ERROR in queryChanges, Check The Entity and EntiryRepo Configuration! ", e);
            exit(getLocalContext());
            return null;
        } catch (InvalidPropertyException | InvalidEntityException e) {
            exit(getLocalContext());
        } catch (Exception e) {
            log.error("ERROR in queryChanges", e);
            exit(getLocalContext());
        }
        return objects;
    }

    private void writeBackProcessingIndicator(String tableName,Date dbReadDate, List<Object> objects) {
        //old implements using JPA
//        saveReadIndicator(objects, flagIndicatorProcessingValue, true, dbReadDate);
        // use plain sql with params
        try {
            if (objects.size() == 0) {
                return;
            }
            String orderColumn ="";
            DbTableIndicatorConfig localConfig = getLocalTableConfig();
            String readIndicatorOrderBy = localConfig.getReadIndicatorOrderBy();
            String readIndicatorOrder = localConfig.getReadIndicatorOrder();
            if (readIndicatorOrderBy.contains(",")) {
                log.error("multiple order by not supported temporally");
                exit(getLocalContext());
            }else{
                orderColumn = readIndicatorOrderBy.split("\\.").length==2?readIndicatorOrderBy.split("\\.")[1]:readIndicatorOrderBy;
            }
            Class entityClass = Class.forName(connectorConfig.get(tableName));
            Table entityConfig = (Table) entityClass.getAnnotation(Table.class);

            String dbTableName = entityConfig.name();
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("indicatorValue", localConfig.getFlagIndicatorProcessingValue());
            if("desc".equalsIgnoreCase(readIndicatorOrder)){
                params.put("minValue", JpaBeanUtil.invokeGetter(objects.get(objects.size() - 1), orderColumn));
                params.put("maxValue", JpaBeanUtil.invokeGetter(objects.get(0), orderColumn));
            } else {
                params.put("minValue", JpaBeanUtil.invokeGetter(objects.get(0), orderColumn));
                params.put("maxValue", JpaBeanUtil.invokeGetter(objects.get(objects.size() - 1), orderColumn));
            }
            params.put("readTime", dbReadDate);

            String indicatorColumnName = JpaBeanUtil.getPropertyColumnName(entityClass, localConfig.getFlagIndicator());
            String orderByColumnName = JpaBeanUtil.getPropertyColumnName(entityClass, orderColumn);
            String readTimeOrderByColumnName = JpaBeanUtil.getPropertyColumnName(entityClass, localConfig.getReadTimeIndicator());

            for(Object o : objects){
                JpaBeanUtil.invokeSetter(o, localConfig.getFlagIndicator(), localConfig.getFlagIndicatorProcessingValue());
                JpaBeanUtil.invokeSetter(o, localConfig.getReadTimeIndicator(),dbReadDate);
            }

            cacheDdl(getLocalContext(),objects);
            sendLVQ(getLocalContext());
            int updateCount = databaseUtil.updateBatchIndicator(dbTableName, indicatorColumnName, orderByColumnName, readTimeOrderByColumnName, params);

            if (objects.size() != updateCount) {
                log.error("writeBackProcessingIndicator number: {} NOT equals event number {}", updateCount, objects.size());
                exit(getLocalContext());
            }
            cleaCacheDdl(getLocalContext());
        } catch (ClassNotFoundException | InvalidPropertyException e) {
            e.printStackTrace();
            exit(getLocalContext());
        }

    }

    private void writeBackProcessedIndicator(DataCollecttingContext localContext , List<Object> objects , int index, boolean checkBuffer) {
        //old implements using JPA
//        saveReadIndicator(objects, flagIndicatorProcessingValue, false, null);
        // use plain sql with params
        try {
            DbTableIndicatorConfig localConfig = localContext.getTableConfig();
            String tableName =localConfig.getTableKey();
            if (objects.size() > 1) {
                saveReadIndicator(objects, localConfig.getFlagIndicatorProcessedValue(), false, null);
            } else {
                String orderColumn ="";
                String readIndicatorOrderBy = localConfig.getReadIndicatorOrderBy();
                if (readIndicatorOrderBy.contains(",")) {
                    log.error("multiple order by not supported temporally");
                    exit(localContext);
                }else{
                    orderColumn = readIndicatorOrderBy.split("\\.").length==2?readIndicatorOrderBy.split("\\.")[1]:readIndicatorOrderBy;
                }
                Class entityClass = Class.forName(connectorConfig.get(tableName));
                Table entityConfig = (Table) entityClass.getAnnotation(Table.class);

                String dbTableName = entityConfig.name();
                Map<String, Object> params = new HashMap<>();
                params.put("indicatorValue", localConfig.getFlagIndicatorProcessedValue());
                params.put("orderValue", JpaBeanUtil.invokeGetter(objects.get(0), orderColumn));
                params.put("trackingValue", JpaBeanUtil.invokeGetter(objects.get(0), localConfig.getTrackingIdIndicator()));

                String indicatorColumnName = JpaBeanUtil.getPropertyColumnName(entityClass, localConfig.getFlagIndicator());
                String orderByColumnName = JpaBeanUtil.getPropertyColumnName(entityClass, orderColumn);
                String trackingColumnName = JpaBeanUtil.getPropertyColumnName(entityClass, localConfig.getTrackingIdIndicator());

                if (config.isEnableBufferedJpaBatch() && checkBuffer) {
                    //put buffer pool
                    List<Map<String, Object>> batchParams=localContext.getBatchParams();
                    List<Object> finishedList= localContext.getFinishedList();
                    List<Object> totalList= localContext.getTotalList();
                    synchronized (batchParams) {
                        batchParams.add(params);
                    }

                    JpaBeanUtil.invokeSetter(objects.get(0), localConfig.getFlagIndicator(), localConfig.getFlagIndicatorProcessedValue());

                    finishedList.addAll(objects);
                    //execute batch update when all events done
                    if (finishedList.size() == totalList.size()) {
                        localContext.setFlushStartTime();
                        cacheDdl(localContext,finishedList);
                        Map<String, Object>[] batchParamsArray = new Map[batchParams.size()];
                        sendLVQ(localContext);
//                        exit(999);
                        int[] batchStatus = databaseUtil.batchUpdateSingleIndicator(dbTableName, indicatorColumnName, orderByColumnName, trackingColumnName, batchParams.toArray(batchParamsArray));
                        // return -2 for oracle driver means successfully updated
                        for(int status: batchStatus){
                            if(status!=-2 && status!=1){
                                log.error("writeBackProcessedIndicator status: {} NOT equals -2 or 1 ,which means success .", status);
                                exit(localContext);
                            }
                        }
                        cleaCacheDdl(localContext);
                        localContext.getFinishedList().clear();
                        sendLVQ(localContext);
                        localContext.getDone().set(true);
                        localContext.reportBenchmarkTime(this.config.isEnableBenchmarkInfo());
                        localContext.reset();
                        reportThreadTaskDone(localContext.getTableConfig());
                    }
                } else {
                    cacheDdl(localContext,objects);
                    localContext.setFlushStartTime();
                    int updateCount = databaseUtil.updateSingleIndicator(dbTableName, indicatorColumnName, orderByColumnName, trackingColumnName, params);
                    if (objects.size() != updateCount) {
                        log.error("writeBackProcessedIndicator number: {} NOT equals event number {}", updateCount, objects.size());
                        exit(localContext);
                    }
                    cleaCacheDdl(localContext);
                }
            }
        } catch (ClassNotFoundException | InvalidPropertyException e) {
            e.printStackTrace();
            exit(localContext);
        }
    }

    private void writeIndicator(DataCollecttingContext localContext, List<Object> objects) {
        try {
            DbTableIndicatorConfig tableConfig = localContext.getTableConfig();
            String tableName =tableConfig.getTableKey();

            if (objects.size() == 0) {
                return;
            }
            String orderColumn ="";
            String readIndicatorOrderBy = tableConfig.getReadIndicatorOrderBy();
            String flagIndicator= tableConfig.getFlagIndicator();
            String trackingIdIndicator = tableConfig.getTrackingIdIndicator();
            String readTimeIndicator = tableConfig.getReadTimeIndicator();

            if (readIndicatorOrderBy.contains(",")) {
                log.error("sequence of multiple columns is not supported currently, need to be enhanced.");
                exit(localContext);
            }else{
                orderColumn = readIndicatorOrderBy.split("\\.").length==2?readIndicatorOrderBy.split("\\.")[1]:readIndicatorOrderBy;
            }
            Class entityClass = Class.forName(connectorConfig.get(tableName));
            Table entityConfig = (Table) entityClass.getAnnotation(Table.class);

            String dbTableName = entityConfig.name();
            String indicatorColumnName = JpaBeanUtil.getPropertyColumnName(entityClass, flagIndicator);
            String orderByColumnName = JpaBeanUtil.getPropertyColumnName(entityClass, orderColumn);
            String trackingColumnName = JpaBeanUtil.getPropertyColumnName(entityClass, trackingIdIndicator);
            String readTimeColumnName = JpaBeanUtil.getPropertyColumnName(entityClass, readTimeIndicator);
            List<Map<String, Object>> batchParams = new ArrayList<>();
            for(Object o : objects){
                Map<String, Object> params = new HashMap<>();
                params.put("indicatorValue", JpaBeanUtil.invokeGetter(o, flagIndicator));
                params.put("orderValue", JpaBeanUtil.invokeGetter(o, orderColumn));
                params.put("readTime", JpaBeanUtil.invokeGetter(o, readTimeIndicator));
                params.put("trackingValue", JpaBeanUtil.invokeGetter(o, trackingIdIndicator));
                batchParams.add(params);
            }
            Map<String, Object>[] batchParamsArray = new Map[objects.size()];
            //execute batch update when all events done
            localContext.setFlushStartTime();
            int[] batchStatus = databaseUtil.batchUpdateSingleIndicator(dbTableName, indicatorColumnName, orderByColumnName, trackingColumnName, readTimeColumnName, batchParams.toArray(batchParamsArray));
            // return -2 for oracle driver means successfully updated
            for(int status: batchStatus){
                if(status!=-2 && status!=1){
                    log.error("writeBackProcessedIndicator status: {} NOT equals -2 or 1 ,which means success .", status);
                    exit(localContext);
                }
            }
        } catch (ClassNotFoundException | InvalidPropertyException e) {
            e.printStackTrace();
        }
    }

    /**
     * building destination from the given pattern
     * <br/> eg. DXB/{COL:customer_id}/{COL:name}/{COL:address}/SGP/{DB_Table}
     * <br/> <bold>COL:column_name</bold> will be replaced by column value
     * <br/> <bold>DB_Table</bold> will be replaced by DB_Table property in connector_config.properties
     *
     * @param destinationPattern given pattern
     * @return
     */
    protected String buildTargetTopic(String tableName ,String destinationPattern, List<Map<String, Object>> rowMaps) {

//        if(outputDestination.split("/").length < 3){
//            log.error("connector destination: {} dos not match the pattern part1/part2/part3/... " , destinationPattern);
//            exit();
//        }

        Map<String, Object> oneRow = null;
        for (Map<String, Object> row : rowMaps) {
            oneRow = row;
        }
        StringBuilder ret = new StringBuilder();
        if (oneRow != null) {
            String[] pattern = destinationPattern.split(SLASH);
            for (String s : pattern) {
                if (s.startsWith("{COL:")) {
                    Object val = oneRow.get(s.substring(5, s.length() - 1));
                    if (val == null) {
                        log.error("{COL:{}} not found in Entity ! Connector Will Exit !", s.substring(5, s.length() - 1));
                        return null;
                    }
                    s = val.toString();
                } else if (s.equals("{DB_Table}")) {
                    s = tableName;
                }
                ret.append(s).append(SLASH);
            }
        }

        String targetTopic = ret.toString();

        return targetTopic.substring(0, targetTopic.length() - 1);
    }

    /**
     * send messages to solace and handle the status
     * @param mha
     */
    private void sendMessages(String tableName ,MessageHeaderAccessor mha) {

        long time1 = Calendar.getInstance().getTimeInMillis();
        final long[] time6 = {time1};
        getLocalContext().setSendWaitingTime(0);
        int loopCount = 0;
        List<MessageLoad> xmlResult = getLocalContext().getXmlResult();
        for (int i = 0; i < xmlResult.size(); i++) {
            long time3 = Calendar.getInstance().getTimeInMillis();
            if(globalShutdown.get()){
                break;
            }
            MessageLoad sendObject = null;
            if (i < xmlResult.size()) {
                sendObject = xmlResult.get(i);
                final DataCollecttingContext currentLocalContext = getLocalContext();
                final DbTableIndicatorConfig currentLocalConfig = getLocalTableConfig();
                if (!sendObject.isReady() ) {
                    i--;
                    loopCount++;
                    try {
                        if(globalShutdown.get()){
                            break;
                        }
                        //snap for translating xml
                        Thread.sleep(1000l);
                        log.info("blocking... loop");
                    } catch (InterruptedException e) {
//                        e.printStackTrace();
                    }
                    getLocalContext().addSendWaitingTime();
                    continue;
                } else {
                    try {
                        if (loopCount > 0) {
//                            log.info("==== LOOP for {} times waiting for xmlResultIndex {}", loopCount, i);
                            loopCount = 0;
                        }
                        if(currentLocalContext.getSendLock().get()){
                            prepareToExit(currentLocalContext);
                            return ;
                        }
                        String xml = sendObject.getXml();
                        String topic = sendObject.getTopic();

                        if (StrUtil.isEmpty(xml)) {
                            log.error("Empty Xml !");
                            exit(currentLocalContext);
                        }

                        log.debug("message payload : {}", xml);

                        for (String key : sendObject.getHeaderMap().keySet()) {
                            mha.setHeader(key, sendObject.getHeaderMap().get(key));
                        }

                        CorrelationData correlationData = new CorrelationData();
                        correlationData.getFuture().addCallback(this::onSuccess, this::onFailure);
                        mha.setHeader(SolaceBinderHeaders.CONFIRM_CORRELATION, correlationData);
                        mha.setHeader(BinderHeaders.TARGET_DESTINATION,currentLocalConfig.getTopic());
                        mha.setHeader("contentType","plain/text");
                        Message<String> message = MessageBuilder
                                .withPayload(xml)
                                .setHeaders(mha)
                                .build();

                        if (topic == null) {
                            log.error("Empty topic ！");
                            exit(currentLocalContext);
                            return;
                        }

                        if (currentLocalContext.getHandleMessageCount() > 0) {
                            currentLocalContext.setStartSendingTime();
                        }

                        AtomicBoolean sendLock = currentLocalContext.getSendLock();
                        List<Object> finishedList= currentLocalContext.getFinishedList();
                        List<Object> totalList= currentLocalContext.getTotalList();
                        final AtomicBoolean successed = new AtomicBoolean(false);
                        successed.set(streamBrideg.send(currentLocalConfig.getBinding(), message, MimeType.valueOf(outputContentType)));
                        MessageLoad finalSendObject = sendObject;
                        int finalI = i;

                        Runnable task = () ->{
                            Future f = correlationData.getFuture();
//                            if (f != null) {
                            try {
                                f.get();
                                if(currentLocalContext.getSuccesedMessageCount() <=finalI){
                                    currentLocalContext.setSuccesedMessageCount(finalI +1) ;
                                }
                                writeBackProcessedIndicator(currentLocalContext,finalSendObject.getObjectList(),finalI,true);
                                if (isTest!=null && isTest.get()) {
                                    long time5 = Calendar.getInstance().getTimeInMillis();
                                    if ((finishedList.size() + 1) % 100 == 0) {
                                        log.info("BenchMark: -->  Send {} events ,this 100 events using {} milliseconds ", finishedList.size() + 1, (time5 - time6[0]));
                                        time6[0] = time5;
                                    }
                                    if ((finishedList.size() + 1) == totalList.size()) {
                                        log.info("BenchMark: -->  Finish Sending {} messages using {} milliseconds ，Waiting for database indicator flush ", finishedList.size() + 1, (time5 - time1));
                                    }
                                }
                            } catch (Exception e) {
                                successed.set(false);
                                log.error("message send error: ", e);
                                if (config.isEnableRetry() && !isBlock.get()) {
                                    int retryCount = 0;
                                    while (!successed.get() && retryCount < config.getRetryCount()) {
                                        try {
                                            Thread.sleep(config.getRetryInterval() * 1000l);
                                            successed.set(streamBrideg.send(topic, message, MimeType.valueOf(outputContentType)));
                                            correlationData.getFuture().get(5L, TimeUnit.SECONDS);
                                        } catch (Exception ex) {
                                            successed.set(false);
                                            isBlock.set(true);
                                            log.error("retry {}/{} message send error: {}", ++retryCount,config.getRetryCount(),ex.getMessage());
                                        }
                                    }
                                }
                                isBlock.set(true);
                                try {
                                    sendLock.set(true);
                                    currentLocalContext.getSendLock().set(true);
                                    prepareToExit(currentLocalContext);
                                } catch (InterruptedException ex) {
                                    ex.printStackTrace();
                                    DataCollecttingService.this.exit(3);
                                }
                            }
//                            }
                        };

                        correlationExecutor.execute(task);

                    } catch (Exception e) {
                        isBlock.set(true);
                        forceFlushEvents(currentLocalContext);
                        e.printStackTrace();
                        exit(currentLocalContext);
                    }
                    if (isTest!=null && isTest.get()) {
                        long time2 = Calendar.getInstance().getTimeInMillis();
                        if (time2 - time3 > 10) {
                            log.info("BenchMark: --> [===SLOW SEND===] Send one using {} milliseconds ", (time2 - time3));
                        }
                    }
                }
            }
        }
        getLocalContext().setWaitAckTime();
    }

    public void onSuccess(Void result) {
        log.debug("-------------------message send success callback----------------");
    }

    public void onFailure(Throwable ex) {
        log.error("------------------------message send failure callback------------------");
        exit(3);
    }

    private boolean forceFlushEvents(DataCollecttingContext localContext) {

        if(localContext.getForceFlushFlag().get()){
            return false;
        }else {
            sendLVQ(localContext);
            localContext.getForceFlushFlag().set(true);
            if(localContext.getForceFlushFlag().get()) {
                List<Object> finishedList = localContext.getFinishedList();
                List<Object> totalList = localContext.getTotalList();
                if (finishedList.size() != totalList.size()) {
                    // mark as processed
                    if (finishedList.size() != 0) {
                        writeIndicator(localContext,finishedList);
                    }

                    localContext.setStartSendingTime(-1);
                    List<Object> subList = totalList.subList(finishedList.size(), finishedList.size() + 1);

                    try {
                        writeBackFailed(localContext,subList);
                        log.info("writeBackFailed size : {}", subList.size());
                        subList = totalList.subList(finishedList.size() + 1, totalList.size());
                        writeBackDefault(localContext,subList);
                        log.info("writeBackDefault size : {}", subList.size());
                    } catch (InvalidPropertyException e) {
                        e.printStackTrace();
                        exit(localContext);
                    }
                    log.info("======force flush buffered events======> success: {} , fail: {} ,leave default: {}", finishedList.size(), 1, totalList.size() - finishedList.size() - 1);
                    return true;
                }
            }
        }
        return false;
    }

    private void writeBackDefault(DataCollecttingContext localContext, List<Object> subList) throws InvalidPropertyException {
        DbTableIndicatorConfig tableConfig = localContext.getTableConfig();
        for(Object o : subList){
            JpaBeanUtil.invokeSetter(o,tableConfig.getFlagIndicator(),tableConfig.getFlagIndicatorDefaultValue());
            JpaBeanUtil.invokeSetter(o,tableConfig.getTrackingIdIndicator(),null);
            JpaBeanUtil.invokeSetter(o,tableConfig.getReadTimeIndicator(),null);
        }

        writeIndicator(localContext,subList);
    }

    private void writeBackFailed(DataCollecttingContext localContext, List<Object> subList) throws InvalidPropertyException {
        DbTableIndicatorConfig tableConfig = localContext.getTableConfig();
        String flagIndicator= tableConfig.getFlagIndicator();
        for(Object o : subList){
            JpaBeanUtil.invokeSetter(o,flagIndicator,tableConfig.getFlagIndicatorFailedValue());
            JpaBeanUtil.invokeSetter(o,tableConfig.getTrackingIdIndicator(),null);
            JpaBeanUtil.invokeSetter(o,tableConfig.getReadTimeIndicator(),null);
        }
        writeIndicator(localContext,subList);
    }

    /**
     * JPA way for update indicator is slow
     */
    @Deprecated
    private void saveReadIndicator(List<Object> sub, String indicatorValue, boolean immediate, @Nullable Date dbReadDate) {
        if (sub.size() == 0) {
            return;
        }
        DbTableIndicatorConfig localConfig = getLocalTableConfig();
        for (Object o : sub) {
            try {

                if (dbReadDate != null && indicatorValue.equals(localConfig.getFlagIndicatorProcessingValue())) {
                    JpaBeanUtil.invokeSetter(o, localConfig.getReadTimeIndicator(), dbReadDate);
                }

                JpaBeanUtil.invokeSetter(o, localConfig.getFlagIndicator(), indicatorValue);
            } catch (InvalidPropertyException e) {
                exit(3);
            }
        }
        if (immediate) {
            flushIndicators(getLocalContext(),sub);
        } else {
            bufferedSaveReadIndicator(sub);
        }
    }

    private void bufferedSaveReadIndicator(List<Object> sub) {

        if (enableBuffer()) {
            List<Object> finishedList= getLocalContext().getFinishedList();
            List<Object> totalList= getLocalContext().getTotalList();
            if (!ready.get()) {
                return;
            }
            // buffered jpa batch is only available when sendBatchSize = 1
            finishedList.addAll(sub);
//            log.info("{} events buffering", sub.size());
            if (finishedList.size() == getLocalContext().getHandleMessageCount()) {
                getLocalContext().setFlushStartTime();
                flushIndicators(getLocalContext(),finishedList);
                log.info("{} buffered events flushed", finishedList.size());
                if (isTest != null && isTest.get()) {
                    long time = Calendar.getInstance().getTimeInMillis();
                    log.info("BenchMark: --> Finish pulling {} events using {} seconds ", finishedList.size(), (time - getLocalContext().getPollerStartTime()) / 1000d);
                }
                finishedList.clear();
                getLocalContext().setStartSendingTime(-1);
                getLocalContext().setHandleMessageCount(0);
            }
        } else {
            getLocalContext().setFlushStartTime();
            flushIndicators(getLocalContext(),sub);
        }

    }

    private boolean enableBuffer() {
        return config.isEnableBufferedJpaBatch() && config.getSendBatchSize() == 1;
    }

    public void checkBufferedEvents() {

        for(DataCollecttingContext context : contextMap.values()){
            if(!context.checkBufferedEvents(config.getBufferWaitSeconds() * 1000L) ){
                if (forceFlushEvents(context)) {
                    exit(context);
                }
            }
        }

    }

    @Deprecated
    private void flushIndicators(DataCollecttingContext localContext, List<Object> sub) {
        if (!ready.get()) {
            return;
        }
        SynLock synLock = localContext.getSynLock();
        List<Object> lastPerformedList = localContext.getLastPerformedList();
        List<Object> totalList = localContext.getTotalList();
        JpaRepository repo = localContext.getRepo();
        this.postConstruct(localTableName.get() );
        try {
            if (localContext.isJpaLocked()) {
                log.warn("{} events have been blocked ， JDBC Connection hanging, keep waiting ... ", lastPerformedList.size());
//                lastPerformedList.forEach(o-> {
//                    System.out.print(invokeGetter(o,flagIndicator));
//                });
//                System.out.println();

                // perform lastPerformedList first
                if (lastPerformedList.size() > 0) {
                    cacheDdl(localContext,sub);
                    writeIndicator(localContext,lastPerformedList);
                    localContext.unLockJpa();
                    cleaCacheDdl(localContext);
                }
            } else {
                //use the lastPerformedList for database disconnect reasons
                localContext.lockJpa();
                cacheDdl(localContext,sub);
                writeIndicator(localContext,sub);
                localContext.unLockJpa();
                cleaCacheDdl(localContext);
            }

        } catch (Exception e) {
            log.error("ERROR during flushIndicators ", e);
            exit(localContext);
        } finally {
            if (totalList.size() > 0) {

                synLock.setObjs(totalList);
                synLock.setLastkey(Integer.toString(totalList.hashCode()));
                sendLastValueQueue(synLock);
            }
        }
    }

    // not sure
    private void cleaCacheDdl(DataCollecttingContext localContext) {
        localContext.getLastPerformedList().clear();
//        streamBrideg.send("Lvq","");
    }

    private void cacheDdl(DataCollecttingContext localContext, List<Object> sub) {
        localContext.getLastPerformedList().addAll(sub);
//        sendLastValueQueue(lastPerformedList);
    }

    @Deprecated
    private void sendLastValueQueue(Object payload) {
        if (config.isEnableLVQ()) {
            log.info("sync to lvq......");
            streamBrideg.send(config.getLVQTopicName(), payload);
        }
    }

    private void sendLVQ(DataCollecttingContext context){
        if(context.getForceFlushFlag().get()){
            //do nothing on force flush mode
            return ;
        }

        DbTableIndicatorConfig currentLocalConfig = context.getTableConfig();
        SynLock synLock = context.getSynLock();
        synLock.setSynStatus(SynLock.SynStatus.Recover);
        synLock.setObjs(context.getFinishedList());
        synLock.setLastkey(Integer.toString(context.getFinishedList().hashCode()));
        context.getSynLock().setLastkey(synLock.getLastkey());
        MessageHeaderAccessor mha = new MessageHeaderAccessor();
//        mha.setHeader(BinderHeaders.TARGET_DESTINATION,currentLocalConfig.getTopic());
        Message<?> message = MessageBuilder
                .withPayload(synLock)
                .setHeaders(mha)
                .build();
//        log.info("Caching to lvq topic [{}]... object : {}",currentLocalConfig.getTableKey(),synLock.statusString());
        streamBrideg.send(currentLocalConfig.getBinding(), message, MimeType.valueOf(outputContentType));

    }



    //    @PostConstruct
    public void postConstruct(String tableName) {
        String jpaEntity = connectorConfig.get(tableName);
        try {
            getLocalContext().setRepo((JpaRepository) context.getBean(Class.forName(jpaEntity + "Repo")));
        } catch (ClassNotFoundException e) {
            log.error("ERROR initializing Repo Bean {}", jpaEntity + "Repo");
            exit(3);
        }
    }

    @PreDestroy
    public void performlastList() {
        log.info("flush events before shutdown...");
        ready.set(false);
        for(DataCollecttingContext context: contextMap.values()){
            forceFlushEvents(context);
        }
    }

    public void status (){
        for(DataCollecttingContext context: contextMap.values()){
            context.status();
        }
    }
}
