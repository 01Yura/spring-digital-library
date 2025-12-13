package online.ityura.springdigitallibrary.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "book_files", uniqueConstraints = {
    @UniqueConstraint(columnNames = "book_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookFile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false, unique = true)
    private Book book;
    
    @Column(name = "storage_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private StorageType storageType;
    
    @Column(name = "file_path", nullable = false, columnDefinition = "TEXT")
    private String filePath;
    
    @Column(name = "original_filename")
    private String originalFilename;
    
    @Column(name = "content_type")
    @Builder.Default
    private String contentType = "application/pdf";
    
    @Column(name = "size_bytes")
    private Long sizeBytes;
    
    private String checksum;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public enum StorageType {
        LOCAL, S3, MINIO
    }
}

