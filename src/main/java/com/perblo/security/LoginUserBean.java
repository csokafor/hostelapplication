/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.perblo.security;

import com.perblo.hostel.entity.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.perblo.security.LoginManager.LOGIN_USER;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;


/**
 *
 * @author cokafor
 */
@ManagedBean(name="loginUserBean")
@SessionScoped
public class LoginUserBean implements Serializable {
    private static final Logger log = LoggerFactory.getLogger(LoginUserBean.class);
    private LoginUser user;
    private boolean userLoggedIn;
    
    public LoginUserBean() {
        user = getCurrentUser();
        log.info("login user " + user);
    }
    
    public LoginUser getCurrentUser() {
        return (LoginUser) FacesContext.getCurrentInstance().
                getExternalContext().getSessionMap().get(LOGIN_USER);
    }
    
    public boolean userHasRole(String role) {
        return hasRole(role);
    }
    
    public boolean hasRole(String role) {
        boolean hasRole = false;
        if(user != null) {
            for(Role userRole : user.getRoles()) {            
                if(userRole.getRoleName().equalsIgnoreCase(role)) {
                    hasRole = true;
                }
            }
        }
        return hasRole;
    }

    public LoginUser getUser() {
        return user;
    }

    public void setUser(LoginUser user) {
        this.user = user;
    }
    
    public boolean isUserLoggedIn() {
        if (getCurrentUser() == null) {
            return false;
        } else {
            return getCurrentUser().getUserEmail() != null;
        }
    }
   
    public void setUserLoggedIn(boolean userLoggedIn) {
        this.userLoggedIn = userLoggedIn;
    }
}
