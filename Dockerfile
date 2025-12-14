# Многоэтапная сборка для оптимизации размера образа
# Этап 1: Сборка приложения
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Копируем pom.xml и загружаем зависимости (кэшируется если pom.xml не изменился)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем исходный код и собираем приложение
COPY src ./src
RUN mvn clean package -DskipTests -B

# Этап 2: Финальный образ с минимальным размером
FROM eclipse-temurin:21-jre-alpine

# Создаем пользователя для запуска приложения (безопасность)
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# Копируем JAR файл из этапа сборки
COPY --from=build /app/target/spring-digital-library-*.jar app.jar

# Меняем владельца файла
RUN chown spring:spring app.jar

# Переключаемся на пользователя spring
USER spring

# Открываем порт (по умолчанию Spring Boot использует 8080)
EXPOSE 8080

# Переменные окружения для JVM оптимизации
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Запускаем приложение
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

