package com.rettichlp.redirect;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@Component
public class RedirectInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (request.getServerPort() != 8888) {
            return true;
        }

        String uri = request.getRequestURI();
        String queryParameters = Optional.ofNullable(request.getQueryString()).orElse("");

        String redirect = "https://rettichlp.de:8443" + uri + (!queryParameters.isBlank() ? "?" + queryParameters : "");
        response.sendRedirect(redirect);

        ServerApplication.LOGGER.info("Redirect {} with parameters {}", uri, queryParameters.replace("&", " and "));

        return false;
    }
}

