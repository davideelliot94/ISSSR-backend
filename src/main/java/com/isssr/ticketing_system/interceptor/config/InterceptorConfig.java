package com.isssr.ticketing_system.interceptor.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.isssr.ticketing_system.exception.TokenExpiredException;
import com.isssr.ticketing_system.jwt.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.Enumeration;


@Component
public class InterceptorConfig implements HandlerInterceptor {

    private static String authToken = null;
    private String requestedURI;
    private static JwtTokenUtil jwtTokenUtil = null;


    public static void setJwtTokenUtil(JwtTokenUtil jwtTokenUtil){
        System.out.println("setting jwt token: " + jwtTokenUtil);
        InterceptorConfig.jwtTokenUtil = jwtTokenUtil;
    }

    public static void setJwtToken(String authToken){
        InterceptorConfig.authToken = authToken;
    }



    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        boolean res = true;
        System.out.println("pre handle; jwttoken: " + jwtTokenUtil);

        requestedURI = request.getRequestURI();
        if(!requestedURI.equals("/ticketingsystem/public/login/")){
            System.out.println("not login");
            System.out.println("requestUri is: " + requestedURI);
            res = InterceptorConfig.jwtTokenUtil.canTokenBeRefreshed(authToken);
            System.out.println("refreshable token: " + res);
            if(res == false) {
                response.getWriter().write("expiration");
            }
            return res;

        }else{System.out.println("it's login");}



        return true;
    }


    /*public static void saveToken(String token){
        JWTToken = token;
    }*/


    @Override
    public void postHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView)
            throws Exception {

        System.out.println("postHandle!");
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        System.out.println("afterCompletion() is invoked");
    }

}
