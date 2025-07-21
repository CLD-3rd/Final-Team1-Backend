package likelion.team1.mindscape.content.service;

import likelion.team1.mindscape.content.dto.response.content.MusicResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

@Service
public class MusicService {
    @Value("${service.api.lastfm}")
    private String lastfmApi;

    public MusicResponse getMusicDetails(String artist, String title) throws IOException {
        // set query and request url -> create url object
        String artist_query = URLEncoder.encode(artist, "UTF-8");
        String title_query = URLEncoder.encode(title, "UTF-8");
        String apiURL = "http://ws.audioscrobbler.com/2.0/?method=track.getInfo&format=json&api_key=" + lastfmApi + "&artist=" + artist_query + "&track=" + title_query;
        URL url = new URL(apiURL);

        // open http connection
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // 200 = get inputstream, else = get errorstream
        int responseCode = connection.getResponseCode();
        BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        responseCode == 200 ? connection.getInputStream() : connection.getErrorStream()
                )
        );

        // read response line -> parse into jsonobject
        JSONObject musicJson = new JSONObject(br.readLine());
        // extract "track" array
        JSONObject musicInfo = musicJson.getJSONObject("track");

        // album image -> get lagrest one
        JSONArray images = musicInfo.getJSONObject("album").getJSONArray("image");
        String imageUrl = null;
        for (int i = images.length() - 1; i >= 0; i--) {
            JSONObject imageObj = images.getJSONObject(i);
            if (!imageObj.getString("#text").isEmpty()) {
                imageUrl = imageObj.getString("#text");
                break;
            }
        }

        // make result into musicresponse
        MusicResponse musicResponse = new MusicResponse(
                musicInfo.getString("name"),
                musicInfo.getJSONObject("artist").getString("name"),
                imageUrl
        );

        return musicResponse;
    }
}