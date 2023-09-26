package com.rettichlp.redirect;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
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

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(redirect, String.class);

        HttpStatus statusCode = responseEntity.getStatusCode();
        String responseBody = Optional.ofNullable(responseEntity.getBody()).orElse("");

        ServerApplication.LOGGER.info("Redirect set: {} {} [{}]", uri, "with parameters " + queryParameters.replace("&", " and "), statusCode);

        response.setStatus(statusCode.value());
        response.getWriter().write(responseBody);
        response.getWriter().flush();

        return false;
    }
}