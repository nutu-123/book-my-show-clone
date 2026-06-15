package com.showtime.show_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String  message;
    private T       data;
    private String  error;

    @Builder.Default
    private String timestamp = LocalDateTime.now().toString();

    private Integer page;
    private Integer size;
    private Long    totalElements;
    private Integer totalPages;

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true).message(message).data(data).build();
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Operation successful");
    }

    public static <T> ApiResponse<T> error(String message, String error) {
        return ApiResponse.<T>builder()
                .success(false).message(message).error(error).build();
    }
}