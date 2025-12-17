package online.ityura.springdigitallibrary.service;

import online.ityura.springdigitallibrary.model.Book;
import online.ityura.springdigitallibrary.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class BookFileService {
    
    @Autowired
    private BookRepository bookRepository;
    
    public Resource downloadBookFile(Long bookId) {
        // Получаем книгу из базы данных
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Book not found with id: " + bookId));
        
        // Проверяем, есть ли PDF файл у книги
        if (book.getPdfPath() == null || book.getPdfPath().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "PDF file not found for book id: " + bookId);
        }
        
        try {
            Path filePath = Paths.get(book.getPdfPath());
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "PDF file not found or not readable at path: " + book.getPdfPath());
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error reading PDF file: " + e.getMessage());
        }
    }
    
    public String getOriginalFilename(Long bookId) {
        // Получаем книгу из базы данных
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Book not found with id: " + bookId));
        
        // Проверяем, есть ли PDF файл у книги
        if (book.getPdfPath() == null || book.getPdfPath().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "PDF file not found for book id: " + bookId);
        }
        
        // Извлекаем имя файла из пути
        Path filePath = Paths.get(book.getPdfPath());
        String filename = filePath.getFileName().toString();
        
        // Если имя файла не найдено, формируем на основе названия книги
        if (filename == null || filename.isEmpty()) {
            filename = book.getTitle()
                    .replaceAll("\\s+", "_")
                    .replaceAll("[<>:\"|?*]", "") + ".pdf";
        }
        
        return filename;
    }
}

