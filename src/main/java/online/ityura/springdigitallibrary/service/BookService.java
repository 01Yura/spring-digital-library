package online.ityura.springdigitallibrary.service;

import online.ityura.springdigitallibrary.dto.response.AuthorResponse;
import online.ityura.springdigitallibrary.dto.response.BookResponse;
import online.ityura.springdigitallibrary.dto.response.ReviewResponse;
import online.ityura.springdigitallibrary.dto.response.UserInfoResponse;
import online.ityura.springdigitallibrary.model.Book;
import online.ityura.springdigitallibrary.model.Review;
import online.ityura.springdigitallibrary.repository.BookFileRepository;
import online.ityura.springdigitallibrary.repository.BookRepository;
import online.ityura.springdigitallibrary.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookService {
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private BookFileRepository bookFileRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    public Page<BookResponse> getAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable)
                .map(this::mapToBookResponse);
    }
    
    public BookResponse getBookById(Long bookId) {
        Book book = bookRepository.findByIdWithAuthor(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found with id: " + bookId));
        
        // Загружаем отзывы для детальной информации о книге (с JOIN FETCH для избежания N+1 проблем)
        List<Review> reviews = reviewRepository.findByBookIdWithUserOrderByCreatedAtDesc(bookId);
        List<ReviewResponse> reviewResponses = reviews.stream()
                .map(this::mapToReviewResponse)
                .collect(Collectors.toList());
        
        return mapToBookResponseWithReviews(book, reviewResponses);
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
                .imagePath(book.getImagePath())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }
    
    private BookResponse mapToBookResponseWithReviews(Book book, List<ReviewResponse> reviews) {
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
                .imagePath(book.getImagePath())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .reviews(reviews)
                .build();
    }
    
    private ReviewResponse mapToReviewResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .bookId(review.getBook().getId())
                .user(UserInfoResponse.builder()
                        .id(review.getUser().getId())
                        .nickname(review.getUser().getNickname())
                        .email(review.getUser().getEmail())
                        .build())
                .text(review.getText())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}

