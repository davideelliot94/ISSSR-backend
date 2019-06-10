package com.isssr.ticketing_system.response_entity;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class CommonResponseEntity {
    public static ResponseEntity NotFoundResponseEntity(String status) {
        System.out.println("not found");
        return BaseResponseEntity(HttpStatus.NOT_FOUND, status);
    }

    public static ResponseEntity BadRequestResponseEntity(String status) {
        System.out.println("Bad request");
        return BaseResponseEntity(HttpStatus.BAD_REQUEST, status);
    }

    public static ResponseEntity OkResponseEntity(String status) {
        System.out.println("response entity");
        return BaseResponseEntity(HttpStatus.OK, status);
    }

    public static ResponseEntity UnprocessableEntityResponseEntity(String status) {
        System.out.println("unprocessable");
        return BaseResponseEntity(HttpStatus.UNPROCESSABLE_ENTITY, status);
    }

    public static ResponseEntity UnauthorizedResponseEntity(String status) {
        System.out.println("unauthorized");
        return BaseResponseEntity(HttpStatus.UNAUTHORIZED, status);
    }

    public static ResponseEntity FailedDependencyResponseEntity(String status) {
        System.out.println("failed dependency");
        return BaseResponseEntity(HttpStatus.FAILED_DEPENDENCY, status);
    }

    public static ResponseEntity CreatedResponseEntity(String status) {
        System.out.println("created");
        return BaseResponseEntity(HttpStatus.CREATED, status);
    }
    public static ResponseEntity BaseResponseEntity(HttpStatus httpStatus, String status) {
        System.out.println("BaseResponseEntity");
        return new HashMapResponseEntityBuilder()
                .set("ticketStatus", status)
                .setStatus(httpStatus)
                .build();
    }
}
