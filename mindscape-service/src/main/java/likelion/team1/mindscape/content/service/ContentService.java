package likelion.team1.mindscape.content.service;

import likelion.team1.mindscape.content.dto.response.BookResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.*;

@Service
public class ContentService {
    @Value("${service.api.kakaobooks}")
    private String kakaoApi;

    public BookResponse getBookDetails(String title) throws IOException {
        String apiURL = "https://dapi.kakao.com/v3/search/book?query=";
        // set query and request url -> create url object
        String query = URLEncoder.encode(title, "UTF-8");
        String reqUrl = apiURL + query;
        URL url = new URL(reqUrl);

        // open http connection
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "KakaoAK " + kakaoApi);

        // 200 = get inputstream, else = get errorstream
        int responseCode = connection.getResponseCode();
        BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        responseCode == 200 ? connection.getInputStream() : connection.getErrorStream()
                )
        );

        // read response line -> parse into jsonobject
        JSONObject bookJson = new JSONObject(br.readLine());
        // extract "documents" array (incl. all books w/ same title)
        JSONArray bookInfo = bookJson.getJSONArray("documents");
        // get first book from bookInfo
        JSONObject book = bookInfo.getJSONObject(0);

        // author = jsonarray type => extract authors and make it into string type
        JSONArray authorsArray = book.getJSONArray("authors");
        String authors = String.join(", ", authorsArray.toList().stream()
                .map(Object::toString)
                .toArray(String[]::new));

        // make result into bookresponse
        BookResponse bookResponse = new BookResponse(
                (String) book.get("title"),
                authors,
                (String) book.get("contents"),
                (String) book.get("thumbnail"));

        return bookResponse;
    }
}
