package online.ityura.springdigitallibrary.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        
        // Пропускаем Swagger эндпоинты без проверки JWT
        if (requestPath.startsWith("/swagger-ui") || 
            requestPath.startsWith("/v3/api-docs") || 
            requestPath.startsWith("/swagger-ui.html") ||
            requestPath.equals("/swagger-ui/index.html") ||
            requestPath.startsWith("/swagger-resources") ||
            requestPath.startsWith("/webjars")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Пропускаем публичные эндпоинты книг без проверки JWT
        if (requestPath.startsWith("/api/v1/books") && 
            (requestPath.equals("/api/v1/books") || 
             requestPath.matches("/api/v1/books/\\d+/image") ||
             requestPath.equals("/api/v1/books/images/all") ||
             (request.getMethod().equals("GET") && requestPath.matches("/api/v1/books/\\d+/reviews")))) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                username = jwtTokenProvider.extractUsername(token);
            } catch (Exception e) {
                // Invalid token
            }
        }
        
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            if (jwtTokenProvider.validateToken(token, userDetails)) {
                String role = jwtTokenProvider.getRoleFromToken(token);
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        
        filterChain.doFilter(request, response);
    }
}

