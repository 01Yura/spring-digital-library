package online.ityura.springdigitallibrary.config;

import online.ityura.springdigitallibrary.model.User;
import online.ityura.springdigitallibrary.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Проверяем, существует ли уже админ
        if (!userRepository.existsByEmail("admin@gmail.com")) {
            User admin = User.builder()
                    .nickname("Admin")
                    .email("admin@gmail.com")
                    .passwordHash(passwordEncoder.encode("admin"))
                    .role(User.Role.ADMIN)
                    .build();
            
            userRepository.save(admin);
            System.out.println("Admin user created successfully!");
        } else {
            System.out.println("Admin user already exists.");
        }
    }
}

