package com.solace.connector.db.config;

import lombok.Data;

@Data
public class ValueTransferConfig {

    String timeFormat="yyyy/MM/dd HH:mm:ss"; //default datetime format
    String msgHeaderTimeFormat="yyyy/MM/dd HH:mm:ss.SSS" ;
}
