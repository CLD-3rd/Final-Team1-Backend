package likelion.team1.mindscape.content.dto.response.content;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class MovieDto {
    private String title;

    @JsonProperty("release_date")
    private Date releaseDate;

    @JsonProperty("overview")
    private String description;

    @JsonProperty("poster_path")
    private String poster;
}
