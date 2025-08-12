package likelion.team1.mindscape.content.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class ContentCountDto {
    private String title;
    private Long cnt;
    private List<Long> ids;

    public ContentCountDto(String title, Long cnt, List<Long> ids) {
        this.title = title;
        this.cnt = cnt;
        this.ids = ids;
    }
}