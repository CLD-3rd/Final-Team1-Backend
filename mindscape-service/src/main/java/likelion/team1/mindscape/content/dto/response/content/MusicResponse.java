package likelion.team1.mindscape.content.dto.response.content;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;

@Getter
@AllArgsConstructor
@Builder
@ToString
public class MusicResponse {
    private String title;
    private String artist;
    private String album; // image

    public static MusicResponse fromRedis(Map<Object, Object> cached) {
        return new MusicResponse(
                (String) cached.getOrDefault("title", ""),
                (String) cached.getOrDefault("artist", ""),
                (String) cached.getOrDefault("album", "")
        );
    }
}