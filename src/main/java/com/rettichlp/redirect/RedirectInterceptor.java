package com.rettichlp.redirect;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Log4j2
@Component
public class RedirectInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = URLDecoder.decode(request.getRequestURI(), StandardCharsets.UTF_8);
        if (!uri.contains("unicacityaddon/v1/")) {
            return true;
        }

        String queryParameters = Optional.ofNullable(request.getQueryString()).map(s -> URLDecoder.decode(s, StandardCharsets.UTF_8)).orElse("");
        String redirect = "https://rettichlp.de:8443" + uri + (!queryParameters.isBlank() ? "?" + queryParameters : "");

        HttpStatus statusCode;
        String responseBody;

        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(redirect, String.class);
            statusCode = responseEntity.getStatusCode();
            responseBody = Optional.ofNullable(responseEntity.getBody()).orElse("");
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            statusCode = e.getStatusCode();
            responseBody = e.getResponseBodyAsString();
        }

        if (!uri.contains("/authorize")) {
            log.info("Redirect set: {}{}{} [{}]", uri, !queryParameters.isBlank() ? "?" : "", queryParameters.replace("&", " and "), statusCode);
        }

        response.setStatus(statusCode.value());
        response.getWriter().write(responseBody);
        response.getWriter().flush();

        return false;
    }
}