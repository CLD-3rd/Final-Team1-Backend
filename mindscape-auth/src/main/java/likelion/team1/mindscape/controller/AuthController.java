package likelion.team1.mindscape.controller;

import likelion.team1.mindscape.entity.User;
import likelion.team1.mindscape.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@ResponseBody
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder bCryptPasswordEncoder;

    //사용자 인증 검토
    @GetMapping("/info")
    public String auth(){
        return "Hello World";
    }

    //회원가입
    @PostMapping("/join")
    public String join(@RequestBody User User){

        User.setPassword(bCryptPasswordEncoder.encode(User.getPassword()));
        userRepository.save(User);

        return "redirect:/";
    }


}
