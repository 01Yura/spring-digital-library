# Настройка пользователя PostgreSQL с правами read-only

## Описание

Скрипты для создания пользователя PostgreSQL с правами только на чтение (read-only) для базы данных `spring_digital_bookstore`.

## Варианты использования

### Вариант 1: Запуск на сервере (вне Docker)

Если PostgreSQL запущен на сервере напрямую (не в Docker):

```bash
# Сделать скрипт исполняемым
chmod +x create_readonly_user.sh

# Запуск с параметрами по умолчанию
./create_readonly_user.sh

# Или указать имя пользователя и пароль
./create_readonly_user.sh readonly_user my_secure_password

# Или через переменные окружения
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=spring_digital_bookstore
export DB_ADMIN_USER=nobugs228
export DB_ADMIN_PASSWORD=nobugs228PASSWORD!#
./create_readonly_user.sh readonly_user my_secure_password
```

### Вариант 2: Запуск внутри Docker контейнера

Если PostgreSQL запущен в Docker контейнере:

**Способ 1: Копирование скрипта в контейнер (рекомендуется)**

```bash
# Скопировать скрипт в контейнер
docker cp create_readonly_user_docker.sh spring-digital-library-db:/tmp/create_readonly_user.sh

# Запуск с указанием пользователя и пароля
docker exec -it spring-digital-library-db bash /tmp/create_readonly_user.sh readonly_user my_secure_password

# Или с автоматической генерацией пароля (только имя пользователя)
docker exec -it spring-digital-library-db bash /tmp/create_readonly_user.sh readonly_user
```

**Способ 2: Через stdin с правильной передачей аргументов**

```bash
# Запуск с передачей аргументов через -- (правильный синтаксис)
docker exec -i spring-digital-library-db bash -s -- readonly_user my_secure_password < create_readonly_user_docker.sh

# Или с автоматической генерацией пароля
docker exec -i spring-digital-library-db bash -s -- readonly_user < create_readonly_user_docker.sh
```

**Способ 3: Через переменные окружения (самый надежный)**

```bash
# Скопировать скрипт в контейнер
docker cp create_readonly_user_docker.sh spring-digital-library-db:/tmp/create_readonly_user.sh

# Запуск с переменными окружения
docker exec -it spring-digital-library-db bash -c \
  'READONLY_USER=readonly_user READONLY_PASSWORD=my_secure_password bash /tmp/create_readonly_user.sh'
```

### Вариант 3: Прямое выполнение SQL команд

Если нужно выполнить вручную:

```bash
# Подключиться к контейнеру
docker exec -it spring-digital-library-db psql -U nobugs228 -d spring_digital_bookstore

# Затем выполнить SQL команды:
```

```sql
-- Создание пользователя
CREATE ROLE readonly_user WITH LOGIN PASSWORD 'your_secure_password';

-- Права на подключение к базе данных
GRANT CONNECT ON DATABASE spring_digital_bookstore TO readonly_user;

-- Права на использование схемы
GRANT USAGE ON SCHEMA public TO readonly_user;

-- Права SELECT на все существующие таблицы
GRANT SELECT ON ALL TABLES IN SCHEMA public TO readonly_user;

-- Права SELECT на все будущие таблицы
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO readonly_user;

-- Права на последовательности
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO readonly_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE, SELECT ON SEQUENCES TO readonly_user;
```

## Проверка прав

После создания пользователя можно проверить его права:

```bash
# Подключиться как readonly пользователь
docker exec -it spring-digital-library-db psql -U readonly_user -d spring_digital_bookstore

# Попробовать выполнить SELECT (должно работать)
SELECT * FROM books LIMIT 1;

# Попробовать выполнить INSERT (должно выдать ошибку)
INSERT INTO books (title) VALUES ('Test');  -- ERROR: permission denied
```

## Удаление пользователя

Если нужно удалить пользователя:

```sql
-- Подключиться как администратор
docker exec -it spring-digital-library-db psql -U nobugs228 -d postgres

-- Удалить пользователя
REVOKE ALL PRIVILEGES ON DATABASE spring_digital_bookstore FROM readonly_user;
DROP ROLE IF EXISTS readonly_user;
```

## Безопасность

⚠️ **Важно:**

- Используйте надежные пароли
- Не храните пароли в открытом виде
- Ограничьте доступ к скриптам (chmod 700)
- Регулярно проверяйте список пользователей БД

## Параметры по умолчанию

- **DB_HOST**: localhost
- **DB_PORT**: 5432
- **DB_NAME**: spring_digital_bookstore
- **DB_ADMIN_USER**: nobugs228
- **READONLY_USER**: readonly_user (или первый аргумент скрипта)
- **READONLY_PASSWORD**: автоматически генерируется (или второй аргумент скрипта)
