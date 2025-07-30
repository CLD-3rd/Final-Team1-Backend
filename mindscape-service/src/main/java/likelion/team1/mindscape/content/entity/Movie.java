package likelion.team1.mindscape.content.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
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
    
    public Movie(String title, Date releaseDate, String description, String poster, RecomContent recommendedContent) {
        this.title = title;
        this.releaseDate = releaseDate;
        this.description = description;
        this.poster = poster;
        this.recommendedContent = recommendedContent;
    }

}
