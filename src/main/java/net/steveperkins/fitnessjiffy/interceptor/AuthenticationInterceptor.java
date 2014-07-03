package net.steveperkins.fitnessjiffy.interceptor;

import com.google.common.base.Optional;
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
public final class AuthenticationInterceptor extends HandlerInterceptorAdapter {

    private static final String AUTHENTICATION_COOKIE_NAME = "fitnessjiffy-session";

    @Autowired
    private AuthenticationService authenticationService;

    @Override
    public boolean preHandle(
            @Nonnull final HttpServletRequest request,
            @Nonnull final HttpServletResponse response,
            @Nonnull final Object handler
    ) throws Exception {
        final String sessionToken = retrieveSessionToken(request);
        final UUID userId = (sessionToken == null) ? null : authenticationService.validateSessionToken(sessionToken);
        if (userId == null) {
            response.sendRedirect("/login");
            return false;
        } else {
            request.setAttribute("userId", userId);
            return true;
        }
    }

    @Nullable
    private String retrieveSessionToken(@Nonnull final HttpServletRequest request) {
        String token = null;
        final Cookie[] cookies = Optional.fromNullable(request.getCookies()).or(new Cookie[0]);
        for (final Cookie cookie : cookies) {
            if (cookie.getName().equals(AUTHENTICATION_COOKIE_NAME)) {
                token = cookie.getValue();
                break;
            }
        }
        return token;
    }

}
