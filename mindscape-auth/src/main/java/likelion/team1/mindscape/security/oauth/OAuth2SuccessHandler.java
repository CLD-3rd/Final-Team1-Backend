package likelion.team1.mindscape.security.oauth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import likelion.team1.mindscape.entity.User;
import likelion.team1.mindscape.repository.UserRepository;
import likelion.team1.mindscape.security.jwt.JwtProperties;
import likelion.team1.mindscape.service.PrincipalDetails;
import likelion.team1.mindscape.service.RedisRefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Value("${server.frontend.pageUrl}")
    private String redirectPageUrl;

    private final UserRepository userRepository;
    private final JwtProperties jwtProperties;
    private final RedisRefreshTokenService redisRefreshTokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        User requestUser = principalDetails.getUser();

        String accountid = requestUser.getAccountId();

        User user = userRepository.findByAccountId(accountid);
        if (user == null) {
            throw new ServletException("User not found");
        }

        String accessToken = JWT.create()
                .withSubject(user.getAccountId())
                .withClaim("uid", user.getId())
                .withClaim("uname", user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtProperties.getACCESS_TOKEN_EXPIRATION()))
                .sign(Algorithm.HMAC512(jwtProperties.getSECRET()));

        String refreshToken = JWT.create()
                .withSubject(user.getAccountId())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtProperties.getREFRESH_TOKEN_EXPIRATION()))
                .sign(Algorithm.HMAC512(jwtProperties.getSECRET()));

        redisRefreshTokenService.saveRefreshToken(user.getAccountId(), refreshToken,
                jwtProperties.getREFRESH_TOKEN_EXPIRATION());


        String accessCookie = String.format(
                "AccessToken=%s; Path=/; Max-Age=%d; HttpOnly; Secure; SameSite=None",
                accessToken,jwtProperties.getACCESS_TOKEN_EXPIRATION());



        String refreshCookie = String.format(
                "RefreshToken=%s; Path=/; Max-Age=%d; HttpOnly; Secure; SameSite=None",
                refreshToken, jwtProperties.getREFRESH_TOKEN_EXPIRATION());


        Cookie jsessionidCookie = new Cookie("JSESSIONID", null);
        jsessionidCookie.setMaxAge(0);
        jsessionidCookie.setPath("/");


        response.addHeader("Set-Cookie", accessCookie);
        response.addHeader("Set-Cookie", refreshCookie);
        response.addCookie(jsessionidCookie);

        response.sendRedirect(redirectPageUrl);
    }
}
