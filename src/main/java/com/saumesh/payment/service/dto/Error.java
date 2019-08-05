package com.saumesh.payment.service.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Error {
    @JsonProperty
    private int status;
    @JsonProperty
    private String code;
    @JsonProperty
    private String message;

    @JsonCreator
    public Error(@JsonProperty("status") int status,
                 @JsonProperty("code") String code,
                 @JsonProperty("message") String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
