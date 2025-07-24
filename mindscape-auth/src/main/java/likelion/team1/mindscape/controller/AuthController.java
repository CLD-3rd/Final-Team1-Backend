package likelion.team1.mindscape.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import likelion.team1.mindscape.dto.UserResponseDTO;
import likelion.team1.mindscape.entity.User;
import likelion.team1.mindscape.repository.UserRepository;
import likelion.team1.mindscape.security.jwt.JwtProperties;
import likelion.team1.mindscape.service.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpResponse;
import java.util.Date;

@ResponseBody
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder bCryptPasswordEncoder;
    private final JwtProperties jwtProperties;

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

        // 로그아웃 시, refreshToken 삭제
        if(principalDetails != null){
            User user = principalDetails.getUser();
            user.clearRefreshToken();
            userRepository.save(user);
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

    @PostMapping("/refresh")
    public ResponseEntity<HttpStatus> refresh(HttpServletRequest request, HttpServletResponse response) {

        // refreshToken 쿠키에서 추출
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(JwtProperties.REFRESH_TOKEN_STRING)) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        // 예외처리
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try{
            // refreshToken 검증
            String accountId = JWT.decode(refreshToken).getClaim("accountId").asString();

            User user = userRepository.findByAccountId(accountId);

            // refreshToken 유효성 검사 및 DB의 refreshToken과 비교
            if(!user.isRefreshTokenValid() || !user.getRefreshToken().equals(refreshToken)){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // 새로운 accessToken 발급
            String newAccessToken = JWT.create()
                    .withSubject(user.getAccountId())
                    .withClaim("uid", user.getId())
                    .withClaim("uname", user.getUsername())
                    .withExpiresAt(new Date(System.currentTimeMillis() + jwtProperties.getACCESS_TOKEN_EXPIRATION()))
                    .sign(Algorithm.HMAC512(jwtProperties.getSECRET()));

            // accessToken 쿠키에 설정
            Cookie newAccessTokenCookie = new Cookie(JwtProperties.ACCESS_TOKEN_STRING, newAccessToken);
            newAccessTokenCookie.setHttpOnly(true);
            newAccessTokenCookie.setPath("/");

            response.addCookie(newAccessTokenCookie);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }

        return ResponseEntity.ok(HttpStatus.OK);
    }

}
