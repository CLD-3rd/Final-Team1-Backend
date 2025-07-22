package likelion.team1.mindscape.content.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "book")
@Data
@NoArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookId;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(length = 100)
    private String author;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String image;

    @ManyToOne
    @JoinColumn(name = "recomId", nullable = false)
    private RecomContent recommendedContent;
    
    
    
    
    public Book(String title, String author, String description, String image, RecomContent recommendedContent) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.image = image;
        this.recommendedContent = recommendedContent;
    }
}


