package com.example.docupload.controller;

import com.example.common.CommonUtils;
import com.example.docupload.model.ResponseObject;
import com.example.docupload.service.DocumentUploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

@Controller
public class DocUploadController {

    private final DocumentUploadService documentUploadService;

    public DocUploadController(DocumentUploadService documentUploadService) {
        this.documentUploadService = documentUploadService;
    }

    @PostMapping("/api/upload")
    public ResponseEntity<ResponseObject> uploadDocument(@RequestParam("files")MultipartFile[] files, @RequestParam(name = "document", required = true) String document, HttpServletRequest request){

        CommonUtils.setClientIpAddress(request);
        ResponseObject response = documentUploadService.processFileUpload(files,document);
        CommonUtils.logTransactionDetails(request,"USER",document);
        return new ResponseEntity<>(response,response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

}
