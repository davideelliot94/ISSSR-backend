package com.isssr.ticketing_system.configuration;

import com.isssr.ticketing_system.acl.CustomPermissionEvaluator;
import com.isssr.ticketing_system.controller.RequestLoggerController;
import com.isssr.ticketing_system.entity.RequestLog;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.Date;

@Log
@Configuration
public class LoggableDispatcherServlet extends DispatcherServlet {

    public static final int MAX_PAYLOAD_LENGTH_TO_LOG = 5120;

    private final RequestLoggerController requestLoggerController;
    private final ConfigProperties configProperties;

    @Autowired
    public LoggableDispatcherServlet(RequestLoggerController requestLoggerController, ConfigProperties configProperties) {
        this.requestLoggerController = requestLoggerController;
        this.configProperties = configProperties;
    }

    @Bean(name = DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)
    public DispatcherServlet dispatcherServlet() {
        return new LoggableDispatcherServlet(requestLoggerController, configProperties);
    }

/*

    @Autowired
    private CustomPermissionEvaluator permissionEvaluator;

    @Bean
    public MethodSecurityExpressionHandler createExpressionHandler() {
        //return defaultMethodSecurityExpressionHandler;
        DefaultMethodSecurityExpressionHandler expressionHandler =
                new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(permissionEvaluator);
        return expressionHandler;
    }
*/

    @Override
    protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (!(request instanceof ContentCachingRequestWrapper)) {
            request = new ContentCachingRequestWrapper(request);
        }

        HandlerExecutionChain handler = getHandler(request);

        try {
            super.doDispatch(request, response);
        } finally {
            log(request, response, handler);
        }
    }

    private void log(HttpServletRequest requestToCache, HttpServletResponse responseToCache,
                     HandlerExecutionChain handler) {
        RequestLog requestLog = new RequestLog(
                responseToCache.getStatus(),
                requestToCache.getMethod(),
                requestToCache.getRequestURI(),
                requestToCache.getRemoteAddr(),
                new Timestamp(new Date().getTime()),
                handler.toString(),
                getRequestPayload(requestToCache),
                getResponsePayload(responseToCache)
        );

        requestLoggerController.saveRequestLog(requestLog);

        if (configProperties.getDebugConfig().isDebug()) {
            log.info(requestLog.toString());
        }
    }

    @Nullable
    private String getResponsePayload(HttpServletResponse response) {
        ContentCachingResponseWrapper wrapper = WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        return wrapper != null
                ? extractPayloadFromBuffer(wrapper.getContentAsByteArray(), wrapper.getCharacterEncoding())
                : null;
    }


    @Nullable
    private String getRequestPayload(HttpServletRequest request) {
        ContentCachingRequestWrapper wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        return wrapper != null
                ? extractPayloadFromBuffer(wrapper.getContentAsByteArray(), wrapper.getCharacterEncoding())
                : null;
    }

    private String extractPayloadFromBuffer(byte[] buf, String characterEncoding) {
        if (buf.length > 0) {
            int length = Math.min(buf.length, MAX_PAYLOAD_LENGTH_TO_LOG);
            try {
                return new String(buf, 0, length, characterEncoding);
            } catch (UnsupportedEncodingException ex) {
                return "[Error extracting payload";
            }
        }
        return "";
    }

}