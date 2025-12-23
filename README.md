# Spring Digital Library

Веб-приложение для управления цифровой библиотекой на базе Spring Boot.

## Описание

Spring Digital Library - это RESTful API для управления цифровой библиотекой с возможностью:

- Регистрации и аутентификации пользователей
- Просмотра каталога книг
- Загрузки и скачивания книг в формате PDF
- Оставления рейтингов и отзывов на книги
- Административного управления книгами

## Технологии

- **Java 21**
- **Spring Boot 4.0.0**
- **Spring Security** (JWT аутентификация)
- **Spring Data JPA**
- **PostgreSQL**
- **Lombok**
- **Swagger/OpenAPI** (документация API)
- **JUnit 5** (юнит-тесты)

## Требования

- Java 21 или выше
- Maven 3.6+
- PostgreSQL 12+

## Установка и запуск

### 1. Клонирование репозитория

```bash
git clone <repository-url>
cd spring-digital-library
```

### 2. Настройка базы данных

Создайте базу данных PostgreSQL:

```sql
CREATE DATABASE spring_digital_bookstore;
```

### 3. Настройка конфигурации

Отредактируйте `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/spring_digital_bookstore
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 4. Настройка путей для файлов

Создайте директории для хранения файлов:

```bash
# Windows
mkdir G:\opt\spring-digital-bookstore\pictures
mkdir G:\opt\spring-digital-bookstore\pdf

# Linux/Mac
mkdir -p /opt/spring-digital-bookstore/pictures
mkdir -p /opt/spring-digital-bookstore/pdf
```

Или установите переменные окружения:

- `APP_IMAGES_STORAGE_PATH` - путь для изображений книг
- `APP_PDF_STORAGE_PATH` - путь для PDF файлов

### 5. Запуск приложения

```bash
mvn spring-boot:run
```

Приложение будет доступно по адресу: `http://localhost:8080`

## API Документация

После запуска приложения документация Swagger доступна по адресу:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Основные эндпоинты

### Аутентификация

- `POST /api/v1/auth/register` - Регистрация пользователя
- `POST /api/v1/auth/login` - Вход в систему
- `POST /api/v1/auth/refresh` - Обновление токена

### Книги (публичные)

- `GET /api/v1/books` - Получить список книг (с пагинацией)
- `GET /api/v1/books/{id}` - Получить книгу по ID
- `GET /api/v1/books/{id}/image` - Получить изображение книги
- `GET /api/v1/books/{id}/reviews` - Получить отзывы на книгу

### Книги (требуется аутентификация)

- `GET /api/v1/books/{id}/download` - Скачать PDF книги

### Рейтинги (требуется аутентификация)

- `POST /api/v1/books/{id}/ratings` - Создать рейтинг
- `PATCH /api/v1/books/{id}/ratings` - Обновить свой рейтинг

### Отзывы (требуется аутентификация)

- `POST /api/v1/books/{id}/reviews` - Создать отзыв
- `PATCH /api/v1/books/{id}/reviews` - Обновить свой отзыв
- `GET /api/v1/user/reviews` - Получить свои отзывы

### Административные функции (требуется роль ADMIN)

- `POST /api/v1/admin/books` - Создать книгу
- `PUT /api/v1/admin/books/{id}` - Обновить книгу
- `PATCH /api/v1/admin/books/{id}` - Частично обновить книгу
- `DELETE /api/v1/admin/books/{id}` - Удалить книгу
- `POST /api/v1/admin/books/{id}/image` - Загрузить изображение книги

## Тестирование

Запуск всех тестов:

```bash
mvn test
```

Юнит-тесты находятся в директории `src/test/java/online/ityura/springdigitallibrary/unit/`

## Структура проекта

```
src/
├── main/
│   ├── java/
│   │   └── online/ityura/springdigitallibrary/
│   │       ├── config/          # Конфигурационные классы
│   │       ├── controller/      # REST контроллеры
│   │       ├── dto/             # Data Transfer Objects
│   │       ├── exception/       # Обработка исключений
│   │       ├── model/           # JPA сущности
│   │       ├── repository/      # Репозитории Spring Data JPA
│   │       ├── security/        # Конфигурация безопасности
│   │       └── service/         # Бизнес-логика
│   └── resources/
│       └── application.properties
└── test/
    └── java/
        └── online/ityura/springdigitallibrary/
            └── unit/            # Юнит-тесты
```

## Безопасность

- Аутентификация через JWT токены
- Пароли хешируются с использованием BCrypt
- Роли пользователей: USER, ADMIN
- Защита эндпоинтов через Spring Security

## Лицензия

Этот проект создан в образовательных целях.

