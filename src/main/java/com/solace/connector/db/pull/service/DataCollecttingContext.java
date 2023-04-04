package com.solace.connector.db.pull.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import com.solace.connector.db.config.DbTableIndicatorConfig;
import com.solace.connector.db.pull.lock.SynLock;
import com.solace.connector.exception.InvalidPropertyException;
import com.solace.connector.utility.JpaBeanUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

@Data
@Slf4j
public class DataCollecttingContext {

    private DbTableIndicatorConfig tableConfig;

    private List<Object> totalList = new ArrayList<>();
    private List<Object> finishedList = new CopyOnWriteArrayList<>();
    private List<Object> lastPerformedList = new CopyOnWriteArrayList<>();
    private List<MessageLoad> xmlResult = new CopyOnWriteArrayList<>();

    private int handleMessageCount = 0;
    private int succesedMessageCount = 0;
    private SynLock synLock = null;

    private AtomicBoolean sendLock = new AtomicBoolean(false);
    private AtomicBoolean jpaLock = new AtomicBoolean(false);
    private AtomicBoolean forceFlushFlag = new AtomicBoolean(false);
    private AtomicBoolean done = new AtomicBoolean(false);
    private AtomicBoolean exitPerforming = new AtomicBoolean(false);

    public boolean noLock(){
        return !sendLock.get() && !jpaLock.get();
    }

    public boolean isJpaLocked(){
        return jpaLock.get();
    }

    public void lockJpa(){
        jpaLock.set(true);
    }

    public void unLockJpa(){
        jpaLock.set(false);
    }

    public boolean isSendLocked(){
        return sendLock.get();
    }

    public void lockSend(){
        sendLock.set(true);
    }

    public void unLockSend(){
        sendLock.set(false);
    }

    private List<Map<String, Object>> batchParams = new ArrayList<>();

    private boolean dynamicDestination = false;
    private boolean isTest =false;

    private Map<String, String> staticValues = new HashMap<>();
    private Map<String, String> msgHeaderValues = new HashMap<>();

    private JpaRepository repo = null;

    private long pollerStartTime;
    private long setpStartTime;

    public void setPollerStartTime() {
        this.pollerStartTime = Calendar.getInstance().getTimeInMillis();
    }

    public void setSetpStartTime() {
        this.setpStartTime = Calendar.getInstance().getTimeInMillis();
    }

    public void setTranslatorStartTime() {
        this.translatorStartTime =Calendar.getInstance().getTimeInMillis();
    }

    public void addSendWaitingTime() {
        this.sendWaitingTime +=1;
    }

    public void setSendWaitingTime() {
        this.sendWaitingTime =Calendar.getInstance().getTimeInMillis();
    }

    public void setSendStartTime() {
        this.sendStartTime =Calendar.getInstance().getTimeInMillis();
    }

    public void setWaitAckTime() {
        this.waitAckTime =Calendar.getInstance().getTimeInMillis();
    }

    public void setFlushStartTime() {
        this.flushStartTime = Calendar.getInstance().getTimeInMillis();
    }

    public void setStartSendingTime() {
        this.startSendingTime = Calendar.getInstance().getTimeInMillis();
    }

    private long startSendingTime = -1;
    private long translatorStartTime;
    private long sendWaitingTime;
    private long sendStartTime;
    private long waitAckTime;
    private long flushStartTime;


    public DataCollecttingContext(DbTableIndicatorConfig config) {
        this.tableConfig = config;
        initHardCoreValues(config.getScaledConfig());
        initBenchmarkTime();
    }

    public void initBenchmarkTime() {
        pollerStartTime = Calendar.getInstance().getTimeInMillis();
        setpStartTime = pollerStartTime;
        translatorStartTime = pollerStartTime;
        sendStartTime = pollerStartTime;
        waitAckTime = pollerStartTime;
        flushStartTime = pollerStartTime;
    }

    public void reportBenchmarkTime(boolean enable){
        if (enable) {
            long finishTime = Calendar.getInstance().getTimeInMillis();
            log.info("==[BenchMark Summary]== Working on Table: ======[{}]======. ", tableConfig.getTableKey());
            log.info("==[BenchMark Summary]== Query         Time: {} milliseconds. ", setpStartTime - pollerStartTime);
            log.info("==[BenchMark Summary]== Set Processed Time: {} milliseconds. ", translatorStartTime - setpStartTime);
            log.info("==[BenchMark Summary]== Translate     Time: {} milliseconds. ", sendStartTime - translatorStartTime);
            log.info("==[BenchMark Summary]== Send          Time: {} milliseconds. waiting : {} milliseconds", waitAckTime - sendStartTime,sendWaitingTime);
            log.info("==[BenchMark Summary]== Wait Ack      Time: {} milliseconds. ", flushStartTime - waitAckTime);
            log.info("==[BenchMark Summary]== Flush         Time: {} milliseconds. ", finishTime - flushStartTime);
            log.info("==[BenchMark Summary]== Total         Time: {} milliseconds. ", finishTime - pollerStartTime);
        }
    }


    public void reset(){
        finishedList.clear();
        xmlResult.clear();
        totalList.clear();
        batchParams.clear();
        handleMessageCount=0;
    }

    /**
     *
     * @param maxWait
     * @return true for safe ,false for wait timeout
     */
    public boolean checkBufferedEvents(long maxWait) {

        List<Object> finishedList= getFinishedList();
        List<Object> totalList= getTotalList();

        int handleMessageCount = getHandleMessageCount();
        long startSendingTime = getStartSendingTime();
        if (finishedList.size() > 0 || handleMessageCount > 0) {
            log.info("======checking buffered events======> table : {} finished: {}  in  total: {}", tableConfig.getTableKey(), finishedList.size(), handleMessageCount);
        }

        long now = Calendar.getInstance().getTimeInMillis();
        if (noLock()) {

            if (startSendingTime != -1 && now - startSendingTime > maxWait) {
                forceFlushFlag.set(true);
                return false;
            }

        } else {
            setStartSendingTime();
        }
        return true;
    }

    private void initHardCoreValues(Map<String,String> configMap ){
        for (String s : configMap.keySet()) {
            //add hard code value for the xxx_hc_prop in main entity
//            if (s.contains("_hc_")) {
//                String hc_prop = s.split("_hc_")[1];
//                if (tableConfig.trgtMapping.containsKey(hc_prop)) {
//                    staticValues.put(s.split("_hc_")[1], configMap.get(s));
//                }
//            }
            //entity property for message header
            if (s.startsWith("MESSAGE_HEADER_")) {
                msgHeaderValues.put(s.substring(15), configMap.get(s));
            }

        }
    }

    public Map<String, Object> parseEntity2Map(Object jpaEntity, Map<String, String> entityTrgtMapping) throws InvalidPropertyException {

        Map<String, Object> rowMap = new HashMap<>();




        for (String k : entityTrgtMapping.keySet()) {
            Object o = null;
            if(jpaEntity instanceof Map){
                o = ((Map)jpaEntity).get(k.toUpperCase(Locale.ROOT));
            }else{
                o = JpaBeanUtil.invokeGetter(jpaEntity, k);
            }


            if (o instanceof Date) {
                //parse Date  to String
                rowMap.put(k, DateUtil.format((Date) o, tableConfig.getTimeFormat()));
            } else if (o instanceof List) {
                List<Map<String, Object>> subs = new ArrayList<>();
                for(Object row: ((List<Object>) o)){
                    String value = entityTrgtMapping.get(k);
                    String[] vs = value.split("/");
                    value = vs[vs.length - 1];
//                    subs.add(parseEntity2Map(row, tableConfig.subTrgtMapping.get(value.replace("[", "").replace("]", ""))));
                }

                rowMap.put(k, subs);
            //TODO encode byte[] to hex char array ,due to the other side
            }else if(o instanceof byte[]){
                rowMap.put(k, HexUtil.encodeHex((byte[]) o));
            }else {
                if(o==null){
                    o="";
                }
                rowMap.put(k, o);
            }
        }

        return rowMap;
    }

    public String status(){
        return StrUtil.format("{} status : {}/{} {}",tableConfig.getTableKey(),finishedList.size(),totalList.size(),done);
    }
}
