package online.ityura.springdigitallibrary.service;

import online.ityura.springdigitallibrary.model.BookFile;
import online.ityura.springdigitallibrary.repository.BookFileRepository;
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
    private BookFileRepository bookFileRepository;
    
    @Autowired
    private BookRepository bookRepository;
    
    public Resource downloadBookFile(Long bookId) {
        // Проверяем существование книги
        if (!bookRepository.existsById(bookId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Book not found with id: " + bookId);
        }
        
        BookFile bookFile = bookFileRepository.findByBookId(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "File not found for book id: " + bookId));
        
        try {
            Path filePath = Paths.get(bookFile.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "File not found or not readable");
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error reading file: " + e.getMessage());
        }
    }
    
    public String getOriginalFilename(Long bookId) {
        BookFile bookFile = bookFileRepository.findByBookId(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "File not found for book id: " + bookId));
        
        return bookFile.getOriginalFilename() != null 
                ? bookFile.getOriginalFilename() 
                : "book_" + bookId + ".pdf";
    }
}

