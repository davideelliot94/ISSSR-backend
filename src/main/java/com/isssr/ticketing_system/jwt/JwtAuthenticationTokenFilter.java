package com.isssr.ticketing_system.jwt;

import com.isssr.ticketing_system.exception.TokenExpiredException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

/**
 * This class implements a filter to process jwt authenticated requests
 */
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Autowired
    @Qualifier("JwtUserDetailsService")
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Value("${jwt.header}")
    private String tokenHeader;

    /**
     * Extract userDetails from jwt authenticaton token and set the current request as authenticated
     *
     * @param httpServletRequest the request
     * @param httpServletResponse the response
     * @param filterChain the filter to execute after jwt authentication filter
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest,
                                    HttpServletResponse httpServletResponse,
                                    FilterChain filterChain) throws ServletException, IOException {

        System.out.println("executing 1");
        String authToken = httpServletRequest.getHeader(this.tokenHeader);

        UserDetails userDetails = null;
        if (authToken != null) {
            userDetails = jwtTokenUtil.getUserDetails(authToken);
        }



        if (userDetails != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            //ricostruisco l userdetails con i dati contenuti nel token
            //controllo l'integrità del token
            try {
                if (jwtTokenUtil.validateToken(authToken, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails
                            , null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
                    // Iniezione utente autenticato nel contesto di sicurezza
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }catch(Exception e){
                e.printStackTrace();
            }

        }
        System.out.println("executing 2");
        System.out.println("httpservletrquest: " + httpServletRequest);
        System.out.println("servletresponse: " + httpServletResponse);
        System.out.println("filterchain: " + filterChain);

        try {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        }catch(Exception e){System.out.println("exception in dofilter");
        System.out.println("token header: " + tokenHeader);
        e.printStackTrace();
         }
        System.out.println("executing 3");
    }
}