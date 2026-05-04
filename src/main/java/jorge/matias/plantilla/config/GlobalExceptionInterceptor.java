package jorge.matias.plantilla.config;

import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.RequiredArgsConstructor;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionInterceptor  {
    
    private final MessageSource messageSource;
}
