package likelion.team1.mindscape.content.service;

import likelion.team1.mindscape.content.client.GeminiApiClient;
import likelion.team1.mindscape.content.client.TestServiceClient;
import likelion.team1.mindscape.content.dto.response.GeminiResponse;
import likelion.team1.mindscape.content.dto.response.TestInfoResponse;
import likelion.team1.mindscape.content.entity.Book;
import likelion.team1.mindscape.content.entity.Movie;
import likelion.team1.mindscape.content.entity.Music;
import likelion.team1.mindscape.content.entity.RecomContent;
import likelion.team1.mindscape.content.repository.BookRepository;
import likelion.team1.mindscape.content.repository.MovieRepository;
import likelion.team1.mindscape.content.repository.MusicRepository;
import likelion.team1.mindscape.content.repository.RecomContentRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class GeminiService {
    private final TestServiceClient testServiceClient;
    private final GeminiApiClient geminiApiClient;
    private final BookRepository bookRepository;
    private final MovieRepository movieRepository;
    private final MusicRepository musicRepository;
    private final RecomContentRepository recomContentRepository;

    public GeminiResponse recommend(Long testId) {
        //---------------------------------------------
        // 1. TestServiceClient를 통해 실제 또는 임시 데이터 받기
        //나중에 변경
        TestInfoResponse testInfo = testServiceClient.getTestInfo(testId);

        String userType = testInfo.getUserType();

        String prompt = String.format(
                "사용자의 성향은 DISC검사 중 %s 입니다. 이 성향에 맞는 영화 3개, 책 3개, 음악 3개를 추천해줘. 아래 JSON 형식으로 출력해주세요. 답변은 json만 주세요:\n" +
                        "{\n  \"movie\": [\"제목1\", \"제목2\", \"제목3\"],\n  \"book\": [\"제목1\", \"제목2\", \"제목3\"],\n  \"music\": [\"가수 - 제목1\", \"가수 - 제목2\", \"가수 - 제목3\"]\n}",
                userType
        );

        GeminiResponse geminiResponse = geminiApiClient.getRecommendations(prompt);

        RecomContent savedRecom = recomContentRepository.save(new RecomContent(testId));

        for (String title : geminiResponse.getBook()) {
            bookRepository.save(new Book(title, null, null, null, savedRecom));
        }
        for (String title : geminiResponse.getMovie()) {
            movieRepository.save(new Movie(title, null, null, null, savedRecom));
        }
        for (String title : geminiResponse.getMusic()) {
            String[] tmp = title.split("[–-]", 2);
            if (tmp.length == 2) {
                musicRepository.save(new Music(tmp[1].trim(), tmp[0].trim(), null, savedRecom));
            }
        }
        return geminiResponse;
    }

}
