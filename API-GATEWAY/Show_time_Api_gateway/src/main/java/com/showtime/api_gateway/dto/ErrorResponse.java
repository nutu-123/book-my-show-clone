package com.showtime.api_gateway.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;

/**
 * Standard error response DTO for the API Gateway.
 * Returned when routing fails, JWT is invalid, or service is down.
 */
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    
	public ErrorResponse() {
    }

    // Parameterized constructor
    public ErrorResponse(boolean success, String message, String error, String timestamp) {
        this.success = success;
        this.message = message;
        this.error = error;
        this.timestamp = timestamp;
    }
    
	private boolean success;
    private String  message;
    private String  error;
    private String  timestamp;
	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
    
}