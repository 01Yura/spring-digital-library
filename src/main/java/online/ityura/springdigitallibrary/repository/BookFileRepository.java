package online.ityura.springdigitallibrary.repository;

import online.ityura.springdigitallibrary.model.BookFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookFileRepository extends JpaRepository<BookFile, Long> {
    Optional<BookFile> findByBookId(Long bookId);
    boolean existsByBookId(Long bookId);
}

