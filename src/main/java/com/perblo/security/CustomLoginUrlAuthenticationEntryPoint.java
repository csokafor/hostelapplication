package com.perblo.security;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jboss.logging.Logger;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.RedirectUrlBuilder;
import org.springframework.security.web.util.UrlUtils;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


public class CustomLoginUrlAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint{
    private static final Logger log = Logger.getLogger(CustomLoginUrlAuthenticationEntryPoint.class);

    public CustomLoginUrlAuthenticationEntryPoint(String loginFormUrl) {
        super(loginFormUrl);
    }
      
   
    @Override
    protected String buildRedirectUrlToLoginPage(HttpServletRequest request,
			HttpServletResponse response, AuthenticationException authException) {

        String loginForm = determineUrlToUseForThisRequest(request, response,
                        authException);

        if (UrlUtils.isAbsoluteUrl(loginForm)) {
                return loginForm;
        }

        int serverPort = getPortResolver().getServerPort(request);
        String scheme = request.getScheme();

        RedirectUrlBuilder urlBuilder = new RedirectUrlBuilder();

        urlBuilder.setScheme(scheme);
        urlBuilder.setServerName(request.getServerName());
        urlBuilder.setPort(serverPort);
      //  urlBuilder.setContextPath(request.getContextPath());
       urlBuilder.setPathInfo(loginForm);

        if (isForceHttps() && "http".equals(scheme)) {
            Integer httpsPort = getPortMapper().lookupHttpsPort(Integer.valueOf(serverPort));

            if (httpsPort != null) {
                // Overwrite scheme and port in the redirect URL
                urlBuilder.setScheme("https");
                urlBuilder.setPort(httpsPort.intValue());
            }
            else {
                log.warn("Unable to redirect to HTTPS as no port mapping found for HTTP port "
                                    + serverPort);
            }
        }
       return urlBuilder.getUrl();
    }
}
