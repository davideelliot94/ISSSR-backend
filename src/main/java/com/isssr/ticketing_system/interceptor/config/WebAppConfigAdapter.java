package com.isssr.ticketing_system.interceptor.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Component
public class WebAppConfigAdapter implements WebMvcConfigurer {
    @Autowired
    InterceptorConfig productServiceInterceptor;
    //InterceptorConfig  interceptor;

    @Override
    public void addInterceptors (InterceptorRegistry interceptorRegistry) {

        /*AGGIUNGO L'INTERCEPTOR ED ESCLUDO IL PATH /perm POICHÉ VIENE INVIATO PRIMA DELL'AUTENTICAZIONE
        * QUANDO IL TOKEN NON È STATO ANCORA CREATO */

        interceptorRegistry.addInterceptor(new InterceptorConfig()).addPathPatterns("/**").excludePathPatterns("/public/perm").excludePathPatterns("/error");
    }
}
