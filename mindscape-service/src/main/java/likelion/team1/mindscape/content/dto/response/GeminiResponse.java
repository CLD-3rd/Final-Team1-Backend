package likelion.team1.mindscape.content.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GeminiResponse  {
    private List<String> movie;
    private List<String> book;
    private List<String> music;
}