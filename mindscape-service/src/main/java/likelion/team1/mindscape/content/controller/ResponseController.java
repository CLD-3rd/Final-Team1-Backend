package likelion.team1.mindscape.content.controller;

import likelion.team1.mindscape.content.client.TestServiceClient;
import likelion.team1.mindscape.content.dto.response.HistoryResponse;
import likelion.team1.mindscape.content.dto.response.content.BookDto;
import likelion.team1.mindscape.content.dto.response.content.MovieDto;
import likelion.team1.mindscape.content.dto.response.content.MusicDto;
import likelion.team1.mindscape.content.service.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/response")
@RequiredArgsConstructor
public class ResponseController {
    private final ResponseService responseService;
    private final TestServiceClient testServiceClient;

    @GetMapping(value = "/history", params = "testId")
    public ResponseEntity<HistoryResponse> getHistoryByTestId(@RequestParam Long testId) {
        List<BookDto> bookList = responseService.getBookDtoByTestId(testId);

        List<MusicDto> musicList = responseService.getMusicDtoByTestId(testId);

        List<MovieDto> movieList = responseService.getMovieDtoByTestId(testId);

        HistoryResponse.Recommend recommend = new HistoryResponse.Recommend(bookList, musicList, movieList);
        HistoryResponse response = new HistoryResponse(testId, recommend);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/history", params = "userId")
    public ResponseEntity<List<HistoryResponse>> getHistoryByUserId(@RequestParam Long userId) {
        //TODO: get test ID with user ID
        List<Long> testIds = testServiceClient.getTestIdsByUserId(userId);
        List<HistoryResponse> responses = new ArrayList<>();

        for (Long testId : testIds) {
            responses.add(getHistoryByTestId(testId).getBody());
        }
        return ResponseEntity.ok().body(responses);
    }
}
