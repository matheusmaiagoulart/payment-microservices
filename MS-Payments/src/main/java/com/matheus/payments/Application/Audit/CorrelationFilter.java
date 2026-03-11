package com.matheus.payments.Application.Audit;

import jakarta.servlet.*;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CorrelationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        try {
            // Before doFilter(), always executed first, before the request is sent to the controller
            CorrelationId.generate();
            filterChain.doFilter(request, response);
            // After doFilter(), always executed last, after the response came from controller
        } finally {
            CorrelationId.clear();
        }
    }
}
