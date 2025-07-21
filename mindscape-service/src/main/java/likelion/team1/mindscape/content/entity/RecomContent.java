package likelion.team1.mindscape.content.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;

@Entity
@Table(name="recommended_content")
@AllArgsConstructor
public class RecomContent {

    @Id
    private Long recomId;

    @Column(nullable = false)
    private Integer testId;

}
