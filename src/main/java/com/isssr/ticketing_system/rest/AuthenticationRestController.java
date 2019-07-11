package com.isssr.ticketing_system.rest;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.isssr.ticketing_system.acl.groups.Group;
import com.isssr.ticketing_system.controller.GroupController;
import com.isssr.ticketing_system.controller.UserController;
import com.isssr.ticketing_system.enumeration.UserRole;
import com.isssr.ticketing_system.entity.SoftDelete.SoftDelete;
import com.isssr.ticketing_system.entity.SoftDelete.SoftDeleteKind;
import com.isssr.ticketing_system.exception.EntityNotFoundException;
import com.isssr.ticketing_system.exception.UsernameNotFoundException;
import com.isssr.ticketing_system.jwt.JwtAuthenticationRequest;
import com.isssr.ticketing_system.jwt.JwtTokenUtil;
import com.isssr.ticketing_system.jwt.service.JwtAuthenticationResponse;
import com.isssr.ticketing_system.jwt.service.JwtUserDetailsServiceImpl;
import com.isssr.ticketing_system.logger.aspect.LogOperation;
import com.isssr.ticketing_system.entity.User;
import com.isssr.ticketing_system.response_entity.ResponseEntityBuilder;
import com.mchange.io.FileUtils;
import com.wordnik.swagger.annotations.ApiOperation;
import org.aspectj.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import static org.springframework.http.HttpStatus.CREATED;

/**
 * This class contains method for user login and registration
 */
@RestController
@ComponentScan
@CrossOrigin(origins = "*")
public class AuthenticationRestController {

    @Value("${jwt.header}")
    private String tokenHeader;

    private AuthenticationManager authenticationManager;
    private JwtTokenUtil jwtTokenUtil;
    private JwtUserDetailsServiceImpl userDetailsService;
    private UserController userController;
    private GroupController groupController;

    @Autowired
    public AuthenticationRestController(
            AuthenticationManager authenticationManager,
            JwtTokenUtil jwtTokenUtil,
            JwtUserDetailsServiceImpl userDetailsService,
            UserController userController,
            GroupController groupController
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
        this.userController = userController;
        this.groupController = groupController;
    }

    /**
     * Endpoint for login
     *
     * @param authenticationRequest the authentication request
     * @param response the http response
     * @return the jwt token
     * @throws AuthenticationException
     * @throws JsonProcessingException
     */
    @PostMapping("public/login")
    @LogOperation(inputArgs = {"authenticationRequest"}, returnObject = false, tag = "loginAttempt", opName = "createAuthenticationToken")
    @SoftDelete(SoftDeleteKind.NOT_DELETED)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtAuthenticationRequest authenticationRequest,
                                                       HttpServletResponse response)
            throws AuthenticationException, JsonProcessingException {
        // Effettuo l'autenticazione
        System.out.println("creating authentication token");
        try {
            final Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationRequest.getUsername(),
                            authenticationRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Genero Token
            final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());

            User u = null;
            try {
                u = userController.findUserByUsername(userDetails.getUsername());
            } catch (EntityNotFoundException e) {
                throw new UsernameNotFoundException("Username not found");
            }


            UserRole userRoles = u.getRole();


            final String token = jwtTokenUtil.generateToken(userDetails);
            response.setHeader("Access-Control-Expose-Headers", "Authorization");
            response.setHeader(tokenHeader, token);
            // Ritorno il token
            return new ResponseEntity<>(
                    new JwtAuthenticationResponse(
                            u.getId(),
                            userDetails.getUsername(),
                            userDetails.getAuthorities(),
                            userRoles
                    ), HttpStatus.OK);
        } catch (AuthenticationException e) {
            throw e;
        }
    }

    /**
     * Endpoint for customer registration
     *
     * @param user the user to register
     * @return the user registered or en HTTP error message
     */
    @PostMapping("public/register")
    @ResponseStatus(CREATED)
    @ApiOperation(value = "Creates a new Views",
            notes = "The newly created user will return in response body")
    //@JsonView(Views.Internal.class)
    @SoftDelete(SoftDeleteKind.NOT_DELETED)
    public ResponseEntity<User> registerUser(@RequestBody User user) {
        if (userController.existsByUsername(user.getUsername()))
            return new ResponseEntity<>(HttpStatus.CONFLICT);

        UserRole role = UserRole.CUSTOMER;

        user.setRole(role);
        User registeredUser = userController.insertUser(user);

        Group g = groupController.getGroupByName("GRUPPO CUSTOMER");
        g.addMember(user);
        groupController.updateGroup(g.getId(), g);

        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }

    /**
     * Refresh a jwt token and return a new token
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @return the new token
     */
    @GetMapping("protected/refresh-token")
    public ResponseEntity<?> refreshAndGetAuthenticationToken(HttpServletRequest request, HttpServletResponse response) {
        String token = request.getHeader(tokenHeader);
        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User u = null;
        try {
            u = userController.findUserByUsername(userDetails.getUsername());
        } catch (EntityNotFoundException e) {
            throw new UsernameNotFoundException("Username not found");
        }

        UserRole userRole = u.getRole();

        if (jwtTokenUtil.canTokenBeRefreshed(token)) {
            String refreshedToken = jwtTokenUtil.refreshToken(token);
            response.setHeader(tokenHeader, refreshedToken);

            return ResponseEntity.ok(
                    new JwtAuthenticationResponse(
                            u.getId(),
                            userDetails.getUsername(),
                            userDetails.getAuthorities(),
                            userRole
                    ));
        } else {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /*
    @RequestMapping(value = "protected/revoke-token", method = RequestMethod.GET)
    public ResponseEntity logout(@RequestHeader(value = "Authorization") String authorization) {
        try {
            String tokenValue = authorization.replace("Bearer", "").trim();
            this.tokenServices.revokeToken(tokenValue);
            return CommonResponseEntity.OkResponseEntity("SUCCESS");
        } catch (Exception e) {
            return CommonResponseEntity.OkResponseEntity("ERROR");
        }
    }
    */

    @RequestMapping(path = "public/perm", method = RequestMethod.GET)
    public ResponseEntity<String> sendFilejson() {

        ClassPathResource jsonFile = new ClassPathResource("permission.json");
//        InputStreamResource r;
        String jsonData;
        try {
//            r = new InputStreamResource(jsonFile.getInputStream());
            jsonData = FileUtil.readAsString(jsonFile.getFile());
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntityBuilder<>(jsonData).setStatus(HttpStatus.OK).build();
    }
}
