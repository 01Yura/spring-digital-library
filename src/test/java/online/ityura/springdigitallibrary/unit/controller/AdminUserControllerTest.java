package online.ityura.springdigitallibrary.unit.controller;

import online.ityura.springdigitallibrary.controller.AdminUserController;
import online.ityura.springdigitallibrary.dto.response.AdminUserResponse;
import online.ityura.springdigitallibrary.model.User;
import online.ityura.springdigitallibrary.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserControllerTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private AdminUserController adminUserController;
    
    private User testUser;
    private User testAdminUser;
    
    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .nickname("testuser")
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .role(User.Role.USER)
                .createdAt(LocalDateTime.now())
                .build();
        
        testAdminUser = User.builder()
                .id(2L)
                .nickname("admin")
                .email("admin@example.com")
                .passwordHash("hashedPassword")
                .role(User.Role.ADMIN)
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    @Test
    void testGetAllUsers_Success_ShouldReturn200() {
        // Given
        List<User> users = List.of(testUser, testAdminUser);
        when(userRepository.findAll()).thenReturn(users);
        
        // When
        ResponseEntity<List<AdminUserResponse>> response = adminUserController.getAllUsers();
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        
        AdminUserResponse firstUser = response.getBody().get(0);
        assertEquals(1L, firstUser.getId());
        assertEquals("testuser", firstUser.getNickname());
        assertEquals("test@example.com", firstUser.getEmail());
        assertEquals(User.Role.USER, firstUser.getRole());
        
        AdminUserResponse secondUser = response.getBody().get(1);
        assertEquals(2L, secondUser.getId());
        assertEquals("admin", secondUser.getNickname());
        assertEquals("admin@example.com", secondUser.getEmail());
        assertEquals(User.Role.ADMIN, secondUser.getRole());
        
        verify(userRepository).findAll();
    }
    
    @Test
    void testGetAllUsers_EmptyList_ShouldReturn200() {
        // Given
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        
        // When
        ResponseEntity<List<AdminUserResponse>> response = adminUserController.getAllUsers();
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        
        verify(userRepository).findAll();
    }
    
    @Test
    void testDeleteUser_Success_ShouldReturn204() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(any(User.class));
        
        // When
        ResponseEntity<Void> response = adminUserController.deleteUser(1L);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        
        verify(userRepository).findById(1L);
        verify(userRepository).delete(testUser);
    }
    
    @Test
    void testDeleteUser_UserNotFound_ShouldThrow404() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> adminUserController.deleteUser(999L));
        
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("User not found with id: 999"));
        
        verify(userRepository).findById(999L);
        verify(userRepository, never()).delete(any(User.class));
    }
    
    @Test
    void testDeleteUser_AdminUser_ShouldThrow403() {
        // Given
        when(userRepository.findById(2L)).thenReturn(Optional.of(testAdminUser));
        
        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> adminUserController.deleteUser(2L));
        
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Cannot delete user with ADMIN role"));
        
        verify(userRepository).findById(2L);
        verify(userRepository, never()).delete(any(User.class));
    }
}

