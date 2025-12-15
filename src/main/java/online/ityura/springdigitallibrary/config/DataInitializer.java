package online.ityura.springdigitallibrary.config;

import online.ityura.springdigitallibrary.model.Author;
import online.ityura.springdigitallibrary.model.Book;
import online.ityura.springdigitallibrary.model.Genre;
import online.ityura.springdigitallibrary.model.Rating;
import online.ityura.springdigitallibrary.model.Review;
import online.ityura.springdigitallibrary.model.User;
import online.ityura.springdigitallibrary.repository.AuthorRepository;
import online.ityura.springdigitallibrary.repository.BookRepository;
import online.ityura.springdigitallibrary.repository.RatingRepository;
import online.ityura.springdigitallibrary.repository.ReviewRepository;
import online.ityura.springdigitallibrary.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

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

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${app.images.storage-path}")
    private String storagePath;

    private final Random random = new Random();

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
        
        // Обработка картинок из папки pictures
        processBookImages();
        
        // Добавление смешных отзывов к каждой второй книге
        addFunnyReviews();
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
                new BookData("Complexity. A Love Story", "Eugene Knuth", "A nerdy and funny journey through Big-O and algorithmic tradeoffs", 2020, Genre.FOR_NERDS),
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
                new BookData("Git Push and Pray", "Nina Carter", "A spiritual journey through force-pushes and broken pipelines", 2022, Genre.FOR_NERDS),
                new BookData("Fifty Shades of Pull Requests", "Anonymous Author", "A forbidden romance between a junior developer and production branch", 2016, Genre.PORNO),
                new BookData("Hardcoded Desires", "Johnny Runtime", "Passion, constants, and values that should never be in plain text", 2017, Genre.PORNO),
                new BookData("Backend After Midnight", "Lola Stacktrace", "Things get hot when the frontend finally stops calling", 2019, Genre.PORNO),
                new BookData("Deploy Me Gently", "Johnny Runtime", "A slow and careful release with unexpected rollbacks", 2021, Genre.PORNO),
                new BookData("Touch My Endpoint", "Lola Stacktrace", "A very insecure API exposed to the whole internet", 2023, Genre.PORNO)
        };


        int createdCount = 0;
        int existingCount = 0;
        List<Book> savedBooks = new ArrayList<>();

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
            Book book;
            if (!bookRepository.existsByTitleAndAuthorId(bookData.title, author.getId())) {
                book = Book.builder()
                        .title(bookData.title)
                        .author(author)
                        .description(bookData.description)
                        .publishedYear(bookData.publishedYear)
                        .genre(bookData.genre)
                        .deletionLocked(false)
                        .build();

                book = bookRepository.save(book);
                createdCount++;
            } else {
                book = bookRepository.findByTitleAndAuthorId(bookData.title, author.getId())
                        .orElse(null);
                existingCount++;
            }
            
            if (book != null) {
                savedBooks.add(book);
            }
        }

        System.out.println("Books initialization completed: " + createdCount + " created, " + existingCount + " already exist.");
        
        // Сохраняем список книг для последующего добавления отзывов
        this.savedBooks = savedBooks;
    }
    
    private void processBookImages() {
        try {
            // Получаем путь к папке pictures в resources
            Resource picturesResource = resourceLoader.getResource("classpath:pictures");
            Path picturesPath = null;
            
            try {
                if (picturesResource.exists()) {
                    // Пытаемся получить путь как файл (работает в IDE и при запуске из файловой системы)
                    java.io.File file = picturesResource.getFile();
                    if (file.exists() && file.isDirectory()) {
                        picturesPath = file.toPath();
                    }
                }
            } catch (Exception e) {
                // Если не удалось получить как файл (например, в JAR), пробуем альтернативный путь
            }
            
            // Альтернативный способ получения пути (для разработки)
            if (picturesPath == null || !Files.exists(picturesPath)) {
                picturesPath = Paths.get("src/main/resources/pictures");
                if (!Files.exists(picturesPath)) {
                    System.out.println("Pictures directory not found. Skipping image processing.");
                    return;
                }
            }
            
            // Создаем папку для хранения изображений если её нет
            Path storageDir = Paths.get(storagePath);
            if (!Files.exists(storageDir)) {
                Files.createDirectories(storageDir);
                System.out.println("Created storage directory: " + storagePath);
            }
            
            // Получаем все книги из базы данных
            List<Book> allBooks = bookRepository.findAll();
            
            AtomicInteger processedCount = new AtomicInteger(0);
            AtomicInteger matchedCount = new AtomicInteger(0);
            
            // Проходим по всем файлам в папке pictures
            if (Files.exists(picturesPath) && Files.isDirectory(picturesPath)) {
                try (java.util.stream.Stream<Path> files = Files.list(picturesPath)) {
                    files.filter(Files::isRegularFile)
                        .forEach(imageFile -> {
                        try {
                            String imageFileName = imageFile.getFileName().toString();
                            // Убираем расширение для сравнения
                            String imageNameWithoutExt = imageFileName;
                            int lastDotIndex = imageFileName.lastIndexOf('.');
                            if (lastDotIndex > 0) {
                                imageNameWithoutExt = imageFileName.substring(0, lastDotIndex);
                            }
                            
                            // Нормализуем имя файла: заменяем _ на пробелы, приводим к нижнему регистру
                            String normalizedImageName = imageNameWithoutExt
                                .replaceAll("_", " ")
                                .replaceAll("\\s+", " ") // Нормализуем множественные пробелы
                                .toLowerCase()
                                .trim();
                            
                            // Ищем соответствующую книгу
                            for (Book book : allBooks) {
                                // Пропускаем книги, у которых уже есть изображение
                                if (book.getImagePath() != null && !book.getImagePath().isEmpty()) {
                                    continue;
                                }
                                
                                // Нормализуем название книги: приводим к нижнему регистру, нормализуем пробелы
                                String bookTitleNormalized = book.getTitle()
                                    .replaceAll("\\s+", " ") // Нормализуем множественные пробелы
                                    .toLowerCase()
                                    .trim();
                                
                                // Сравниваем нормализованные имена
                                if (normalizedImageName.equals(bookTitleNormalized)) {
                                    try {
                                        // Получаем расширение файла
                                        String extension = "";
                                        if (lastDotIndex > 0) {
                                            extension = imageFileName.substring(lastDotIndex);
                                        }
                                        
                                        // Формируем имя файла для сохранения (на основе названия книги)
                                        // Заменяем пробелы на _, но сохраняем все символы включая русские
                                        String sanitizedTitle = book.getTitle()
                                            .replaceAll("\\s+", "_")
                                            .replaceAll("[<>:\"|?*]", ""); // Удаляем только недопустимые символы для Windows/Linux
                                        String targetFileName = sanitizedTitle + extension;
                                        
                                        // Путь для сохранения
                                        Path targetPath = storageDir.resolve(targetFileName);
                                        
                                        // Если файл уже существует, добавляем UUID
                                        if (Files.exists(targetPath)) {
                                            String baseName = sanitizedTitle;
                                            String uuid = java.util.UUID.randomUUID().toString().substring(0, 8);
                                            targetFileName = baseName + "_" + uuid + extension;
                                            targetPath = storageDir.resolve(targetFileName);
                                        }
                                        
                                        // Копируем файл
                                        Files.copy(imageFile, targetPath, StandardCopyOption.REPLACE_EXISTING);
                                        
                                        // Обновляем путь к изображению в базе данных
                                        book.setImagePath(targetPath.toString());
                                        bookRepository.save(book);
                                        
                                        System.out.println("Matched and copied image for book: " + book.getTitle() + " -> " + targetFileName);
                                        matchedCount.incrementAndGet();
                                        
                                    } catch (IOException e) {
                                        System.err.println("Failed to copy image for book " + book.getTitle() + ": " + e.getMessage());
                                    }
                                    break; // Нашли соответствие, переходим к следующему файлу
                                }
                            }
                            processedCount.incrementAndGet();
                        } catch (Exception e) {
                            System.err.println("Error processing image file " + imageFile.getFileName() + ": " + e.getMessage());
                        }
                    });
                } catch (IOException e) {
                    System.err.println("Error listing files in pictures directory: " + e.getMessage());
                }
            }
            
            System.out.println("Image processing completed: " + processedCount.get() + " files processed, " + matchedCount.get() + " images matched and copied.");
            
        } catch (Exception e) {
            System.err.println("Error during image processing: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private List<Book> savedBooks;
    
    private void addFunnyReviews() {
        // Получаем или создаем тестовых пользователей для отзывов
        User reviewer1 = getOrCreateUser("funny_reviewer_1", "funny1@example.com");
        User reviewer2 = getOrCreateUser("funny_reviewer_2", "funny2@example.com");
        User reviewer3 = getOrCreateUser("funny_reviewer_3", "funny3@example.com");
        
        List<User> reviewers = List.of(reviewer1, reviewer2, reviewer3);
        
        // Список смешных отзывов
        String[] funnyReviews = {
            "Прочитал за один присест! Правда, пришлось три раза перезагружать компьютер, но оно того стоило!",
            "Купил книгу случайно, думал это про кулинарию. Теперь я Senior Developer! 10/10, рекомендую!",
            "Автор явно не тестировал код перед публикацией. У меня ничего не работает, но читать было весело!",
            "Книга отличная, но почему-то после прочтения мой код стал еще хуже. Возможно, я что-то не так понял?",
            "Прочитал на работе вместо выполнения задач. Босс недоволен, но я теперь знаю про dependency injection!",
            "Купил для подарка другу-программисту. Он плакал от смеха, а потом от отчаяния. Отличная книга!",
            "Книга помогла мне понять, что я вообще ничего не понимаю в программировании. Спасибо за просветление!",
            "Прочитал за выходные. Теперь у меня нет выходных, но зато есть понимание Spring Boot!",
            "Книга настолько хорошая, что я забыл поесть. И поспать. И выйти из дома. Помогите!",
            "Автор обещал, что после прочтения я стану гуру. Я стал гуру в чтении книг о программировании!",
            "Купил книгу, прочитал, ничего не понял, перечитал, снова ничего не понял. Купил еще одну книгу!",
            "Книга изменила мою жизнь! Теперь я не сплю по ночам, но не потому что читаю, а потому что дебажу код!",
            "Прочитал книгу и понял, что все эти годы я программировал неправильно. Теперь я программирую еще неправильнее!",
            "Книга отличная, но почему-то мой кот начал писать на Java после того, как я ее прочитал. Это нормально?",
            "Купил книгу для повышения квалификации. Теперь я квалифицированно не сплю по ночам!",
            "Прочитал книгу и решил переписать весь проект. Теперь у меня нет проекта, но есть опыт!",
            "Книга помогла мне понять, что я не один такой. Есть еще люди, которые не понимают, что они делают!",
            "Купил книгу случайно, открыл случайно, прочитал случайно. Теперь я случайный Senior Developer!",
            "Книга настолько информативная, что после прочтения мой мозг перезагрузился. Пришлось перечитать!",
            "Прочитал книгу и понял, что все мои проблемы были из-за того, что я не читал эту книгу раньше!",
            "Купил книгу по совету коллеги. Теперь я понимаю, почему он уволился!",
            "Прочитал книгу и начал видеть код во сне. Просыпаюсь и пишу на Java. Помогите!",
            "Книга отличная, но почему-то после прочтения мой компьютер начал сам себя обновлять. Это нормально?",
            "Купил книгу для изучения. Теперь я знаю, что не знаю ничего. Спасибо за честность!",
            "Прочитал книгу за один день. На следующий день забыл все. Перечитал. Забыл снова. Цикл бесконечен!",
            "Книга помогла мне понять, что мой код - это не баги, это фичи! Спасибо за вдохновение!",
            "Купил книгу случайно, прочитал случайно, понял случайно. Теперь я случайный архитектор!",
            "Прочитал книгу и решил стать программистом. Теперь я программист, который не умеет программировать!",
            "Книга настолько хорошая, что я купил еще 5 экземпляров. На всякий случай. И для друзей. И для кота.",
            "Прочитал книгу и понял, что все эти годы я использовал неправильный фреймворк. Теперь использую еще неправильнее!",
            "Купил книгу для повышения зарплаты. Зарплата не повысилась, но я теперь знаю про паттерны проектирования!",
            "Прочитал книгу и начал рефакторить весь код. Теперь у меня нет рабочего кода, но есть понимание!",
            "Книга отличная, но почему-то после прочтения мой IDE начал предлагать мне уволиться. Это нормально?",
            "Купил книгу по акции. Теперь понимаю, почему она была по акции!",
            "Прочитал книгу и понял, что мой код - это произведение искусства. Плохого искусства, но искусства!",
            "Книга помогла мне понять, что я не тупой, просто книга слишком умная для меня!",
            "Прочитал книгу и начал писать комментарии на русском. Теперь весь код на русском. Помогите!",
            "Купил книгу для изучения. Теперь я эксперт в чтении книг о программировании. Код все еще не работает!"
        };
        
        int reviewIndex = 0;
        int reviewsAdded = 0;
        
        // Добавляем отзывы к каждой второй книге (начиная с индекса 1: 1, 3, 5, 7...)
        for (int i = 1; i < savedBooks.size(); i += 2) {
            Book book = savedBooks.get(i);
            
            // Определяем количество отзывов (1 или 2)
            int numberOfReviews = random.nextInt(2) + 1; // 1 или 2
            
            for (int j = 0; j < numberOfReviews; j++) {
                // Выбираем случайного ревьюера
                User reviewer = reviewers.get(random.nextInt(reviewers.size()));
                
                // Проверяем, нет ли уже отзыва от этого пользователя
                if (reviewRepository.existsByBookIdAndUserId(book.getId(), reviewer.getId())) {
                    continue;
                }
                
                // Выбираем случайный отзыв
                String reviewText = funnyReviews[reviewIndex % funnyReviews.length];
                reviewIndex++;
                
                // Создаем отзыв
                Review review = Review.builder()
                        .book(book)
                        .user(reviewer)
                        .text(reviewText)
                        .build();
                reviewRepository.save(review);
                
                // Создаем оценку (от 3 до 5, чтобы было смешно)
                short ratingValue = (short) (random.nextInt(3) + 3); // 3, 4 или 5
                
                // Проверяем, нет ли уже оценки от этого пользователя
                if (!ratingRepository.existsByBookIdAndUserId(book.getId(), reviewer.getId())) {
                    Rating rating = Rating.builder()
                            .book(book)
                            .user(reviewer)
                            .value(ratingValue)
                            .build();
                    ratingRepository.save(rating);
                }
                
                reviewsAdded++;
            }
        }
        
        System.out.println("Funny reviews initialization completed: " + reviewsAdded + " reviews added.");
    }
    
    private User getOrCreateUser(String nickname, String email) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User user = User.builder()
                            .nickname(nickname)
                            .email(email)
                            .passwordHash(passwordEncoder.encode("password123"))
                            .role(User.Role.USER)
                            .build();
                    return userRepository.save(user);
                });
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

