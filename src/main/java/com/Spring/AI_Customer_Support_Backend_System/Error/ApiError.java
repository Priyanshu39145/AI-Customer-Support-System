package com.Spring.AI_Customer_Support_Backend_System.Error;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
public class ApiError {
    private LocalDateTime timestamp;
    private String error;
    private HttpStatus statusCode;

    public ApiError()   {
        timestamp = LocalDateTime.now();
    }

    public ApiError(String error , HttpStatus statusCode)   {
        this(); //Calling default constructor --- to set the timestamp;
        this.statusCode = statusCode;
        this.error = error;

    }
}