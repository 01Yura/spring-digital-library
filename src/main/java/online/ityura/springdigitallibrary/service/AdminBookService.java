package online.ityura.springdigitallibrary.service;

import online.ityura.springdigitallibrary.dto.request.CreateBookRequest;
import online.ityura.springdigitallibrary.dto.request.UpdateBookRequest;
import online.ityura.springdigitallibrary.dto.response.AuthorResponse;
import online.ityura.springdigitallibrary.dto.response.BookResponse;
import online.ityura.springdigitallibrary.model.Author;
import online.ityura.springdigitallibrary.model.Book;
import online.ityura.springdigitallibrary.repository.AuthorRepository;
import online.ityura.springdigitallibrary.repository.BookFileRepository;
import online.ityura.springdigitallibrary.repository.BookRepository;
import online.ityura.springdigitallibrary.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminBookService {
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private AuthorRepository authorRepository;
    
    @Autowired
    private BookFileRepository bookFileRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Transactional
    public BookResponse createBook(CreateBookRequest request) {
        // Проверка уникальности (title, author)
        Author author = authorRepository.findByFullName(request.getAuthorName())
                .orElseGet(() -> {
                    Author newAuthor = Author.builder()
                            .fullName(request.getAuthorName())
                            .build();
                    return authorRepository.save(newAuthor);
                });
        
        if (bookRepository.existsByTitleAndAuthorId(request.getTitle(), author.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                    "Book with this title and author already exists");
        }
        
        Book book = Book.builder()
                .title(request.getTitle())
                .author(author)
                .description(request.getDescription())
                .publishedYear(request.getPublishedYear())
                .isbn(request.getIsbn())
                .deletionLocked(false)
                .build();
        
        book = bookRepository.save(book);
        return mapToBookResponse(book);
    }
    
    @Transactional
    public BookResponse updateBook(Long bookId, UpdateBookRequest request) {
        Book book = bookRepository.findByIdWithAuthor(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Book not found with id: " + bookId));
        
        // Обновление автора если нужно
        if (request.getAuthorName() != null && !request.getAuthorName().equals(book.getAuthor().getFullName())) {
            Author author = authorRepository.findByFullName(request.getAuthorName())
                    .orElseGet(() -> {
                        Author newAuthor = Author.builder()
                                .fullName(request.getAuthorName())
                                .build();
                        return authorRepository.save(newAuthor);
                    });
            
            // Проверка уникальности если меняется title или author
            if (request.getTitle() != null && !request.getTitle().equals(book.getTitle())) {
                if (bookRepository.existsByTitleAndAuthorId(request.getTitle(), author.getId())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, 
                            "Book with this title and author already exists");
                }
            }
            book.setAuthor(author);
        }
        
        if (request.getTitle() != null) {
            // Проверка уникальности если меняется только title
            if (!request.getTitle().equals(book.getTitle()) && 
                bookRepository.existsByTitleAndAuthorId(request.getTitle(), book.getAuthor().getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, 
                        "Book with this title and author already exists");
            }
            book.setTitle(request.getTitle());
        }
        
        if (request.getDescription() != null) {
            book.setDescription(request.getDescription());
        }
        if (request.getPublishedYear() != null) {
            book.setPublishedYear(request.getPublishedYear());
        }
        if (request.getIsbn() != null) {
            book.setIsbn(request.getIsbn());
        }
        
        book = bookRepository.save(book);
        return mapToBookResponse(book);
    }
    
    @Transactional
    public void deleteBook(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Book not found with id: " + bookId));
        
        // Проверка deletion_locked
        if (book.getDeletionLocked()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                    "Cannot delete book: deletion is locked");
        }
        
        // Проверка наличия связанных сущностей (можно сделать каскадное удаление или запрет)
        // Для безопасности - запрещаем удаление если есть отзывы
        if (reviewRepository.countByBookId(bookId) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                    "Cannot delete book: it has reviews");
        }
        
        // Удаление файла если есть
        bookFileRepository.findByBookId(bookId).ifPresent(bookFileRepository::delete);
        
        bookRepository.delete(book);
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
                .isbn(book.getIsbn())
                .ratingAvg(book.getRatingAvg())
                .ratingCount(book.getRatingCount())
                .hasFile(hasFile)
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }
}

