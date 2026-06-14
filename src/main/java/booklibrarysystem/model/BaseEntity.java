package booklibrarysystem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

//MappedSuperclass is to share common fields across multiple entities.
@MappedSuperclass
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(updatable =false)
    private LocalDateTime createdTime;

    @Version
    private Long version;

    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

}
