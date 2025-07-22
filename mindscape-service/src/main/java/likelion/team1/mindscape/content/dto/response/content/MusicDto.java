package likelion.team1.mindscape.content.dto.response.content;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MusicDto {
    private String title;

    private String artist;

    private String album;

    public static MusicDto from(MusicResponse response) {
        return new MusicDto(
                response.getTitle(),
                response.getArtist(),
                response.getAlbum()
        );
    }
}
