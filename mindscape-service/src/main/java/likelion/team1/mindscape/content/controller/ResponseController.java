package likelion.team1.mindscape.content.controller;

import likelion.team1.mindscape.content.client.TestServiceClient;
import likelion.team1.mindscape.content.dto.response.HistoryResponse;
import likelion.team1.mindscape.content.dto.response.content.BookDto;
import likelion.team1.mindscape.content.dto.response.content.MovieDto;
import likelion.team1.mindscape.content.dto.response.content.MusicDto;
import likelion.team1.mindscape.content.global.security.AESUtil;
import likelion.team1.mindscape.content.service.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/response")
@RequiredArgsConstructor
public class ResponseController {

    private final AESUtil aesUtil;
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

    @GetMapping(value = "/history", params = {"userId", "page", "size"})
    public ResponseEntity<List<HistoryResponse>> getHistoryByUserId(@RequestParam Long userId,
                                                                    @RequestParam(defaultValue = "0") int page,
                                                                    @RequestParam(defaultValue = "5") int size) {
        //TODO: get test ID with user ID
        List<Long> testIds = testServiceClient.getTestIdsByUserId(userId, page, size);
        List<HistoryResponse> responses = new ArrayList<>();

        for (Long testId : testIds) {
            responses.add(getHistoryByTestId(testId).getBody());
        }
        return ResponseEntity.ok().body(responses);
    }

    @GetMapping(value = "/share/{testId}/{name}")
    public ResponseEntity<Model> createShareLink(@PathVariable String testId,
                                                  @PathVariable String name, Model model) throws Exception {

        String rowData = testId + "|" + name;

        //testId 암호화
        String encryptedTestId = aesUtil.encrypt(rowData);

        //testId URL값에 맞게 인코딩
        String sharingURLvalue = URLEncoder.encode(encryptedTestId, "UTF-8");

        model.addAttribute("value", sharingURLvalue);

        return ResponseEntity.ok(model);
    }

    @GetMapping(value = "/share", params = "value")
    public ResponseEntity<Model> getHistoryBySharingURLvalue(@RequestParam String value, Model model) throws Exception {

        //testId URL값에 맞게 인코딩
        String decodedData = URLDecoder.decode(value, "UTF-8");

        //testId 복호화
        String encryptedTestId = aesUtil.decrypt(decodedData);



        String testId = encryptedTestId.split("\\|")[0];

        String name = encryptedTestId.split("\\|")[1];


        ResponseEntity<HistoryResponse> recommendHistory = getHistoryByTestId(Long.parseLong(testId));

        model.addAttribute("name", name);
        model.addAttribute("RecommendHistory", recommendHistory);

        return  ResponseEntity.ok(model);
    }


}
