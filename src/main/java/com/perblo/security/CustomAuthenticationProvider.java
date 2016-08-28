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
import org.apache.log4j.Logger;
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

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {
    private static final Logger log = Logger.getLogger(CustomAuthenticationProvider.class);
    
    @Autowired    
    private UserDAO userDAO;
    
    @Autowired
    LoginManager loginManager;

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
            user = userDAO.authenticateUser(userName, password);

            if (user == null) {
                throw new BadCredentialsException("Bad credentials");
            } else {                
                log.info("user: " + user.toString());
                List<GrantedAuthority> grantedAuths = new ArrayList<GrantedAuthority>();
                for (Role role : user.getUserRoles()) {
                    //grantedAuths.add(new SimpleGrantedAuthority(role.getRoleName().toUpperCase()));
                    log.info("role: " + role);
                    String newRole = "ROLE_" + role.getRoleName().toUpperCase();
                    grantedAuths.add(new SimpleGrantedAuthority(newRole));
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
}
