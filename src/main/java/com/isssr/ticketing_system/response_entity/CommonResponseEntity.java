package com.isssr.ticketing_system.response_entity;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class CommonResponseEntity {
    public static ResponseEntity NotFoundResponseEntity(String status) {
        return BaseResponseEntity(HttpStatus.NOT_FOUND, status);
    }

    public static ResponseEntity BadRequestResponseEntity(String status) {
        return BaseResponseEntity(HttpStatus.BAD_REQUEST, status);
    }

    public static ResponseEntity OkResponseEntity(String status) {
        return BaseResponseEntity(HttpStatus.OK, status);
    }

    public static ResponseEntity UnprocessableEntityResponseEntity(String status) {
        return BaseResponseEntity(HttpStatus.UNPROCESSABLE_ENTITY, status);
    }

    public static ResponseEntity UnauthorizedResponseEntity(String status) {
        return BaseResponseEntity(HttpStatus.UNAUTHORIZED, status);
    }

    public static ResponseEntity FailedDependencyResponseEntity(String status) {
        return BaseResponseEntity(HttpStatus.FAILED_DEPENDENCY, status);
    }

    public static ResponseEntity CreatedResponseEntity(String status) {
        return BaseResponseEntity(HttpStatus.CREATED, status);
    }
    public static ResponseEntity BaseResponseEntity(HttpStatus httpStatus, String status) {
        return new HashMapResponseEntityBuilder()
                .set("ticketStatus", status)
                .setStatus(httpStatus)
                .build();
    }
}
