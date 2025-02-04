package com.example.docupload.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseObject {

    private String message;

    private boolean success;
}
