package likelion.team1.mindscape.test.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestResponseDto {
    private Long testId;
    private String userType;
    private String typeDescription;
    private LocalDateTime createdAt;
}
