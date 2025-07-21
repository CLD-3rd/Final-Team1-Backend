package likelion.team1.mindscape.content.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
@Table(name="movie")
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long movieId;

    @Column(length = 100, nullable = false)
    private String title;

    private Date releaseDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String poster;

    @ManyToOne
    @JoinColumn(name = "recomId", nullable = false)
    private RecomContent recommendedContent;
}
