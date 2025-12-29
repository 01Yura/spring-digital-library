#!/bin/bash

# Скрипт для создания пользователя PostgreSQL с правами read-only
# Версия для запуска внутри Docker контейнера PostgreSQL
# 
# Использование (способ 1 - копирование в контейнер):
#   docker cp create_readonly_user_docker.sh spring-digital-library-db:/tmp/create_readonly_user.sh
#   docker exec -it spring-digital-library-db bash /tmp/create_readonly_user.sh [username] [password]
#
# Использование (способ 2 - через stdin):
#   docker exec -i spring-digital-library-db bash -s -- [username] [password] < create_readonly_user_docker.sh

set -e  # Остановка при ошибке

# Параметры подключения к БД
# Пароль администратора по умолчанию берется из docker-compose.yml (POSTGRES_PASSWORD)
# Можно переопределить через переменную окружения: DB_ADMIN_PASSWORD=your_password
# Если запускаете с хоста (не внутри контейнера), укажите DB_HOST=localhost DB_PORT=5432
DB_NAME="${DB_NAME:-spring_digital_bookstore}"
DB_ADMIN_USER="${DB_ADMIN_USER}"
DB_ADMIN_PASSWORD="${DB_ADMIN_PASSWORD}"
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"

# Параметры нового пользователя (приоритет: переменные окружения > аргументы командной строки > значения по умолчанию)
READONLY_USER="${READONLY_USER:-${1:-readonly_user}}"
if [ -z "$READONLY_PASSWORD" ] && [ -n "$2" ]; then
    READONLY_PASSWORD="$2"
elif [ -z "$READONLY_PASSWORD" ]; then
    READONLY_PASSWORD="pass_123_XYZ!"
fi

# Цвета для вывода
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Создание пользователя PostgreSQL с правами read-only${NC}"
echo "=========================================="
echo "База данных: $DB_NAME"
echo "Новый пользователь: $READONLY_USER"
echo "=========================================="
echo ""

# Проверка наличия psql
if ! command -v psql &> /dev/null; then
    echo -e "${RED}Ошибка: psql не найден.${NC}"
    exit 1
fi

# Устанавливаем пароль для psql через переменную окружения
export PGPASSWORD="$DB_ADMIN_PASSWORD"

# Проверка подключения к БД
echo -e "${YELLOW}Проверка подключения к базе данных...${NC}"
echo -e "${YELLOW}Хост: $DB_HOST, Порт: $DB_PORT, Пользователь: $DB_ADMIN_USER, БД: $DB_NAME${NC}"
if ! psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_ADMIN_USER" -d "$DB_NAME" -c "SELECT 1;" > /dev/null 2>&1; then
    echo -e "${RED}Ошибка: Не удалось подключиться к базе данных${NC}"
    echo -e "${RED}Проверьте параметры: DB_HOST=$DB_HOST, DB_PORT=$DB_PORT, DB_ADMIN_USER=$DB_ADMIN_USER, DB_NAME=$DB_NAME${NC}"
    echo -e "${YELLOW}Подсказка: Установите переменную окружения DB_ADMIN_PASSWORD если пароль отличается${NC}"
    echo -e "${YELLOW}Или запустите скрипт внутри контейнера: docker exec -i spring-digital-library-db bash -s < create_readonly_user_docker.sh${NC}"
    exit 1
fi

echo -e "${GREEN}Подключение успешно!${NC}"
echo ""

# Проверка существования пользователя
echo -e "${YELLOW}Проверка существования пользователя '$READONLY_USER'...${NC}"
USER_EXISTS=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_ADMIN_USER" -d postgres -tAc "SELECT 1 FROM pg_roles WHERE rolname='$READONLY_USER'")

if [ "$USER_EXISTS" = "1" ]; then
    echo -e "${YELLOW}Пользователь '$READONLY_USER' уже существует.${NC}"
    echo "Удаление существующего пользователя..."
    psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_ADMIN_USER" -d postgres <<EOF
REVOKE ALL PRIVILEGES ON DATABASE $DB_NAME FROM $READONLY_USER;
DROP ROLE IF EXISTS $READONLY_USER;
EOF
    echo -e "${GREEN}Пользователь удален.${NC}"
fi

# Создание пользователя
echo -e "${YELLOW}Создание пользователя '$READONLY_USER'...${NC}"
psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_ADMIN_USER" -d postgres <<EOF
CREATE ROLE $READONLY_USER WITH LOGIN PASSWORD '$READONLY_PASSWORD';
EOF

if [ $? -eq 0 ]; then
    echo -e "${GREEN}Пользователь создан успешно!${NC}"
else
    echo -e "${RED}Ошибка при создании пользователя${NC}"
    exit 1
fi

# Предоставление прав на подключение к базе данных
echo -e "${YELLOW}Предоставление прав на подключение к базе данных...${NC}"
psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_ADMIN_USER" -d postgres <<EOF
GRANT CONNECT ON DATABASE $DB_NAME TO $READONLY_USER;
EOF

# Предоставление прав на использование схемы public
echo -e "${YELLOW}Предоставление прав на использование схемы public...${NC}"
psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_ADMIN_USER" -d "$DB_NAME" <<EOF
GRANT USAGE ON SCHEMA public TO $READONLY_USER;
EOF

# Предоставление прав SELECT на все существующие таблицы
echo -e "${YELLOW}Предоставление прав SELECT на все существующие таблицы...${NC}"
psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_ADMIN_USER" -d "$DB_NAME" <<EOF
GRANT SELECT ON ALL TABLES IN SCHEMA public TO $READONLY_USER;
EOF

# Предоставление прав SELECT на все будущие таблицы (по умолчанию)
echo -e "${YELLOW}Настройка прав по умолчанию для будущих таблиц...${NC}"
psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_ADMIN_USER" -d "$DB_NAME" <<EOF
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO $READONLY_USER;
EOF

# Предоставление прав на использование последовательностей (для чтения)
echo -e "${YELLOW}Предоставление прав на последовательности...${NC}"
psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_ADMIN_USER" -d "$DB_NAME" <<EOF
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO $READONLY_USER;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE, SELECT ON SEQUENCES TO $READONLY_USER;
EOF

echo ""
echo -e "${GREEN}=========================================="
echo "Пользователь успешно создан!"
echo "==========================================${NC}"
echo ""
echo "Параметры подключения:"
echo "  Database: $DB_NAME"
echo "  Username: $READONLY_USER"
echo "  Password: $READONLY_PASSWORD"
echo ""
echo -e "${YELLOW}ВНИМАНИЕ: Сохраните пароль в безопасном месте!${NC}"

