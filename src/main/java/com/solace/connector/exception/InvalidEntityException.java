package com.solace.connector.exception;

public class InvalidEntityException extends Exception {

    public InvalidEntityException(String message){
        super("Entity ERROR: "+message);
    }

}
