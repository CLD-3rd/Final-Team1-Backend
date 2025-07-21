package likelion.team1.mindscape.content.dto.response.content;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@Builder
@ToString
public class MusicResponse {
    private String title;
    private String artist;
    private String album; // image
}