package likelion.team1.mindscape.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true)
    private String accountId;

    @Column(nullable = false)
    private String username;

    @Column
    private String password;

    @Column
    private String provider;    // OAuth2 제공자 (google)

    @Column
    private String providerId;  // OAuth2 제공자가 제공하는 ID

    // 리프레시 토큰 저장 필드
    @Transient // DB컬럼 생성 제외 - redis로 관리하기 때문에.
    private String refreshToken;

    // 리프레시 토큰 만료기간
    @Transient // DB컬럼 생성 제외 - redis로 관리하기 때문에.
    private LocalDateTime tokenExpiryDate;

    // 리프레시 토큰 만료기간 업데이트
    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        this.tokenExpiryDate = LocalDateTime.now().plusDays(14);
    }

    // 리프레시 토큰 유효성 검사
    public boolean isRefreshTokenValid() {
        return refreshToken != null &&      //refreshToken 있어야하고,
                tokenExpiryDate != null &&  // tokenExpiryDate 있어야하고,
                LocalDateTime.now().isBefore(tokenExpiryDate);  // 재발급시기가 올바른시점이여야 한다.
    }

    // 리프레시 토큰 제거 (로그아웃 등)
    public void clearRefreshToken() {
        this.refreshToken = null;
        this.tokenExpiryDate = null;
    }

    public boolean vailateRefreshToken(String refreshToken) {
        return this.refreshToken.equals(refreshToken);
    }
}
