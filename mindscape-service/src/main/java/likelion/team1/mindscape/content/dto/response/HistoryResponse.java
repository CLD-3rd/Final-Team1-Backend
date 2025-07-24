package likelion.team1.mindscape.content.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import likelion.team1.mindscape.content.dto.response.content.BookDto;
import likelion.team1.mindscape.content.dto.response.content.MovieDto;
import likelion.team1.mindscape.content.dto.response.content.MusicDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoryResponse {
    private Long testId;

    @JsonProperty("Recommend")
    private Recommend recommend;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Recommend {
        @JsonProperty("Book")
        private List<BookDto> book;
        @JsonProperty("Music")
        private List<MusicDto> music;
        @JsonProperty("Movie")
        private List<MovieDto> movie;
    }
}
