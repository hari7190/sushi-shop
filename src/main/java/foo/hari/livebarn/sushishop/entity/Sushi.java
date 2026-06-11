package foo.hari.livebarn.sushishop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sushi")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Sushi{
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) int id;
        @Column(name = "name", length = 30)
        String name;
        @Column(name="time_to_make", nullable = true)
        Integer timeToMake;
}
