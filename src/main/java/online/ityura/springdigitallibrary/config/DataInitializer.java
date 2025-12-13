package online.ityura.springdigitallibrary.config;

import online.ityura.springdigitallibrary.model.Author;
import online.ityura.springdigitallibrary.model.Book;
import online.ityura.springdigitallibrary.model.Genre;
import online.ityura.springdigitallibrary.model.User;
import online.ityura.springdigitallibrary.repository.AuthorRepository;
import online.ityura.springdigitallibrary.repository.BookRepository;
import online.ityura.springdigitallibrary.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private AuthorRepository authorRepository;
    
    @Override
    public void run(String... args) throws Exception {
        // Проверяем, существует ли уже админ
        if (!userRepository.existsByEmail("admin@gmail.com")) {
            User admin = User.builder()
                    .nickname("Admin")
                    .email("admin@gmail.com")
                    .passwordHash(passwordEncoder.encode("admin"))
                    .role(User.Role.ADMIN)
                    .build();
            
            userRepository.save(admin);
            System.out.println("Admin user created successfully!");
        } else {
            System.out.println("Admin user already exists.");
        }
        
        // Инициализация книг
        initializeBooks();
    }
    
    private void initializeBooks() {
        // Массив данных о книгах
        BookData[] booksData = {
            new BookData("Spring Boot in Action", "Craig Walls", 
                "Practical introduction to building applications with Spring Boot", 2021, Genre.TECHNOLOGY),
            new BookData("Clean Code Explained", "Robert Martin", 
                "Principles and best practices of writing clean and maintainable code", 2019, Genre.EDUCATION),
            new BookData("The Lonely Developer", "Alex Turner", 
                "Reflections on life, work, and solitude in the IT industry", 2020, Genre.FOR_PEOPLE_WITHOUT_PERSONAL_LIFE),
            new BookData("Algorithms for Night Owls", "Eugene Knuth", 
                "Deep dive into algorithms for people who enjoy coding more than sleeping", 2018, Genre.FOR_NERDS),
            new BookData("Fantasy of the Forgotten Kingdom", "Lara Moon", 
                "Epic fantasy story about magic, dragons, and lost empires", 2015, Genre.FANTASY),
            new BookData("Psychology of Motivation", "Daniel Harper", 
                "How motivation works and how to stay productive", 2022, Genre.PSYCHOLOGY),
            new BookData("Business 101", "Michael Roberts", 
                "Basic concepts of modern business and management", 2017, Genre.BUSINESS),
            new BookData("Dark Corners", "Stephen Black", 
                "A collection of psychological horror stories", 2016, Genre.HORROR),
            new BookData("Romance Without Drama", "Emily Stone", 
                "A calm and realistic take on modern relationships", 2023, Genre.ROMANCE),
            new BookData("Adult Fiction Collection", "Anonymous Author", 
                "Fiction intended for mature audiences", 2014, Genre.PORNO)
        };
        
        int createdCount = 0;
        int existingCount = 0;
        
        for (BookData bookData : booksData) {
            // Находим или создаем автора
            Author author = authorRepository.findByFullName(bookData.authorName)
                    .orElseGet(() -> {
                        Author newAuthor = Author.builder()
                                .fullName(bookData.authorName)
                                .build();
                        return authorRepository.save(newAuthor);
                    });
            
            // Проверяем, существует ли уже книга с таким названием и автором
            if (!bookRepository.existsByTitleAndAuthorId(bookData.title, author.getId())) {
                Book book = Book.builder()
                        .title(bookData.title)
                        .author(author)
                        .description(bookData.description)
                        .publishedYear(bookData.publishedYear)
                        .genre(bookData.genre)
                        .deletionLocked(false)
                        .build();
                
                bookRepository.save(book);
                createdCount++;
            } else {
                existingCount++;
            }
        }
        
        System.out.println("Books initialization completed: " + createdCount + " created, " + existingCount + " already exist.");
    }
    
    // Вспомогательный класс для хранения данных о книге
    private static class BookData {
        String title;
        String authorName;
        String description;
        Integer publishedYear;
        Genre genre;
        
        BookData(String title, String authorName, String description, Integer publishedYear, Genre genre) {
            this.title = title;
            this.authorName = authorName;
            this.description = description;
            this.publishedYear = publishedYear;
            this.genre = genre;
        }
    }
}

