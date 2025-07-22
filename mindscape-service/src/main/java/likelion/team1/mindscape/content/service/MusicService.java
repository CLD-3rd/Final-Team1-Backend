package likelion.team1.mindscape.content.service;

import jakarta.transaction.Transactional;
import likelion.team1.mindscape.content.dto.response.content.BookResponse;
import likelion.team1.mindscape.content.dto.response.content.MusicDto;
import likelion.team1.mindscape.content.dto.response.content.MusicResponse;
import likelion.team1.mindscape.content.entity.Book;
import likelion.team1.mindscape.content.entity.Music;
import likelion.team1.mindscape.content.entity.RecomContent;
import likelion.team1.mindscape.content.repository.MusicRepository;
import likelion.team1.mindscape.content.repository.RecomConentRepository;
import lombok.RequiredArgsConstructor;
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
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MusicService {
    @Value("${service.api.lastfm}")
    private String lastfmApi;

    private final MusicRepository musicRepository;
    private final RecomConentRepository recomContentRepository;

    public List<MusicResponse> getMusicDetails(List<String> artists, List<String> titles) throws IOException {
        List<MusicResponse> musicList = new ArrayList<>();
        for (int i = 0; i < titles.size(); i++) {
            musicList.add(getMusicDetail(artists.get(i), titles.get(i)));
        }
        return musicList;
    }

    public MusicResponse getMusicDetail(String artist, String title) throws IOException {
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

    @Transactional
    public List<Music> saveMusic(List<MusicDto> musicList, Long userId) {
        if (musicList == null || musicList.isEmpty()) {
            throw new IllegalArgumentException("music list is empty");
        }
        RecomContent recom = recomContentRepository.findLatestByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("추천 결과가 없습니다."));

        List<Music> toPersist = new ArrayList<>();

        for (MusicDto dto : musicList) {
            musicRepository.findByTitleAndArtist(dto.getTitle(), dto.getArtist())
                    .ifPresentOrElse(music -> {
                        // existed -> refresh recom info
                        music.setRecommendedContent(recom);
                        toPersist.add(music);
                    }, () -> {
                        // new music
                        Music music = new Music();
                        music.setTitle(dto.getTitle());
                        music.setArtist(dto.getArtist());
                        music.setElbum(dto.getAlbum());
                        music.setRecommendedContent(recom);
                        toPersist.add(music);
                    });
        }
        return musicRepository.saveAll(toPersist);
    }
}