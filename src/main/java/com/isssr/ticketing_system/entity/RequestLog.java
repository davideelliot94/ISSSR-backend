package com.isssr.ticketing_system.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "requests_logs")
public class RequestLog {

    private final static int LIMIT = 255;

    @Column(name = "request_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "ticketStatus")
    private Integer status;

    @Column(name = "http_method")
    private String httpMethod;

    @Column(name = "uri")
    private String uri;

    @Column(name = "client_ip")
    private String clientIp;

    @Column(name = "request_timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss", timezone = "Europe/Rome")
    private Timestamp requestTimestamp;

    @Column(name = "java_method")
    private String javaMethod;

    @Column(name = "request_body")
    private String requestBody;

    @Column(name = "response_body")
    private String responseBody;

    public RequestLog(@NotNull Integer status, @NotNull String httpMethod,
                      @NotNull String uri, @NotNull String clientIp,
                      @NotNull Timestamp requestTimestamp) {
        this(
                status,
                httpMethod,
                uri,
                clientIp,
                requestTimestamp,
                null,
                null,
                null
        );
    }

    public RequestLog(@NotNull Integer status, @NotNull String httpMethod,
                      @NotNull String uri, @NotNull String clientIp,
                      @NotNull Timestamp requestTimestamp,
                      String javaMethod, String requestBody,
                      String responseBody) {
        this.status = status;
        this.httpMethod = httpMethod;
        this.uri = uri;
        this.clientIp = clientIp;
        this.requestTimestamp = requestTimestamp;
        this.javaMethod = RequestLog.convertToDatabaseColumn(javaMethod);
        this.requestBody = RequestLog.convertToDatabaseColumn(requestBody);
        this.responseBody = RequestLog.convertToDatabaseColumn(responseBody);
    }

    public static String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        } else if (attribute.length() > LIMIT) {
            return attribute.substring(0, LIMIT);
        } else {
            return attribute;
        }
    }

    @Override
    public String toString() {
        StringBuilder log = new StringBuilder();
        log.append("\n------------------Request id: ").append(id);
        log.append("\nTicketStatus: ").append(status);
        log.append("\nHttp Method: ").append(httpMethod);
        log.append(" Path: ").append(uri);
        log.append("\nClient Ip: ").append(clientIp);
        if (javaMethod != null && !javaMethod.isEmpty()) log.append("\nJava method: ").append(javaMethod);
        if (requestBody != null && !requestBody.isEmpty()) log.append("\nRequest body:").append(requestBody);
        if (responseBody != null && !responseBody.isEmpty()) log.append("\nResponse body:").append(responseBody);
        log.append("\n------------------End request - ").append(requestTimestamp);

        return log.toString();
    }

}
