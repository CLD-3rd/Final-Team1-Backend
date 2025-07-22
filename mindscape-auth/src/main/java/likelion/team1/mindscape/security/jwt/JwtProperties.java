package likelion.team1.mindscape.security.jwt;

import lombok.Value;


public interface JwtProperties {
    String SECRET = "dlrjsJWTxhzmsdmfdkaghghk,alcqhrghkgkfEotkdydgksmszldlqslek!";
    int EXPIRATION_TIME = 60 * 60 * 24 * 1000;
    String TOKEN_PREFIX = "Bearer ";
    String HEADER_STRING = "AccessToken";
}
