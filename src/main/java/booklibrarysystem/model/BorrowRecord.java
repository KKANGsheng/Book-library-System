package booklibrarysystem.model;

import com.fasterxml.jackson.databind.ser.Serializers;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "active_book_id"))
public class BorrowRecord extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name="book_id")
    private Book book;
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "borrower_id")
    private Borrower borrower;
    private LocalDateTime borrowedAt;
    private LocalDateTime returnedAt;
    @Column(name = "active_book_id")
    private Long activeBookId;
}
