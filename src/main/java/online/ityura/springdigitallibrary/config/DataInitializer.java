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
                    .nickname("admin")
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
                new BookData("Spring Boot in Action", "Craig Walls", "Practical introduction to building applications with Spring Boot", 2021, Genre.TECHNOLOGY),
                new BookData("Clean Code Explained", "Robert Martin", "Principles and best practices of writing clean and maintainable code", 2019, Genre.EDUCATION),
                new BookData("The Lonely Developer", "Alex Turner", "Reflections on life, work, and solitude in the IT industry", 2020, Genre.FOR_PEOPLE_WITHOUT_PERSONAL_LIFE),
                new BookData("Algorithms for Night Owls", "Eugene Knuth", "Deep dive into algorithms for people who enjoy coding more than sleeping", 2018, Genre.FOR_NERDS),
                new BookData("Fantasy of the Forgotten Kingdom", "Lara Moon", "Epic fantasy story about magic, dragons, and lost empires", 2015, Genre.FANTASY),
                new BookData("Psychology of Motivation", "Daniel Harper", "How motivation works and how to stay productive", 2022, Genre.PSYCHOLOGY),
                new BookData("Business 101", "Michael Roberts", "Basic concepts of modern business and management", 2017, Genre.BUSINESS),
                new BookData("Dark Corners", "Stephen Black", "A collection of psychological horror stories", 2016, Genre.HORROR),
                new BookData("Romance Without Drama", "Emily Stone", "A calm and realistic take on modern relationships", 2023, Genre.ROMANCE),
                new BookData("Adult Fiction Collection", "Anonymous Author", "Fiction intended for mature audiences", 2014, Genre.PORNO),
                new BookData("Spring Boot Recipes", "Craig Walls", "Collection of practical recipes and patterns for Spring Boot projects", 2020, Genre.TECHNOLOGY),
                new BookData("Spring Security Basics", "Craig Walls", "Introduction to authentication and authorization concepts in Spring Security", 2022, Genre.TECHNOLOGY),
                new BookData("Refactoring Habits", "Robert Martin", "How to refactor code safely with focus on readability and design", 2016, Genre.EDUCATION),
                new BookData("Agile Team Rituals", "Robert Martin", "Team practices that help keep quality high and feedback loops short", 2018, Genre.BUSINESS),
                new BookData("The Weekend Coder", "Alex Turner", "Stories and lessons learned from side projects and late-night debugging", 2019, Genre.FOR_PEOPLE_WITHOUT_PERSONAL_LIFE),
                new BookData("Silence in the Open Space", "Alex Turner", "A short novel about burnout, focus, and finding meaning in routine work", 2021, Genre.PSYCHOLOGY),
                new BookData("Data Structures for Humans", "Eugene Knuth", "Data structures explained with real-life metaphors and examples", 2017, Genre.EDUCATION),
                new BookData("Complexity: A Love Story", "Eugene Knuth", "A nerdy and funny journey through Big-O and algorithmic tradeoffs", 2020, Genre.FOR_NERDS),
                new BookData("Dragons of the Northern Isles", "Lara Moon", "Fantasy adventure across icy seas and ancient ruins", 2016, Genre.FANTASY),
                new BookData("The Wizard's Last Contract", "Lara Moon", "A mage signs a dangerous deal to save a kingdom", 2019, Genre.FANTASY),
                new BookData("Mindset and Discipline", "Daniel Harper", "Practical techniques to build discipline and reduce procrastination", 2021, Genre.PSYCHOLOGY),
                new BookData("The Habit Loop", "Daniel Harper", "How habits are formed and how to redesign them for better results", 2018, Genre.PSYCHOLOGY),
                new BookData("Startup Finance Made Simple", "Michael Roberts", "Basics of budgeting, runway, and financial planning for startups", 2020, Genre.BUSINESS),
                new BookData("Management Without Micromanagement", "Michael Roberts", "How to lead teams with trust, clarity, and measurable outcomes", 2022, Genre.BUSINESS),
                new BookData("Shadows Under the Bed", "Stephen Black", "Horror tales about childhood fears returning in adult life", 2015, Genre.HORROR),
                new BookData("The House That Watches", "Stephen Black", "A psychological horror novel about a town with a secret", 2018, Genre.HORROR),
                new BookData("Love in Small Steps", "Emily Stone", "A gentle romance about building trust and emotional safety", 2020, Genre.ROMANCE),
                new BookData("Messages at Midnight", "Emily Stone", "Romantic drama told through letters, texts, and late-night calls", 2022, Genre.ROMANCE),
                new BookData("Intro to Software Testing", "Nina Carter", "Testing basics: test design, bugs, and working with requirements", 2019, Genre.EDUCATION),
                new BookData("API Testing Playbook", "Nina Carter", "Practical guide to REST API testing: cases, checks, and common pitfalls", 2023, Genre.EDUCATION),
                new BookData("Big-O Until Morning", "Eugene Knuth", "A true story about optimizing algorithms instead of sleeping", 2021, Genre.FOR_NERDS),
                new BookData("Hello World, My Old Friend", "Max Compiler", "Existential thoughts of a developer who prints logs for a living", 2019, Genre.FOR_NERDS),
                new BookData("404: Social Life Not Found", "Alex Turner", "Advanced guide to avoiding people using code, headphones, and deadlines", 2020, Genre.FOR_NERDS),
                new BookData("Refactor First, Ask Questions Later", "Robert Martin", "Why touching legacy code is scary but still unavoidable", 2018, Genre.FOR_NERDS),
                new BookData("Git Push and Pray", "Nina Carter", "A spiritual journey through force-pushes and broken pipelines", 2022, Genre.FOR_NERDS)
                new BookData("Fifty Shades of Pull Requests", "Anonymous Author", "A forbidden romance between a junior developer and production branch", 2016, Genre.PORNO),
                new BookData("Hardcoded Desires", "Johnny Runtime", "Passion, constants, and values that should never be in plain text", 2017, Genre.PORNO),
                new BookData("Backend After Midnight", "Lola Stacktrace", "Things get hot when the frontend finally stops calling", 2019, Genre.PORNO),
                new BookData("Deploy Me Gently", "Johnny Runtime", "A slow and careful release with unexpected rollbacks", 2021, Genre.PORNO),
                new BookData("Touch My Endpoint", "Lola Stacktrace", "A very insecure API exposed to the whole internet", 2023, Genre.PORNO)
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

