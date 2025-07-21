package likelion.team1.mindscape.user.controller;

import likelion.team1.mindscape.user.dto.UserResponseDto;
import likelion.team1.mindscape.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponseDto getMyInfo(@RequestParam Long userId) {
        return userService.getUserById(userId);
    }
}
