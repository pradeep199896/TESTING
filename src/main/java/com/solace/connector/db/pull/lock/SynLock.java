package com.solace.connector.db.pull.lock;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import org.springframework.messaging.support.MessageHeaderAccessor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;



@Data
public class SynLock implements Serializable {

    public enum SynStatus{
        ByPass , Trigger , Recover
    }

    private static final long serialVersionUID = -813497427999346684L;

    private String uid ;
    private String tableKey;
    //default status is Recover
    private SynStatus synStatus = SynStatus.Recover;

    private String connectorId;
    private String orderKey;
    private long lockNumber =0 ;
    private boolean continues = false ;

    private String lastkey ;
    private Object[] lastOrderKey ;

    private long time ;
    private List<Object> objs = new ArrayList<>();

    private Object testPayload;

    public SynLock(){
        uid = UUID.fastUUID().toString();
    }

    public SynLock(boolean continues) {
        super();
        this.continues =  continues;
    }

    public SynLock(String hash){
        super();
        this.connectorId = hash;
    }

    public String statusString(){
        return StrUtil.format("Synlock:[{}] tabelKey: [{}] , synStatus: [{}] , lastKey : [{}] , objectSize: [{}]",uid,tableKey,synStatus,lastkey, objs.size());
    }
}
