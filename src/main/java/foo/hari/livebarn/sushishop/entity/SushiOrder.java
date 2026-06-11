package foo.hari.livebarn.sushishop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "sushi_order")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SushiOrder {
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) int id;
        @Column(nullable = false) int status_id;
        @Column(nullable = false) int sushi_id;
        @CreatedDate @Column(name = "created_at", nullable = false, updatable = false) LocalDateTime createdAt;
}
