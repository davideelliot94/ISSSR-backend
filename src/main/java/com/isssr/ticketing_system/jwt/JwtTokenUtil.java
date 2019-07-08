package com.isssr.ticketing_system.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.isssr.ticketing_system.exception.TokenExpiredException;
import com.isssr.ticketing_system.interceptor.config.InterceptorConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.mobile.device.Device;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class implements an API to handle jwt tokens
 */
@Component
@ComponentScan
public class JwtTokenUtil implements Serializable {

    private static final long serialVersionUID = -3301605591108950415L;

    private static final String CLAIM_KEY_USERNAME = "sub";
    private static final String CLAIM_KEY_AUDIENCE = "audience";
    private static final String CLAIM_KEY_CREATED = "iat";
    private static final String CLAIM_KEY_AUTHORITIES = "roles";
    private static final String CLAIM_KEY_IS_ENABLED = "isEnabled";
    private static final String AUDIENCE_UNKNOWN = "unknown";
    private static final String AUDIENCE_WEB = "web";
    private static final String AUDIENCE_MOBILE = "mobile";
    private static final String AUDIENCE_TABLET = "tablet";

    //@Value("${jwt.secret}")
    private String secret="mySecret";

    @Autowired
    ObjectMapper objectMapper;

    //@Value("${jwt.expiration}")
    private Long expiration = new Long(7200);

    /**
     * Return username contained in jwt token
     *
     * @param token the token jwt
     * @return the username extracted from the jwt token
     */
    public String getUsernameFromToken(String token) { 
        String username;
        try {
            final Claims claims = getClaimsFromToken(token);
            username = claims.getSubject();
        } catch (Exception e) {
            username = null;
        }
        return username;
    }

    /**
     * Extract user details from jwt token
     *
     * @param token the jwt token
     * @return a JwtUser object representing user details
     */
    public JwtUser getUserDetails(String token) {

        if (token == null) {
            return null;
        }
        try {
            final Claims claims = getClaimsFromToken(token);
            List<SimpleGrantedAuthority> authorities = null;
            if (claims.get(CLAIM_KEY_AUTHORITIES) != null) {
                authorities = ((List<String>) claims.get(CLAIM_KEY_AUTHORITIES)).stream()
                        .map(role -> new SimpleGrantedAuthority(role)).collect(Collectors.toList());
            }

            return new JwtUser(
                    claims.getSubject(),
                    "",
                    authorities,
                    (boolean) claims.get(CLAIM_KEY_IS_ENABLED)
            );

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Return the creation date of a specified token
     *
     * @param token the jwt token
     * @return the date of the specified token
     */
    public Date getCreatedDateFromToken(String token) throws TokenExpiredException{
        Date created;
        try {
            final Claims claims = getClaimsFromToken(token);
            created = new Date((Long) claims.get(CLAIM_KEY_CREATED));
        } /*catch (Exception e) {*/
        catch (TokenExpiredException e) {
            created = null;
            throw e;
        }

        return created;
    }

    /**
     * Return the expiration date of a token
     *
     * @param token the jwt token
     * @return the expiration date
     */
    public Date getExpirationDateFromToken(String token) throws TokenExpiredException{
        Date expiration;
        try {
            final Claims claims = getClaimsFromToken(token);
            expiration = claims.getExpiration();

        } /*catch (Exception e) {*/
        catch(TokenExpiredException e){
            expiration = null;
            throw e;
        }
        System.out.println("token value: " + token);
        System.out.println("expiration value: " + expiration);
        return expiration;
    }

    /**
     * Return audience of a jwt token
     *
     * @param token the jwt token
     * @return the audience
     */
    public String getAudienceFromToken(String token) {
        String audience;
        try {
            final Claims claims = getClaimsFromToken(token);
            audience = (String) claims.get(CLAIM_KEY_AUDIENCE);
        } catch (Exception e) {
            audience = null;
        }
        return audience;
    }

    /**
     * Get claims from token
     *
     * @param token the jwt token
     * @return a Claims object
     */
    private Claims getClaimsFromToken(String token) throws TokenExpiredException{
        Claims claims;
        System.out.println("token in claims is: " + token);
        try {

            claims = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            System.out.println("claims expcetion");
            e.printStackTrace();
            claims = null;
            throw new TokenExpiredException("Expired session; please, go to login");
        }
        return claims;
    }

    /**
     * Generate a new expiration date for a token
     *
     * @return the expiration date
     */
    private Date generateExpirationDate() {
        Date d = new Date(System.currentTimeMillis() + expiration*1000);
        System.out.println("date = " + d);
        return d;
    }

    /**
     * Check if a token is expired              UTILIZZARE PER REFRESH SESSIONE!!!!!
     *
     * @param token the jwt token
     * @return true if token is expired, false otherwise
     */
    private Boolean isTokenExpired(String token){
        try {
            final Date expiration = getExpirationDateFromToken(token);
            System.out.println("date: " + expiration);
            System.out.println("token: " + token);
            boolean tokenExp = expiration.before(new Date());
            System.out.println("tokenExp = " + tokenExp);
            /*if(tokenExp==false){
                token = refreshToken(token);
                InterceptorConfig.setJwtToken(token);
            }*/
            return tokenExp;
        }catch (TokenExpiredException e){
            System.out.println("date exception");
            return true;
        }
    }

    /**
     * Generate audience string
     *
     * @param device the device used
     * @return a string representing the odience
     */
    private String generateAudience(Device device) {
        String audience = AUDIENCE_UNKNOWN;
        if (device.isNormal()) {
            audience = AUDIENCE_WEB;
        } else if (device.isTablet()) {
            audience = AUDIENCE_TABLET;
        } else if (device.isMobile()) {
            audience = AUDIENCE_MOBILE;
        }
        return audience;
    }

    /**
     * Ignore the token expiration if the device is a tablet or a mobile device
     * @param token the jwttoken
     * @return true if device is a tablet or mobile device, false otherwise
     */
    private Boolean ignoreTokenExpiration(String token) {
        String audience = getAudienceFromToken(token);
        System.out.println("audience getted:" + audience);
        return (AUDIENCE_TABLET.equals(audience) || AUDIENCE_MOBILE.equals(audience));
    }

    //public String re(UserDetails userDetails, Device device) throws JsonProcessingException {

    /**
     * Generate a new token from an object implementing userdetails
     *
     * @param userDetails the object implementing userdetails
     * @return the jwt token
     * @throws JsonProcessingException
     */
    public String generateToken(UserDetails userDetails) throws JsonProcessingException {
        //InterceptorConfig.setJwtTokenUtil(this);
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_USERNAME, userDetails.getUsername());
        claims.put(CLAIM_KEY_AUDIENCE, AUDIENCE_WEB);
        claims.put(CLAIM_KEY_CREATED, new Date());
        List<String> auth = userDetails.getAuthorities().stream().map(role -> role.getAuthority()).collect(Collectors.toList());
        claims.put(CLAIM_KEY_AUTHORITIES, auth);
        claims.put(CLAIM_KEY_IS_ENABLED, userDetails.isEnabled());
        System.out.println("called");
        return generateToken(claims);
    }

    /**
     * Generate a new token from a map of claims
     *
     * @param claims the map containing the claims
     * @return the jwt token generated
     */
    private String generateToken(Map<String, Object> claims) {
        System.out.println("called2");
        ObjectMapper mapper = new ObjectMapper();
        String jwtToken = Jwts.builder()
                .setClaims(claims)
                .setExpiration(generateExpirationDate())
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
        //InterceptorConfig.setJwtToken(jwtToken);
        return jwtToken;
    }

    /**
     * Check if a token can be refreshed
     *
     * @param token the jwt token to check
     * @return true if the token can be refreshed, false otherwise
     */
    public Boolean canTokenBeRefreshed(String token){
        try {
            final Date created = getCreatedDateFromToken(token);
            return (!isTokenExpired(token) || ignoreTokenExpiration(token));
        }catch(TokenExpiredException e){
            return false;
        }
    }

    /**
     * Refreshed a token a token
     *
     * @param token the jwt token to refresh
     * @return the new jwt token
     */
    public String refreshToken(String token) {
        String refreshedToken;
        try {
            final Claims claims = getClaimsFromToken(token);
            claims.put(CLAIM_KEY_CREATED, new Date());
            refreshedToken = generateToken(claims);
        } catch (Exception e) {
            refreshedToken = null;
        }
        return refreshedToken;
    }

    /**
     * Check if a token is valid
     *
     * @param token the token to check
     * @param userDetails
     * @return true if token is valid, false otherwise
     */
    public Boolean validateToken(String token, UserDetails userDetails) throws TokenExpiredException{
        JwtUser user = (JwtUser) userDetails;
        final String username = getUsernameFromToken(token);
        return (
                username.equals(user.getUsername())
                        && !isTokenExpired(token));
    }

}