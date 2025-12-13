# API Документация - Spring Digital Bookstore

## Базовый URL
```
/api/v1
```

## Аутентификация

Все запросы (кроме `/auth/**`) требуют JWT токен в заголовке:
```
Authorization: Bearer <token>
```

---

## 1. Аутентификация

### POST /api/v1/auth/register
Регистрация нового пользователя (роль по умолчанию: USER)

**Request Body:**
```json
{
  "nickname": "John Doe",
  "email": "john@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": 1,
  "email": "john@example.com",
  "role": "USER"
}
```

### POST /api/v1/auth/login
Вход в систему

**Request Body:**
```json
{
  "email": "john@example.com",
  "password": "password123"
}
```

**Response:** (аналогично register)

---

## 2. Книги (публичные эндпоинты)

### GET /api/v1/books
Получить список книг (с пагинацией)

**Query Parameters:**
- `page` - номер страницы (default: 0)
- `size` - размер страницы (default: 20)
- `sort` - сортировка (например: `title,asc`)

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "title": "Spring Boot Guide",
      "author": {
        "id": 1,
        "fullName": "John Smith"
      },
      "description": "...",
      "publishedYear": 2023,
      "isbn": "1234567890",
      "ratingAvg": 8.5,
      "ratingCount": 10,
      "hasFile": true,
      "createdAt": "2024-01-01T00:00:00",
      "updatedAt": "2024-01-01T00:00:00"
    }
  ],
  "totalElements": 100,
  "totalPages": 5
}
```

### GET /api/v1/books/{bookId}
Получить детальную информацию о книге

**Response:** (объект BookResponse)

### GET /api/v1/books/{bookId}/download
Скачать PDF файл книги

**Response:** PDF файл

---

## 3. Административные операции (требуется роль ADMIN)

### POST /api/v1/admin/books
Создать новую книгу

**Request Body:**
```json
{
  "title": "New Book",
  "authorName": "Author Name",
  "description": "Book description",
  "publishedYear": 2024,
  "isbn": "1234567890"
}
```

**Response:** (объект BookResponse)

### PUT /api/v1/admin/books/{bookId}
Обновить книгу

**Request Body:** (все поля опциональны)
```json
{
  "title": "Updated Title",
  "authorName": "New Author",
  "description": "Updated description"
}
```

**Response:** (объект BookResponse)

### DELETE /api/v1/admin/books/{bookId}
Удалить книгу

**Ошибки:**
- `403 Forbidden` - если `deletion_locked = true`
- `409 Conflict` - если есть связанные отзывы

**Response:** `204 No Content`

---

## 4. Отзывы

### POST /api/v1/books/{bookId}/reviews
Создать отзыв на книгу

**Request Body:**
```json
{
  "text": "Great book!"
}
```

**Ошибки:**
- `409 Conflict` - если отзыв уже существует

**Response:**
```json
{
  "id": 1,
  "bookId": 1,
  "user": {
    "id": 1,
    "nickname": "John Doe",
    "email": "john@example.com"
  },
  "text": "Great book!",
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-01-01T00:00:00"
}
```

### PUT /api/v1/books/{bookId}/reviews/my
Обновить свой отзыв

**Request Body:**
```json
{
  "text": "Updated review text"
}
```

**Response:** (объект ReviewResponse)

### GET /api/v1/books/{bookId}/reviews
Получить список отзывов на книгу (с пагинацией)

**Query Parameters:**
- `page`, `size`, `sort`

**Response:** (Page<ReviewResponse>)

### GET /api/v1/books/{bookId}/reviews/my
Получить свой отзыв на книгу

**Response:** (объект ReviewResponse)

---

## 5. Рейтинги

### POST /api/v1/books/{bookId}/ratings
Поставить рейтинг книге (1-10)

**Request Body:**
```json
{
  "value": 8
}
```

**Ошибки:**
- `409 Conflict` - если рейтинг уже существует
- `400 Bad Request` - если value не в диапазоне 1-10

**Response:**
```json
{
  "id": 1,
  "bookId": 1,
  "userId": 1,
  "value": 8,
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-01-01T00:00:00"
}
```

### PUT /api/v1/books/{bookId}/ratings/my
Изменить свой рейтинг

**Request Body:**
```json
{
  "value": 9
}
```

**Response:** (объект RatingResponse)

---

## Коды ошибок

- `400 Bad Request` - неверный формат запроса
- `401 Unauthorized` - отсутствует или неверный JWT токен
- `403 Forbidden` - недостаточно прав доступа
- `404 Not Found` - ресурс не найден
- `409 Conflict` - конфликт (например, отзыв уже существует)
- `500 Internal Server Error` - внутренняя ошибка сервера

---

## Важные замечания

1. **deletion_locked**: Флаг `deletion_locked` в таблице `books` можно изменить только напрямую в БД. Нет API для его изменения.

2. **Уникальность книг**: Книга уникальна по паре `(title, author)`. Попытка создать дубликат вернет `409 Conflict`.

3. **Рейтинг**: Средний рейтинг книги автоматически пересчитывается при добавлении/изменении/удалении рейтинга (через триггер БД и сервис).

4. **Ограничения**:
   - Один пользователь может оставить только один отзыв на книгу
   - Один пользователь может поставить только один рейтинг на книгу (можно изменить)

