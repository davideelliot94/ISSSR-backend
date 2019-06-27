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

    private String authToken = null;
    private String requestedURI;
    private static JwtTokenUtil jwtTokenUtil;


    public static void setJwtTokenUtil(JwtTokenUtil jwtTokenUtil){
        InterceptorConfig.jwtTokenUtil = jwtTokenUtil;
    }



    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        boolean res = true;
        System.out.println("pre handle");

        requestedURI = request.getRequestURI();
        if(!requestedURI.equals("/ticketingsystem/public/login/")){
            System.out.println("not login");

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
        System.out.println("postHandle() is invoked");
        System.out.println("response headers: " + response.getHeaderNames());
        System.out.println("jwttoken: " +response.getHeader("Authorization"));


        if(requestedURI.equals("/ticketingsystem/public/login/")){
            authToken = response.getHeader("Authorization");

        }
        response.getHeaderNames().add("errorMsg");
        System.out.println("ending interceptor");
        response.setHeader("errorMsg","expiredSession");

        //System.out.println("jwttoken: " +response.getHeader("Authorization"));

    }

    @Override
    public void afterCompletion(
            HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        System.out.println("afterCompletion() is invoked");
    }

}
