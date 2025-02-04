package com.example.Logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionLogger {

    private static Logger log = LoggerFactory.getLogger(TransactionLogger.class);

    public static void writeToTransactionLog(String message, Object... arguments){
        log.info(message, arguments);
    }

    public static void writeToTransactionLog(String appName, String message, Object... arguments){
        log.info("{}" + message, appName, arguments);
    }


    public static void info(String appName, String message, Object... arguments){
        log.info("{}" + message, appName, arguments);
    }

    public static void debug(String appName, String message, Object... arguments){
        log.debug("{}" + message, appName, arguments);
    }

    public static void error(String appName, String message, Object... arguments){
        log.error("{}" + message, appName, arguments);
    }


}
