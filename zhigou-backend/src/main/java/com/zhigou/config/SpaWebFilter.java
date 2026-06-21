package com.zhigou.config;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SpaWebFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();

        // API、actuator、静态资源（带扩展名）不拦截
        if (path.startsWith("/api/") || path.startsWith("/actuator/") || path.contains(".")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 其他所有路径转发到 index.html，交由 Vue Router 处理
        request.getRequestDispatcher("/index.html").forward(request, response);
    }
}
