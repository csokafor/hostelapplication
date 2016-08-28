/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.perblo.security;

import com.perblo.hostel.entity.User;
import java.io.IOException;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import org.springframework.stereotype.Component;

@Component
public class LoginManager {

    private boolean userLoggedIn;
    public static final String LOGIN_USER = "LOGIN_USER";
    
    public void login(User user) {
        LoginUser loginUser = new LoginUser();
        loginUser.setFirstName(user.getFirstName());
        loginUser.setLastName(user.getLastName());
        loginUser.setUserName(user.getUserName());
        loginUser.setUserEmail(user.getEmail());
        loginUser.setUserId(user.getId());        
        loginUser.setRoles(user.getUserRoles());
        FacesContext.getCurrentInstance().
                getExternalContext().getSessionMap().put(LOGIN_USER, loginUser);
              
    }

    public void logout(FacesContext facesContext) throws IOException {
        ExternalContext ec = facesContext.getExternalContext();
        ec.invalidateSession();
        ec.redirect("/index.xhtml");
    }

    /**
     * @return the CurrentUser
     */
    public LoginUser getCurrentUser() {
        return (LoginUser) FacesContext.getCurrentInstance().
                getExternalContext().getSessionMap().get(LOGIN_USER);
    }
    
    
    /**
     * @return the userLoggedIn
     */
    public boolean isUserLoggedIn() {
        if (getCurrentUser() == null) {
            return false;
        } else {
            return getCurrentUser().getUserEmail() != null;
        }
    }

    /**
     * @param userLoggedIn the userLoggedIn to set
     */
    public void setUserLoggedIn(boolean userLoggedIn) {
        this.userLoggedIn = userLoggedIn;
    }

    public String getLoginURL() {
        return "/signin.xhtml";
    }
}
