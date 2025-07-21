package likelion.team1.mindscape.content.dto.response.content;

import lombok.Data;
import lombok.Getter;

import java.util.List;

@Getter
public class TmdbResponse {
    private List<MovieDto> results;
}
