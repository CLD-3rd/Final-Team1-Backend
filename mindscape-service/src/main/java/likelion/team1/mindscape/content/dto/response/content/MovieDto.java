package likelion.team1.mindscape.content.dto.response.content;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class MovieDto {
    private String title;

    @JsonProperty("release_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date releaseDate;

    @JsonProperty("overview")
    private String description;

    @JsonProperty("poster_path")
    private String poster;

    public MovieDto() {}
    public MovieDto(String title) {
        this.title = title;
    }
    public static MovieDto fromRedisHash(Map<Object, Object> hash) {
        MovieDto dto = new MovieDto();
        dto.setTitle((String) hash.get("title"));
        dto.setDescription((String) hash.get("description"));
        dto.setPoster((String) hash.get("poster"));
        try {
            dto.setReleaseDate(new SimpleDateFormat("yyyy-MM-dd").parse((String) hash.get("release_date")));
        } catch (ParseException e) {
            dto.setReleaseDate(null);
        }
        return dto;
    }
}
