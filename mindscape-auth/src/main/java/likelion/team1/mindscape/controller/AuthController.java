package likelion.team1.mindscape.controller;

import likelion.team1.mindscape.dto.UserResponseDTO;
import likelion.team1.mindscape.entity.User;
import likelion.team1.mindscape.repository.UserRepository;
import likelion.team1.mindscape.service.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@ResponseBody
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder bCryptPasswordEncoder;

    //사용자 인증 검토
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> info(@AuthenticationPrincipal PrincipalDetails principalDetails, Model model) {

        if (principalDetails == null) {
            return ResponseEntity.status(401).build();
        }

        User user = userRepository.findByUsername(principalDetails.getUsername());

        return ResponseEntity.ok(UserResponseDTO.from(user));

    }

    //회원가입
    @PostMapping("/join")
    public ResponseEntity<HttpStatus> join(@RequestBody User User){

        try {
            User.setPassword(bCryptPasswordEncoder.encode(User.getPassword()));
            userRepository.save(User);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }


        return ResponseEntity.ok(HttpStatus.CREATED);
    }


}
