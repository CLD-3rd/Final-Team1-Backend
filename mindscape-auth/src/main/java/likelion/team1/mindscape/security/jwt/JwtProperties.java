package likelion.team1.mindscape.security.jwt;

import lombok.Getter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class JwtProperties {

   @Value("${jwt.secret}")
   private String SECRET;

   @Value("${jwt.accessToken.ExpirationTime}")
   private int ACCESS_TOKEN_EXPIRATION;

   @Value("${jwt.refreshToken.ExpirationTime}")
   private int REFRESH_TOKEN_EXPIRATION;

   public static final String ACCESS_TOKEN_STRING = "AccessToken";
   public static final String REFRESH_TOKEN_STRING = "RefreshToken";
}
