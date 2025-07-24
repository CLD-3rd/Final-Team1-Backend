package likelion.team1.mindscape.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import likelion.team1.mindscape.dto.UserResponseDTO;
import likelion.team1.mindscape.entity.User;
import likelion.team1.mindscape.repository.UserRepository;
import likelion.team1.mindscape.security.jwt.JwtProperties;
import likelion.team1.mindscape.service.PrincipalDetails;
import likelion.team1.mindscape.service.RedisRefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final JwtProperties jwtProperties;
    private final RedisRefreshTokenService redisRefreshTokenService;

    //사용자 인증 검토
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> info(@AuthenticationPrincipal PrincipalDetails principalDetails) {

        if (principalDetails == null) {
            return ResponseEntity.status(204).build();
        }

        User user = userRepository.findByAccountId(principalDetails.getAccountId());

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

    //아아디 중복확인
    @GetMapping("/duplicate")
    public ResponseEntity<HttpStatus> duplicate(@RequestParam String accountId){

        User user = userRepository.findByAccountId(accountId);

        if(user != null){
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        return ResponseEntity.ok(HttpStatus.NOT_ACCEPTABLE);
    }


    //로그아웃
    @PostMapping("/logout")
    public ResponseEntity<HttpStatus> logout(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                             HttpServletResponse response) {

        // 로그아웃 시. 리프레시 토큰 삭제
        if(principalDetails != null){
            redisRefreshTokenService.deleteRefreshToken(principalDetails.getAccountId());
        }


        Cookie accessTokenCookie = new Cookie("AccessToken", "null");
        accessTokenCookie.setMaxAge(0);
        accessTokenCookie.setPath("/");

        Cookie refreshTokenCookie = new Cookie("refreshToken", "null");
        refreshTokenCookie.setMaxAge(0);
        refreshTokenCookie.setPath("/");

        response.addCookie(accessTokenCookie);

        return ResponseEntity.ok(HttpStatus.OK);
    }

}
