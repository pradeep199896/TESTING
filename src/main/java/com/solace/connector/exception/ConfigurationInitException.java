package com.solace.connector.exception;

public class ConfigurationInitException extends Exception {

    public ConfigurationInitException(String message){
        super("ConfigurationInit ERROR: "+message);
    }

}
