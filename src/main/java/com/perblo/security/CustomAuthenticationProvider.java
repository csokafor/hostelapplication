/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.perblo.security;


import com.perblo.hostel.entity.Role;
import com.perblo.hostel.entity.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.perblo.hostel.entitymanager.HostelEntityManager;
import com.perblo.hostel.entitymanager.HostelEntityManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.stereotype.Component;

import javax.persistence.Query;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {
    private static final Logger log = LoggerFactory.getLogger(CustomAuthenticationProvider.class);

    @Autowired
    LoginManager loginManager;

    @Autowired
    HostelEntityManager hostelEntityManager;

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws org.springframework.security.core.AuthenticationException {
        String userName = authentication.getName();
        String password = authentication.getCredentials().toString();
        User user;
        Authentication auth = null;
        try {
            log.info("userName: " + userName);
            user = authenticateUser(userName, password);

            if (user == null) {
                throw new BadCredentialsException("Bad credentials");
            } else {                
                log.info("user: " + user.toString());
                List<GrantedAuthority> grantedAuths = new ArrayList<GrantedAuthority>();
                for (Role role : user.getUserRoles()) {
                    grantedAuths.add(new SimpleGrantedAuthority(role.getRoleName().toUpperCase()));
                    log.info("role: " + role);
                }
                auth = new UsernamePasswordAuthenticationToken(userName, password, grantedAuths);
                loginManager.login(user);
            }
        } catch (Exception ex) {           
            log.error("authenticate exception: " + ex.getMessage());
            throw new BadCredentialsException(ex.getMessage());            
        }
        return auth;
    }

    public User authenticateUser(String userName, String password) {
        User user = null;
        try {
            Query query = hostelEntityManager.getEntityManager().createNamedQuery("getUserByUsername");
            query.setParameter(1, userName);
            List results = query.getResultList();
            log.info("getUserByUsername result: " + results.size());
            if (results.size() > 0) {
                user = (User)results.get(0);
                if(user.getUserPassword().equals(password)) {
                    log.info("correct user password");
                } else {
                    user = null;
                    log.warn("incorrect user password");
                }
            } else {
                log.warn("user not found");
            }
        } catch(Exception e) {
            log.error("authenticateUser: " + e.getMessage(), e);
        }

        return user;
    }

    public Set<Role> getUserRoles(long userId) {
        Set<Role> userRoles = null;
        User user = hostelEntityManager.getEntityManager().find(User.class, userId);
        userRoles = user.getUserRoles();

        return userRoles;
    }

}
