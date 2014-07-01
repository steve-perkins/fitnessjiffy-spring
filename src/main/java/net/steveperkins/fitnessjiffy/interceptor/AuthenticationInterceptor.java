package net.steveperkins.fitnessjiffy.interceptor;

import net.steveperkins.fitnessjiffy.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Component
public class AuthenticationInterceptor extends HandlerInterceptorAdapter {

    private static String AUTHENTICATION_COOKIE_NAME = "fitnessjiffy-session";

    @Autowired
    private AuthenticationService authenticationService;

    @Override
    public boolean preHandle(
            @Nonnull HttpServletRequest request,
            @Nonnull HttpServletResponse response,
            @Nonnull Object handler
    ) throws Exception {
        String sessionToken = retrieveSessionToken(request);
        UUID userId = authenticationService.validateSessionToken(sessionToken);
        if(userId == null) {
            response.sendRedirect("/login");
            return false;
        } else {
            request.setAttribute("userId", userId);
            return true;
        }
    }

    @Nullable
    private String retrieveSessionToken(@Nonnull HttpServletRequest request) {
        String token = null;
        for(Cookie cookie : request.getCookies()) {
            if(cookie.getName().equals(AUTHENTICATION_COOKIE_NAME)) {
                token = cookie.getValue();
                break;
            }
        }
        return token;
    }

}
