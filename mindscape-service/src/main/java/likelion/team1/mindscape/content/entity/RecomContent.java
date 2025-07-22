package likelion.team1.mindscape.content.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
<<<<<<< HEAD
import lombok.NoArgsConstructor;
=======
import lombok.Getter;
>>>>>>> origin/feat/gpt

@Entity
@Table(name="recommended_content")
@AllArgsConstructor
<<<<<<< HEAD
@NoArgsConstructor
public class RecomContent {

    @Id
    @Column(name = "recom_id")
    private Long recomId;

    @Column(name="test_id", nullable = false)
    private Long testId;
=======
@Getter
public class RecomContent {
	
	@Id
    private Long recomId;  //testid = recomid

    @Column(nullable = false)
    private Long testId;   
    
 // 기본 생성자 반드시 필요
    protected RecomContent() {
    }

    public RecomContent(Long testId) {
        this.recomId = testId;
        this.testId = testId;
    }
>>>>>>> origin/feat/gpt

}
