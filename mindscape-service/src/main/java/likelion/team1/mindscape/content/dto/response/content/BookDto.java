package likelion.team1.mindscape.content.dto.response.content;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookDto {
    private String title;

    private String author;

    private String description;

    private String image;

    public static BookDto from(BookResponse response) {
        return new BookDto(
                response.getTitle(),
                response.getAuthor(),
                response.getDescription(),
                response.getImage()
        );
    }
}