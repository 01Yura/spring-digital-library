package online.ityura.springdigitallibrary.repository;

import online.ityura.springdigitallibrary.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByTitleAndAuthorId(String title, Long authorId);
    boolean existsByTitleAndAuthorId(String title, Long authorId);
    
    @Query("SELECT b FROM Book b JOIN FETCH b.author WHERE b.id = :id")
    Optional<Book> findByIdWithAuthor(@Param("id") Long id);
}

