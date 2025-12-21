package online.ityura.springdigitallibrary.unit.controller;

import online.ityura.springdigitallibrary.controller.BookFileController;
import online.ityura.springdigitallibrary.service.BookFileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookFileControllerTest {
    
    @Mock
    private BookFileService bookFileService;
    
    @InjectMocks
    private BookFileController bookFileController;
    
    @Test
    void testDownloadBook_Success_ShouldReturn200() {
        // Given
        Resource mockResource = new ByteArrayResource("test pdf content".getBytes());
        String filename = "test-book.pdf";
        
        when(bookFileService.downloadBookFile(1L)).thenReturn(mockResource);
        when(bookFileService.getOriginalFilename(1L)).thenReturn(filename);
        
        // When
        ResponseEntity<Resource> response = bookFileController.downloadBook(1L);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertTrue(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION).contains(filename));
    }
}

