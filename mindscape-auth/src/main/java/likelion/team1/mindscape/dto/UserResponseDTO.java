package likelion.team1.mindscape.dto;

import likelion.team1.mindscape.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponseDTO {
    private Long id;
    private String username;

    public static UserResponseDTO from(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .build();
    }
}
