package online.ityura.springdigitallibrary.service;

import online.ityura.springdigitallibrary.dto.request.CreateBookRequest;
import online.ityura.springdigitallibrary.dto.request.UpdateBookRequest;
import online.ityura.springdigitallibrary.dto.response.AuthorResponse;
import online.ityura.springdigitallibrary.dto.response.BookResponse;
import online.ityura.springdigitallibrary.model.Author;
import online.ityura.springdigitallibrary.model.Book;
import online.ityura.springdigitallibrary.repository.AuthorRepository;
import online.ityura.springdigitallibrary.repository.BookRepository;
import online.ityura.springdigitallibrary.repository.ReviewRepository;
import online.ityura.springdigitallibrary.service.BookImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminBookService {
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private AuthorRepository authorRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private BookImageService bookImageService;
    
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
                .genre(request.getGenre())
                .deletionLocked(false)
                .build();
        
        book = bookRepository.save(book);
        return mapToBookResponse(book);
    }
    
    @Transactional
    public List<BookResponse> createBooks(List<CreateBookRequest> requests) {
        List<BookResponse> responses = new ArrayList<>();
        
        for (CreateBookRequest request : requests) {
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
                        "Book with title '" + request.getTitle() + "' and author '" + request.getAuthorName() + "' already exists");
            }
            
            Book book = Book.builder()
                    .title(request.getTitle())
                    .author(author)
                    .description(request.getDescription())
                    .publishedYear(request.getPublishedYear())
                    .genre(request.getGenre())
                    .deletionLocked(false)
                    .build();
            
            book = bookRepository.save(book);
            responses.add(mapToBookResponse(book));
        }
        
        return responses;
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
        if (request.getGenre() != null) {
            book.setGenre(request.getGenre());
        }
        
        book = bookRepository.save(book);
        return mapToBookResponse(book);
    }
    
    @Transactional
    public BookResponse patchBook(Long bookId, UpdateBookRequest request, MultipartFile imageFile) {
        // Проверка: нельзя изменять автора через PATCH
        if (request.getAuthorName() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Cannot change author: author modification is not allowed");
        }
        
        // Сначала обновляем поля книги
        BookResponse response = updateBook(bookId, request);
        
        // Если передан файл изображения, обновляем изображение
        if (imageFile != null && !imageFile.isEmpty()) {
            bookImageService.uploadBookImage(bookId, imageFile);
            // Получаем обновленную книгу для возврата
            Book book = bookRepository.findByIdWithAuthor(bookId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                            "Book not found with id: " + bookId));
            return mapToBookResponse(book);
        }
        
        return response;
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
        
        // Примечание: PDF файл хранится в файловой системе, 
        // его можно удалить физически, но для простоты оставляем как есть
        // (файл будет оставаться в файловой системе, но ссылка в БД удалится вместе с книгой)
        
        bookRepository.delete(book);
    }
    
    @Transactional
    public void deleteAuthorAndAllBooks(Long authorId) {
        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Author not found with id: " + authorId));
        
        // Находим все книги автора
        List<Book> books = bookRepository.findByAuthorId(authorId);
        
        // Для каждой книги проверяем ограничения и удаляем
        for (Book book : books) {
            Long bookId = book.getId();
            
            // Проверка deletion_locked
            if (book.getDeletionLocked()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                        "Cannot delete book with id " + bookId + ": deletion is locked");
            }
            
            // Проверка наличия связанных отзывов
            if (reviewRepository.countByBookId(bookId) > 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, 
                        "Cannot delete book with id " + bookId + ": it has reviews");
            }
            
            // Примечание: PDF файл хранится в файловой системе, 
            // его можно удалить физически, но для простоты оставляем как есть
            // (файл будет оставаться в файловой системе, но ссылка в БД удалится вместе с книгой)
            
            // Удаляем книгу через репозиторий (соблюдает все проверки)
            bookRepository.delete(book);
        }
        
        // Удаляем автора
        authorRepository.delete(author);
    }
    
    private BookResponse mapToBookResponse(Book book) {
        boolean hasFile = book.getPdfPath() != null && !book.getPdfPath().isEmpty();
        
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
                .imagePath(book.getImagePath())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }
}

