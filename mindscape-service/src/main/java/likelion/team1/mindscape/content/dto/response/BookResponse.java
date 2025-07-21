package likelion.team1.mindscape.content.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@Builder
@ToString
public class BookResponse {
    private String title;
    private String author;
    private String description;
    private String image;
}
