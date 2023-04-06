package com.solace.connector.exception;

public class InvalidPropertyException extends Exception {

    public InvalidPropertyException(String message){
        super("Entity Property ERROR: "+message);
    }

}
