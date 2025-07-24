package likelion.team1.mindscape.user.dto;

import likelion.team1.mindscape.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponseDto {
    private Long id;
    private String accountId;
    private String username;

    public static UserResponseDto from(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .accountId(user.getAccountId())
                .username(user.getUsername())
                .build();
    }
}
