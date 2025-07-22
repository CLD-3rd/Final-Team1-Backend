package likelion.team1.mindscape.content.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Entity
@Table(name="recommended_content")
@AllArgsConstructor
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

}
