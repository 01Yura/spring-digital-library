package online.ityura.springdigitallibrary.service;

import online.ityura.springdigitallibrary.dto.response.AuthorResponse;
import online.ityura.springdigitallibrary.dto.response.BookResponse;
import online.ityura.springdigitallibrary.model.Book;
import online.ityura.springdigitallibrary.repository.BookFileRepository;
import online.ityura.springdigitallibrary.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class BookService {
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private BookFileRepository bookFileRepository;
    
    public Page<BookResponse> getAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable)
                .map(this::mapToBookResponse);
    }
    
    public BookResponse getBookById(Long bookId) {
        Book book = bookRepository.findByIdWithAuthor(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));
        return mapToBookResponse(book);
    }
    
    private BookResponse mapToBookResponse(Book book) {
        boolean hasFile = bookFileRepository.existsByBookId(book.getId());
        
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(AuthorResponse.builder()
                        .id(book.getAuthor().getId())
                        .fullName(book.getAuthor().getFullName())
                        .build())
                .description(book.getDescription())
                .publishedYear(book.getPublishedYear())
                .genre(book.getGenre())
                .ratingAvg(book.getRatingAvg())
                .ratingCount(book.getRatingCount())
                .hasFile(hasFile)
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }
}

