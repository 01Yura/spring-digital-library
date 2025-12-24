package online.ityura.springdigitallibrary.service;

import online.ityura.springdigitallibrary.dto.request.LoginRequest;
import online.ityura.springdigitallibrary.dto.request.RefreshTokenRequest;
import online.ityura.springdigitallibrary.dto.request.RegisterRequest;
import online.ityura.springdigitallibrary.dto.response.LoginResponse;
import online.ityura.springdigitallibrary.dto.response.RegisterResponse;
import online.ityura.springdigitallibrary.model.Role;
import online.ityura.springdigitallibrary.model.User;
import online.ityura.springdigitallibrary.repository.UserRepository;
import online.ityura.springdigitallibrary.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }
        
        User user = User.builder()
                .nickname(request.getNickname())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();
        
        user = userRepository.save(user);
        
        return RegisterResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
    
    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials");
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication failed");
        }
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        
        String accessToken = jwtTokenProvider.generateToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail(), user.getRole().name());
        
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();
    }
    
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        
        // Проверяем, что это действительно refresh токен
        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }
        
        // Извлекаем email из токена
        String email = jwtTokenProvider.extractUsername(refreshToken);
        
        // Проверяем, что пользователь существует
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        
        // Валидируем токен
        org.springframework.security.core.userdetails.UserDetails userDetails = 
                org.springframework.security.core.userdetails.User.builder()
                        .username(user.getEmail())
                        .password(user.getPasswordHash())
                        .authorities("ROLE_" + user.getRole().name())
                        .build();
        
        if (!jwtTokenProvider.validateToken(refreshToken, userDetails)) {
            throw new RuntimeException("Refresh token is expired or invalid");
        }
        
        // Генерируем новые токены
        String newAccessToken = jwtTokenProvider.generateToken(user.getEmail(), user.getRole().name());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail(), user.getRole().name());
        
        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .build();
    }
}

