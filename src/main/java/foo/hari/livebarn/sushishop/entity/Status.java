package foo.hari.livebarn.sushishop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "status")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Status{
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) int id;
        @Column(nullable = false, name="name", length = 30)
        String name;
}
