package online.ityura.springdigitallibrary.service;

import online.ityura.springdigitallibrary.model.Book;
import online.ityura.springdigitallibrary.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class BookImageService {
    
    @Autowired
    private BookRepository bookRepository;
    
    @Value("${app.images.storage-path}")
    private String storagePath;
    
    @Transactional
    public String uploadBookImage(Long bookId, MultipartFile file) {
        // Проверяем существование книги
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Book not found with id: " + bookId));
        
        // Проверяем, что файл не пустой
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Image file is required");
        }
        
        try {
            // Создаем директорию если её нет
            Path storageDir = Paths.get(storagePath);
            if (!Files.exists(storageDir)) {
                Files.createDirectories(storageDir);
            }
            
            // Генерируем имя файла на основе названия книги
            // Удаляем пробелы в начале и конце, затем заменяем пробелы на _
            String bookTitle = book.getTitle() != null ? book.getTitle().trim() : "";
            if (bookTitle.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Book title is empty, cannot generate image filename");
            }
            String sanitizedTitle = bookTitle.replaceAll("\\s+", "_");
            
            // Получаем расширение файла из оригинального имени
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            // Формируем имя файла: название_книги.расширение
            String filename = sanitizedTitle + extension;
            
            // Если файл с таким именем уже существует, добавляем UUID
            Path filePath = storageDir.resolve(filename);
            if (Files.exists(filePath)) {
                String baseName = sanitizedTitle;
                filename = baseName + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
                filePath = storageDir.resolve(filename);
            }
            
            // Сохраняем файл
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Формируем полный путь для сохранения в БД
            String imagePath = filePath.toString();
            
            // Удаляем старое изображение если оно существует
            if (book.getImagePath() != null && !book.getImagePath().isEmpty()) {
                try {
                    Path oldImagePath = Paths.get(book.getImagePath());
                    if (Files.exists(oldImagePath)) {
                        Files.delete(oldImagePath);
                    }
                } catch (IOException e) {
                    // Логируем ошибку, но не прерываем выполнение
                    System.err.println("Failed to delete old image: " + e.getMessage());
                }
            }
            
            // Обновляем путь к изображению в базе данных
            book.setImagePath(imagePath);
            bookRepository.save(book);
            
            return imagePath;
            
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Failed to save image: " + e.getMessage());
        }
    }
    
    public Resource getBookImage(Long bookId) {
        // Проверяем существование книги
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Book not found with id: " + bookId));
        
        // Проверяем наличие изображения
        if (book.getImagePath() == null || book.getImagePath().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Image not found for book id: " + bookId);
        }
        
        try {
            Path imagePath = Paths.get(book.getImagePath());
            Resource resource = new UrlResource(imagePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Image file not found or not readable");
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error reading image: " + e.getMessage());
        }
    }
    
    public Resource getAllBookImagesAsZip() {
        try {
            // Получаем все книги с изображениями
            List<Book> booksWithImages = bookRepository.findAllWithImages();
            
            if (booksWithImages.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "No books with images found");
            }
            
            // Создаем временный ZIP файл
            Path zipPath = Files.createTempFile("book-images-", ".zip");
            
            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
                for (Book book : booksWithImages) {
                    if (book.getImagePath() != null && !book.getImagePath().isEmpty()) {
                        try {
                            Path imagePath = Paths.get(book.getImagePath());
                            if (Files.exists(imagePath) && Files.isReadable(imagePath)) {
                                // Получаем имя файла из пути
                                String fileName = imagePath.getFileName().toString();
                                // Используем ID книги и название для уникальности имени в ZIP
                                String zipEntryName = book.getId() + "_" + fileName;
                                
                                ZipEntry zipEntry = new ZipEntry(zipEntryName);
                                zos.putNextEntry(zipEntry);
                                
                                // Копируем содержимое файла в ZIP
                                try (InputStream is = Files.newInputStream(imagePath)) {
                                    byte[] buffer = new byte[8192];
                                    int length;
                                    while ((length = is.read(buffer)) > 0) {
                                        zos.write(buffer, 0, length);
                                    }
                                }
                                
                                zos.closeEntry();
                            }
                        } catch (IOException e) {
                            // Пропускаем файлы, которые не удалось прочитать
                            System.err.println("Failed to add image for book " + book.getId() + ": " + e.getMessage());
                        }
                    }
                }
            }
            
            // Читаем содержимое ZIP файла в байтовый массив
            byte[] zipBytes = Files.readAllBytes(zipPath);
            
            // Удаляем временный файл
            Files.deleteIfExists(zipPath);
            
            // Создаем Resource из байтового массива
            return new ByteArrayResource(zipBytes);
            
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error creating ZIP archive: " + e.getMessage());
        }
    }
}
