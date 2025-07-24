package likelion.team1.mindscape.test.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestResponseSimpleDto {
    private Long testId;
    private Long userId;
    private String userType;
}
