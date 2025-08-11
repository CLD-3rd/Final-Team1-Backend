package likelion.team1.mindscape.test.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;

public class JwtUtil {

    private static final String SECRET_KEY = "dlrjsJWTxhzmsdmfdkaghghk,alcqhrghkgkfEotkdydgksmszldlqslek!"; // auth-service에서 사용하는 서명 키와 동일해야 함

    public static Long extractUserId(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY.getBytes(StandardCharsets.UTF_8))
                .parseClaimsJws(token.replace("Bearer ", ""))
                .getBody();

        return claims.get("userId", Long.class); // 클레임에서 userId 추출
    }
}
