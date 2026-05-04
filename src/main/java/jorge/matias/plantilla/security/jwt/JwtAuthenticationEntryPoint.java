package jorge.matias.plantilla.security.jwt;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.context.MessageSource;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import tools.jackson.databind.json.JsonMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jorge.matias.plantilla.controller.dto.response.ApiErrorResponse;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint{

    private final MessageSource messageSource;

    private final JsonMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, 
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        
        String message = messageSource.getMessage("auth.unauthorized", null, request.getLocale());
        String code = messageSource.getMessage("auth.unauthorized.code", null, request.getLocale());
        
        ApiErrorResponse res = new ApiErrorResponse(
            LocalDateTime.now(), 
            HttpServletResponse.SC_UNAUTHORIZED, 
            code, 
            message, 
            request.getRequestURI());

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        objectMapper.writeValue(response.getOutputStream(), res);
    }
}
