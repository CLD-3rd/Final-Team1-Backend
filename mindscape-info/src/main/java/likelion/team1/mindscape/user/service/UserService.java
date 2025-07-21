package likelion.team1.mindscape.user.service;

import likelion.team1.mindscape.user.dto.UserResponseDto;
import likelion.team1.mindscape.user.entity.User;
import likelion.team1.mindscape.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponseDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

        return UserResponseDto.fromEntity(user);
    }
}
