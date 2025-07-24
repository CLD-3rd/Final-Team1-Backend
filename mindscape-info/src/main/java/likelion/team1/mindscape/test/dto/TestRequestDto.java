package likelion.team1.mindscape.test.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestRequestDto {
    private Long userId;
    private String userType;
    private String typeDescription;
}
