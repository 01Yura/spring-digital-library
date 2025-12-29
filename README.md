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

**Рекомендуется использовать `.env` файл** для хранения секретных данных (БД пароли, API ключи).

Создайте файл `.env` в корне проекта и заполните реальными значениями:

```env
# Настройки базы данных
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/spring_digital_bookstore
SPRING_DATASOURCE_USERNAME=your_username
SPRING_DATASOURCE_PASSWORD=your_password

# JWT настройки
JWT_SECRET=your-secret-key-here-min-256-bits

# OpenAI API (опционально, для эндпоинта отправки сообщений читателям)
OPENAI_API_KEY=your-openai-api-key

# Пути для хранения файлов (опционально, есть значения по умолчанию)
APP_IMAGES_STORAGE_PATH=G:\opt\spring-digital-bookstore\pictures
APP_PDF_STORAGE_PATH=G:\opt\spring-digital-bookstore\pdf
```

**Важно:** Файл `.env` уже добавлен в `.gitignore` и не будет закоммичен в репозиторий.

Альтернативно, вы можете отредактировать `src/main/resources/application.properties` напрямую, но это не рекомендуется для продакшена.

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

### 5. Настройка OpenAI API (опционально)

Для работы эндпоинта отправки сообщений читателям необходимо настроить OpenAI API ключ.

**Рекомендуемый способ:** Добавьте ключ в `.env` файл (см. шаг 3):

```env
OPENAI_API_KEY=your-api-key-here
```

**Альтернативный способ:** Установите переменную окружения:

```bash
# Windows (PowerShell)
$env:OPENAI_API_KEY="your-api-key-here"

# Windows (CMD)
set OPENAI_API_KEY=your-api-key-here

# Linux/Mac
export OPENAI_API_KEY="your-api-key-here"
```

**Важно:** Не коммитьте реальный API ключ в репозиторий! Файл `.env` уже в `.gitignore`.

### 6. Запуск приложения

#### Локальный запуск (без Docker)

```bash
mvn spring-boot:run
```

Приложение будет доступно по адресу: `http://localhost:8080`

#### Запуск через Docker Compose

Для запуска через Docker Compose используйте файл `infra/docker-compose.yml`:

```bash
cd infra
docker-compose up -d
```

Приложение будет доступно по адресу: `http://localhost:8088` (порт 8088 проброшен на 8080 внутри контейнера)

Для остановки:

```bash
docker-compose down
```

## API Документация

После запуска приложения документация Swagger доступна по адресу:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Системные эндпоинты (доступны без авторизации)

- `GET /api/v1/health` - Проверка состояния приложения (uptime, timestamp)
- `GET /api/v1/kuberinfo` - Информация о Kubernetes окружении (pod, node, OS)

## Основные эндпоинты

### Аутентификация

- `POST /api/v1/auth/register` - Регистрация пользователя
- `POST /api/v1/auth/login` - Вход в систему
- `POST /api/v1/auth/refresh` - Обновление токена

### Книги (публичные)

- `GET /api/v1/books` - Получить список книг (с пагинацией и сортировкой)
- `GET /api/v1/books/{id}` - Получить книгу по ID
- `GET /api/v1/books/{id}/image` - Получить изображение книги
- `GET /api/v1/books/images/all` - Получить все изображения книг в ZIP архиве
- `GET /api/v1/books/{id}/reviews` - Получить отзывы на книгу (с пагинацией)
- `POST /api/v1/books/{id}/message` - Отправить сообщение читателю о книге (требует OpenAI API ключ)

### Книги (требуется аутентификация)

- `GET /api/v1/books/{id}/download` - Скачать PDF книги

### Рейтинги (требуется аутентификация)

- `POST /api/v1/books/{id}/ratings` - Создать рейтинг (от 1 до 10)
- `PUT /api/v1/books/{id}/ratings/my` - Обновить свой рейтинг

### Отзывы (требуется аутентификация)

- `POST /api/v1/books/{id}/reviews` - Создать отзыв
- `PUT /api/v1/books/{id}/reviews/my` - Обновить свой отзыв
- `GET /api/v1/reviews/my` - Получить свои отзывы (с пагинацией)

### Административные функции (требуется роль ADMIN)

#### Управление книгами

- `POST /api/v1/admin/books` - Создать книгу
- `POST /api/v1/admin/books/batch` - Создать несколько книг одновременно
- `PUT /api/v1/admin/books/{id}` - Полностью обновить книгу
- `PATCH /api/v1/admin/books/{id}` - Частично обновить книгу (JSON или multipart/form-data с изображением)
- `DELETE /api/v1/admin/books/{id}` - Удалить книгу
- `DELETE /api/v1/admin/books/authors/{id}` - Удалить автора и все его книги
- `POST /api/v1/admin/books/{id}/image` - Загрузить изображение книги (multipart/form-data)

#### Управление пользователями

- `GET /api/v1/admin/users` - Получить список всех пользователей
- `DELETE /api/v1/admin/users/{id}` - Удалить пользователя (нельзя удалить ADMIN)

## Тестирование

Запуск всех тестов:

```bash
mvn test
```

### Типы тестов

- **Юнит-тесты** находятся в директории `src/test/java/online/ityura/springdigitallibrary/unit/`
- **API тесты** находятся в директории `src/test/java/online/ityura/springdigitallibrary/api/`

### Инфраструктура для тестирования

Проект включает инфраструктуру для тестирования (`testinfra`):

- **DataBaseSteps** - удобные методы для работы с БД в тестах
- **DBRequest** - универсальный класс для выполнения SQL запросов
- **UniversalComparator** - сравнение DTO объектов по правилам из `dto-comparison.properties`
- **HttpClient** - базовый класс для HTTP клиентов
- **CrudRequester** - клиент для CRUD операций через REST API
- **RandomDataGenerator** - генерация тестовых данных

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
│   │       ├── service/         # Бизнес-логика
│   │       └── testinfra/       # Инфраструктура для тестирования
│   │           ├── comparators/ # Сравнение объектов
│   │           ├── configs/     # Конфигурация для тестов
│   │           ├── database/    # Работа с БД в тестах
│   │           ├── generators/  # Генерация тестовых данных
│   │           ├── helpers/    # Вспомогательные классы
│   │           ├── requests/   # HTTP клиенты для тестов
│   │           └── specs/      # Спецификации для REST Assured
│   └── resources/
│       ├── application.properties
│       ├── dto-comparison.properties  # Правила сравнения DTO
│       ├── pictures/           # Исходные изображения книг
│       └── pdf/                # Исходные PDF файлы книг
└── test/
    └── java/
        └── online/ityura/springdigitallibrary/
            ├── api/            # API тесты
            └── unit/           # Юнит-тесты
```

## Дополнительные скрипты

- `scripts/build-and-push.ps1` - PowerShell скрипт для сборки и публикации Docker образа
- `scripts/clear-maven-wrapper-cache.ps1` - Очистка кэша Maven wrapper
- `scripts/create_readonly_user_docker.sh` - Создание пользователя PostgreSQL с правами read-only

## Безопасность

- Аутентификация через JWT токены (access token - 5 минут, refresh token - 24 часа)
- Пароли хешируются с использованием BCrypt
- Роли пользователей: USER, ADMIN
- Защита эндпоинтов через Spring Security
- Публичные эндпоинты доступны без авторизации (список книг, детали книги, изображения, отзывы на книгу)
- Защищенные эндпоинты требуют JWT токен в заголовке `Authorization: Bearer <token>`

### Учетные данные по умолчанию

При первом запуске создаются два администратора:

- Email: `admin@gmail.com`, Пароль: `admin`
- Email: `crackMyPassword@gmail.com`, Пароль: `137Password123!@#`

## Лицензия

Этот проект создан в образовательных целях.
