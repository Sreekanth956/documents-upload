package com.example.docupload.service;


import com.example.Logger.TransactionLogger;
import com.example.constant.CommonConstants;
import com.example.docupload.model.ResponseObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.multipart.MultipartFile;

import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

@Service
@Slf4j
public class DocumentUploadService {

    @Value("${docupload.comments.nofcharsallowed}")
    private String noOfCharactersEntered;

    @Value("${docupload.maxnooffiledupload}")
    private String maxNofOfFilesUploadable;

    @Value("${docupload.filenamelength}")
    private String fileNameLength;

    @Value("${spring.mail.username}")
    private String usernameMail;

    @Value("${spring.mail.password}")
    private String userAppPassword;

    @Value("${docShare.from.emailId}")
    private String docShareFromEmailId;

    @Value("${docShare.to.emailId}")
    private String docShareToEmailId;

    private static final List<String> ALLOWED_MIME_TYPES;

    static {

        ALLOWED_MIME_TYPES = new ArrayList<>();
        ALLOWED_MIME_TYPES.add(MediaType.application("pdf").toString());
        ALLOWED_MIME_TYPES.add(MediaType.application("excel").toString());
        ALLOWED_MIME_TYPES.add(MediaType.application("msword").toString());
        ALLOWED_MIME_TYPES.add(MediaType.application("x-tika-msoffcie").toString());
        ALLOWED_MIME_TYPES.add(MediaType.application("x-tika-ooxml").toString());
        ALLOWED_MIME_TYPES.add(MediaType.application("jpeg").toString());
        ALLOWED_MIME_TYPES.add(MediaType.application("jpg").toString());
        ALLOWED_MIME_TYPES.add(MediaType.application("bmp").toString());
        ALLOWED_MIME_TYPES.add(MediaType.application("png").toString());

    }

    public ResponseObject processFileUpload(MultipartFile[] multiPartFiles, String message){

        ResponseObject fileUploadResponse = ValidateFiles(message, multiPartFiles);
        if(fileUploadResponse != null){
            return fileUploadResponse;
        }

        Multipart multipart = new MimeMultipart();
        StringBuilder sbEmailBody = new StringBuilder();
        StringBuilder sbTransactionLog = new StringBuilder();
        StopWatch stopWatchEmailResponse = new StopWatch();

        try {
            sbEmailBody.append("Date: ").append(LocalDateTime.now()).append(CommonConstants.LINE_SEPARATOR);
            sbEmailBody.append("Message: ").append(message).append(CommonConstants.LINE_SEPARATOR);
            sbTransactionLog.append("Message: ").append(message);
            sbEmailBody.append("# of Files Details: ").append(multiPartFiles.length).append(CommonConstants.LINE_SEPARATOR);
            sbTransactionLog.append(". # of Files Details: ").append(multiPartFiles.length);
            sbEmailBody.append("Files Details: ").append(CommonConstants.LINE_SEPARATOR);
            sbTransactionLog.append(". Files Details: ");

            int i=0;
            long totalSize = 0;
            for(MultipartFile file : multiPartFiles){
                log.info("Doc upload File Size :: {}", file.getSize());
                String originalFilename = file.getOriginalFilename();
                String extension = Objects.requireNonNull(originalFilename).substring(originalFilename.lastIndexOf("."), originalFilename.length());
                if(originalFilename.length() > Integer.parseInt(fileNameLength)){
                    TransactionLogger.writeToTransactionLog("Document Upload : Rejected due to file name too long '"+ originalFilename+"'");
                    return new ResponseObject("Rejected due to file name too long: rename it lessthan "+fileNameLength+" characters and try again.", false);
                }
                if(!CommonConstants.ALLOWED_FILE_TYPES.contains(extension)){
                    return new ResponseObject(String.format("You are trying to upload an invalid file type. Please upload %s files.", CommonConstants.ALLOWED_FILE_TYPES), false);
                }
                if(file.getSize() == 0){
                    TransactionLogger.writeToTransactionLog("Document Upload : File is of 0 KB '"+originalFilename+"'");
                    return new ResponseObject("Documents Upload Failed. Please try again", false);
                }
                totalSize += file.getSize();
                if(totalSize>10485760){
                    return new ResponseObject("Sorry. We cannot process files larger than 10 MB.", false);
                }

                byte[] fileContent = file.getBytes();
                String detectedFileType = (new Tika()).detect(fileContent);
                if(!ALLOWED_MIME_TYPES.contains(detectedFileType)){
                    TransactionLogger.writeToTransactionLog("Document Upload : Rejected due to unapproved MIME type'"+ detectedFileType+"'");
                    return new ResponseObject(String.format("You are trying to upload an invalid file type. Please upload %s files.", CommonConstants.ALLOWED_FILE_TYPES), false);
                }
                i++;
                MimeBodyPart att = new MimeBodyPart();
                ByteArrayDataSource bds = new ByteArrayDataSource(fileContent, detectedFileType);
                att.setDataHandler((new DataHandler(bds)));
                att.setFileName("Document"+i+extension);
                multipart.addBodyPart(att);
                sbEmailBody.append("Document").append(i).append(extension).append(CommonConstants.LINE_SEPARATOR);
                sbTransactionLog.append(". ").append(i).append(":").append(originalFilename).append("Size: ").append(file.getSize()).append(")");
            }
            prepareAndSendAdminEmail(sbEmailBody,multipart,stopWatchEmailResponse);
            stopWatchEmailResponse.stop();
            TransactionLogger.writeToTransactionLog("Document Upload : Success in "+stopWatchEmailResponse.getTotalTimeSeconds()+" seconds."+ sbTransactionLog.toString());
            //emailService.sendConfirmationEmail(CommonConstants.EMAIL_SUBJECT, CommonConstants.EMAIL_TITLE, CommonConstants.EMAIL_CONFIRMATION_MESSAGE);
            return new ResponseObject(CommonConstants.SUCCESS_MESSAGE, true);
        }
        catch (Exception e)
        {
            if(stopWatchEmailResponse.isRunning()){
                stopWatchEmailResponse.stop();
            }
            log.error("Error while reading the uploaded file", e);
            TransactionLogger.writeToTransactionLog("Document Upload : Failed in "+stopWatchEmailResponse.getTotalTimeSeconds()+" seconds. "+sbTransactionLog.toString());
            return new ResponseObject("Documents Upload Failed. Please try again later", false);
        }
    }

    private void prepareAndSendAdminEmail(StringBuilder sbEmailBody, Multipart multipart, StopWatch stopWatchEmailResponse) throws MessagingException {

//        Session session =  Session.getDefaultInstance(setProperties());

        // Create the session with SMTP authentication
        Session session = Session.getInstance(setProperties(), new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(usernameMail, userAppPassword);
            }
        });
        // Create the email message
        MimeMessage emailMessage = new MimeMessage(session);
        emailMessage.setFrom(new InternetAddress(docShareFromEmailId));

        // Add recipients
        String[] toEmailIds = docShareToEmailId.split(",");
        for(String toEmailId : toEmailIds){
            emailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmailId));
        }
        // Set email subject and content
        emailMessage.setSubject("Document(s) uploaded for User( )");
        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText(sbEmailBody.toString());
        multipart.addBodyPart(messageBodyPart);
        emailMessage.setContent(multipart);
        // Start timer and send the email
        stopWatchEmailResponse.start();
        Transport.send(emailMessage);
    }

    private ResponseObject ValidateFiles(String message, MultipartFile[] multiPartFiles) {

        if(message.length() > Integer.parseInt(noOfCharactersEntered)){
            return new ResponseObject("Please do not enter more than "+ noOfCharactersEntered +" characters", false);
        }

        if(multiPartFiles.length > Integer.parseInt(maxNofOfFilesUploadable)){
            //return new ResponseObject("Please submit no more than "+maxNofOfFilesUploadable+" files at a time.", false);
            return new ResponseObject("You can upload up to "+maxNofOfFilesUploadable+" files at a time. Please try again with fewer files.", false);
        } else if (multiPartFiles.length == 0) {
            return new ResponseObject("Please select the type of file you want to upload.", false);
        }

        return null;
    }


    private Properties setProperties(){
//        Properties properties = System.getProperties();
//        properties.setProperty("mail.smtp.host", smtpHost);
        Properties properties = new Properties();

        // SMTP server details
        properties.setProperty("mail.smtp.host", "smtp.gmail.com");
        properties.setProperty("mail.smtp.port", "587");
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        // Optional: Debugging to help diagnose any connection issues
        properties.setProperty("mail.debug", "true");
        return properties;
    }
}
