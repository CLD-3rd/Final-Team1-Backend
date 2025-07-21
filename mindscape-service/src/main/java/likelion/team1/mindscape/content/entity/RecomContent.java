package likelion.team1.mindscape.content.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name="recommended_content")
@AllArgsConstructor
@NoArgsConstructor
public class RecomContent {

    @Id
    @Column(name = "recom_id")
    private Long recomId;

    @Column(name="test_id", nullable = false)
    private Long testId;

}
