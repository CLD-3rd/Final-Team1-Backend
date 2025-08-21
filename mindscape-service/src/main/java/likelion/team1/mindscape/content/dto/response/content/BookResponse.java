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
public class BookResponse {
    private String title;
    private String author;
    private String description;
    private String image;

    public static BookResponse fromRedis(Map<Object, Object> cached) {
        return new BookResponse(
                (String) cached.getOrDefault("title", ""),
                (String) cached.getOrDefault("author", ""),
                (String) cached.getOrDefault("description", ""),
                (String) cached.getOrDefault("image", "")
        );
    }
}
