package com.example.common;

import com.example.Logger.TransactionLogger;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
public class CommonUtils {

    public CommonUtils() {
    }

    private static final String CLIENT_IP = "clientIP";

    private static final String TRANSACTION_TEMPLATE = "User has uploaded document for client id %s message %s loggedTime %s ipAddress  %s .";


    public static void setClientIpAddress(HttpServletRequest request){
        String ipAddress = request.getHeader("x-forwarded-for");
        if(ipAddress != null){
            ipAddress = ipAddress.split(",")[0];
        }else {
            ipAddress = request.getRemoteAddr();
        }
        request.setAttribute(CLIENT_IP,ipAddress);
    }


    public static String getClientIPAddress(HttpServletRequest request){
        Object ipAddress = request.getAttribute(CLIENT_IP);
        if(ipAddress == null){
            setClientIpAddress(request);
            ipAddress = request.getAttribute(CLIENT_IP);
        }
        return (String) ipAddress;
    }


    public static void logTransactionDetails(HttpServletRequest httpRequest,String user, String message){
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.setLength(0);
        messageBuilder.append(
                String.format(TRANSACTION_TEMPLATE,
                        user,
                        message,
                        LocalDateTime.now(),
                        httpRequest.getRemoteAddr()
                ));
        String msg = messageBuilder.toString();
        log.info(msg);
        TransactionLogger.writeToTransactionLog("{}" + msg, "USER");
    }

}
