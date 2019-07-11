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

        System.out.println("adding interceptor");
        System.out.println("this method will get invoked by container while deployment");
        //System.out.println("value of interceptor is "+interceptor);
        //adding custom interceptor
        interceptorRegistry.addInterceptor(new InterceptorConfig()).addPathPatterns("/**").excludePathPatterns("/public/perm");
    }
}
