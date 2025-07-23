package likelion.team1.mindscape.content.service;

import jakarta.transaction.Transactional;
import likelion.team1.mindscape.content.dto.response.content.MusicDto;
import likelion.team1.mindscape.content.dto.response.content.MusicResponse;
import likelion.team1.mindscape.content.entity.Book;
import likelion.team1.mindscape.content.entity.Music;
import likelion.team1.mindscape.content.entity.RecomContent;
import likelion.team1.mindscape.content.repository.MusicRepository;
import likelion.team1.mindscape.content.repository.RecomContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MusicService {
    @Value("${service.api.lastfm}")
    private String lastfmApi;

    private final MusicRepository musicRepository;
    private final RecomContentRepository recomContentRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisService redisService;

    public List<MusicResponse> getMusicDetails(List<String> artists, List<String> titles) throws IOException {
        List<MusicResponse> musicList = new ArrayList<>();
        for (int i = 0; i < titles.size(); i++) {
            MusicResponse detail = getMusicDetail(artists.get(i), titles.get(i));
            if (detail != null) {
                musicList.add(detail);
            } else {
                log.warn("Skip music '{}' - no result from Lastfm API", titles.get(i));
            }
        }
        return musicList;
    }

    public MusicResponse getMusicDetail(String artist, String title) throws IOException {
        // set query and request url -> create url object
        log.info("LASTFM API USED");
        String artist_query = URLEncoder.encode(artist, "UTF-8");
        String title_query = URLEncoder.encode(title, "UTF-8");
        String apiURL = "http://ws.audioscrobbler.com/2.0/?method=track.getInfo&format=json&api_key=" + lastfmApi + "&artist=" + artist_query + "&track=" + title_query;
        URL url = new URL(apiURL);

        // open http connection
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // 200 = get inputstream, else = get errorstream
        int responseCode = connection.getResponseCode();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                responseCode == 200 ? connection.getInputStream() : connection.getErrorStream()
        ))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        }

        // read response line -> parse into jsonobject
        JSONObject musicJson = new JSONObject(sb.toString());
        JSONObject musicInfo = musicJson.optJSONObject("track");
        // if no result from Lastfm API?
        if (musicInfo == null || musicInfo.length() == 0) {
            log.warn("Lastfm API returned no results for artist='{}', title='{}'", artist, title);
            return null;
        }

        // album image -> get largest one
        String imageUrl = null;
        JSONObject albumObj = musicInfo.optJSONObject("album");
        if (albumObj != null) {
            JSONArray images = albumObj.optJSONArray("image");
            if (images != null) {
                for (int i = images.length() - 1; i >= 0; i--) {
                    JSONObject imgObj = images.optJSONObject(i);
                    if (imgObj != null) {
                        String img = imgObj.optString("#text", "");
                        if (!img.isEmpty()) {
                            imageUrl = img;
                            break;
                        }
                    }
                }
            }
        }

        // make result into musicresponse
        String trackName = musicInfo.optString("name", title);
        JSONObject artistObj = musicInfo.optJSONObject("artist");
        String artistName = artistObj != null ? artistObj.optString("name", artist) : artist;

        MusicResponse musicResponse = new MusicResponse(
                trackName,
                artistName,
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

    public void saveMusicToRedis(List<MusicDto> musicList) {
        if (musicList == null || musicList.isEmpty()) {
            throw new IllegalArgumentException("music list is empty(Redis)");
        }
        for (MusicDto dto : musicList) {
            String searchPattern = "music:*" + dto.getTitle();
            Set<String> keys = redisTemplate.keys(searchPattern);
            if (keys != null && !keys.isEmpty()) {
                log.info(dto.getTitle() + ": redis에 이미 존재");
                continue;
            }
            // redis 저장
            Long id = redisService.MusicToRedis(dto);
            log.info(dto.getTitle() + ": redis에 저장 완료 (id=" + id + ")");
        }
    }

    public List<Music> getMusicWithTestId(Long testId) throws IOException {
        // 1. get recomm ID
        Long recomId = testId; // testId = recomId

        // 2. get pre-saved books with recom ID
        List<Music> musicList = musicRepository.findAllByRecommendedContent_RecomId(recomId);
        List<Music> toSave = new ArrayList<>();

        List<String> musicTitles = musicList.stream().map(Music::getTitle).collect(Collectors.toList());

        for (Music music : musicList) {
            String artist = music.getArtist();
            String title = music.getTitle();
            String key = "music:" + title;

            // 3. check redis
            Map<Object, Object> cached = redisTemplate.opsForHash().entries(key);
            MusicResponse info;
            // not existed
            if (cached == null || cached.isEmpty()) {
                info = getMusicDetail(artist, title);
                if (info == null) {
                    //get alternative from redis
                    info = redisService.getAlternativeMusic(musicTitles);
                    if (info == null) {
                        log.warn("'{}' - Lastfm api returned nothing and no alternative found in redis", title);
                        continue;
                    }
                    musicTitles.add(info.getTitle());
                } else {
                    MusicDto dto = new MusicDto(
                            info.getTitle(),
                            info.getArtist(),
                            info.getAlbum()
                    );
                    redisService.MusicToRedis(dto);
                    musicTitles.add(info.getTitle());
                }
            } else {
                // existed
                info = new MusicResponse(
                        (String) cached.get("title"),
                        (String) cached.get("artist"),
                        (String) cached.get("album")
                );
            }

            // 4. update
            music.setTitle(info.getTitle());
            music.setArtist(info.getArtist());
            music.setElbum(info.getAlbum());
            toSave.add(music);
        }

        // 5. save to sql
        return musicRepository.saveAll(toSave);
    }
}