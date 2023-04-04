package com.solace.connector.db.pull.service;


import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class MessageLoad implements Serializable {


    private static final long serialVersionUID = 6164788722188064580L;

    private int index ;
    private boolean ready=false;
    private String xml ;
    private String topic ;
    private int start ;
    private int end ;

    private Map<String,String> headerMap = new HashMap<>();

    private List<Object> objectList=new ArrayList<>();

    public MessageLoad(List<Object> objectList) {
        this.objectList = objectList;
    }
}
