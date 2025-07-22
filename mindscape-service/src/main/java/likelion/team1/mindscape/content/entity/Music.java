package likelion.team1.mindscape.content.entity;

import java.util.Date;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Setter;

@Entity
@Table(name = "music")
@Data
public class Music {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer musicId;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(length = 100)
    private String artist;

    @Column(length = 255)
    private String elbum;

    @ManyToOne
    @JoinColumn(name = "recomId", nullable = false)
    private RecomContent recommendedContent;
    
    public Music(String title, String artist, String elbum, RecomContent recommendedContent) {
        this.title = title;
        this.artist = artist;
        this.elbum = elbum;
        this.recommendedContent = recommendedContent;
    }
}
