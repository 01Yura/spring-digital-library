package online.ityura.springdigitallibrary.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Старый вариант с безопасным алгоритмом хеширования паролей BCrypt:
        // return new BCryptPasswordEncoder();
        // Текущий вариант БЕЗ хеширования, пароли хранятся и сравниваются в открытом виде (только для локальных тестов, не использовать в продакшене):
        return NoOpPasswordEncoder.getInstance();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers(
                    "/swagger-ui/**", 
                    "/v3/api-docs/**", 
                    "/swagger-ui.html",
                    "/swagger-resources/**",
                    "/webjars/**",
                    "/swagger-ui/index.html"
                ).permitAll()
                // TODO: ВРЕМЕННО ОТКРЫТО БЕЗ АВТОРИЗАЦИИ - убрать эту строку для возврата требования авторизации
                .requestMatchers("/api/v1/admin/books/*/image").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                // Публичные эндпоинты для книг (более специфичные правила идут первыми)
                .requestMatchers("/api/v1/books").permitAll()
                .requestMatchers("/api/v1/books/images/all").permitAll()
                // Эндпоинты изображений - публичные (паттерн для /api/v1/books/{id}/image)
                .requestMatchers("/api/v1/books/*/image").permitAll()
                // GET запросы к списку отзывов - публичные
                .requestMatchers(HttpMethod.GET, "/api/v1/books/*/reviews").permitAll()
                // Эндпоинты, требующие авторизацию (используем * вместо ** в середине)
                .requestMatchers("/api/v1/books/*/ratings/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/v1/books/*/reviews/**").hasAnyRole("USER", "ADMIN")
                // Остальные эндпоинты книг (например, /api/v1/books/{id}) - публичные
                .requestMatchers("/api/v1/books/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}

