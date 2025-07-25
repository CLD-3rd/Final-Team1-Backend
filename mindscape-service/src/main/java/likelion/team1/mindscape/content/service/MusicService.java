package likelion.team1.mindscape.content.service;

import likelion.team1.mindscape.content.client.TestServiceClient;
import likelion.team1.mindscape.content.dto.response.TestInfoResponse;
import likelion.team1.mindscape.content.dto.response.content.MusicDto;
import likelion.team1.mindscape.content.dto.response.content.MusicResponse;
import likelion.team1.mindscape.content.entity.Music;
import likelion.team1.mindscape.content.repository.MusicRepository;
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
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MusicService {
    private static final String LASTFM_API_BASE = "http://ws.audioscrobbler.com/2.0/?method=track.getInfo&format=json&api_key=%s&artist=%s&track=%s";
    private static final String REDIS_MUSIC_KEY_PREFIX = "music:";
    @Value("${service.api.lastfm}")
    private String lastfmApi;

    private final MusicRepository musicRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisService redisService;
    private final ContentService contentService;
    private final TestServiceClient testServiceClient;


    /**
     * 단건 상세 조회 (외부 API 직접 호출)
     *
     * @param artist
     * @param title
     * @return
     * @throws IOException
     */
    public MusicResponse getMusicDetail(String artist, String title) throws IOException {
        return fetchFromLastfm(artist, title);
    }

    /**
     * 입력된 MusicDto 리스트를 Redis에 저장 (이미 있으면 스킵)
     *
     * @param musicList
     */
    public void saveMusicToRedis(List<MusicDto> musicList) {
        if (musicList == null || musicList.isEmpty()) {
            throw new IllegalArgumentException("music list is empty(Redis)");
        }

        musicList.stream()
                .filter(dto -> !hasRedisHash(makeRedisKey(dto.getTitle())))
                .forEach(dto -> {
                    log.info("[REDIS@MusicService.saveMusicToRedis] Save music '{}'", dto.getTitle());
                    Long id = redisService.MusicToRedis(dto);
                    log.info("'{}' : redis에 저장 완료 (id={})", dto.getTitle(), id);
                });
    }

    /**
     * testId(recomId)로 저장된 책 목록을 가져온 뒤,
     * 각각의 책에 대해 Redis -> LastFM API -> Redis 대체 순으로 정보를 보강
     *
     * @param testId
     * @return
     * @throws IOException
     */
    public List<Music> getMusicWithTestId(Long testId) throws IOException {
        TestInfoResponse testInfo = testServiceClient.getTestInfo(testId);
        Long userId = testInfo.getUserId();
        Long recomId = testId; // testId = recomId

        log.info("[SQL@MusicService.getMusicWithTestId] Find music by recomId={}", recomId);
        List<Music> musics = musicRepository.findAllByRecommendedContent_RecomId(recomId);
        if (musics.isEmpty()) {
            return musics;
        }

        // 중복 방지를 위해 이미 사용된 타이틀 모음
        Set<String> usedTitles = musics.stream()
                .map(Music::getTitle)
                .collect(Collectors.toCollection(HashSet::new));

        List<Music> toSave = new ArrayList<>();
        for (Music music : musics) {
            MusicResponse info = resolveMusicInfo(music.getArtist(), music.getTitle(), usedTitles);
            if (info == null) {
                log.warn("[fetch@MusicService.getMusicWithTestId] Returned nothing and no alternative found in Redis: {}", music.getTitle());
                continue;
            }
            applyMusicInfo(music, info);
            toSave.add(music);
        }
        log.info("[SQL@MusicService.getMusicWithTestId] Save {} music", toSave.size());
        List<Music> saved = musicRepository.saveAll(toSave);
        List<String> titles = saved.stream().map(Music::getTitle).toList();
        contentService.saveRecomContent(userId, testId, "music", titles);
        return saved;
    }

    /**
     * Helper
     * Redis -> LastFm API -> Redis 대체 순으로 MusicResponse 획득
     *
     * @param artist
     * @param title
     * @param usedTitles
     * @return
     * @throws IOException
     */
    private MusicResponse resolveMusicInfo(String artist, String title, Set<String> usedTitles) throws IOException {
        // 1) Redis 조회
        Optional<MusicResponse> cached = getFromRedis(title);
        if (cached.isPresent()) {
            MusicResponse cachedInfo = cached.get();
            if (isComplete(cachedInfo)) {
                log.info("[REDIS@MusicService.resolveMusicInfo] Found music '{}'", title);
                return cachedInfo;
            } else {
                log.warn("[REDIS@MusicService.resolveMusicInfo] Incomplete data '{}', Refresh via API", title);
            }
        }

        // 2) LastFM API 조회
        MusicResponse info = fetchFromLastfm(artist, title);

        // 3) LastFM 실패 시 Redis에서 대체 찾기
        if (!isComplete(info)) {
            if (info == null) {
                log.warn("[LASTFM API@MusicService.resolveMusicInfo] Failed for '{}'. Get alternative from Redis", title);
            } else {
                log.warn("[LASTFM API@MusicService.resolveMusicInfo] Incomplete MusicResponse '{}'. Get alternative from Redis", title);
            }
            info = redisService.getAlternativeMusic(new ArrayList<>(usedTitles));
            if (!isComplete(info)) {
                log.error("[REDIS@MusicService.resolveMusicInfo] No alternative found in Redis for '{}'", title);
                return null;
            }
            log.info("[REDIS@MusicService.resolveMusicInfo] Use alternative '{}'", info.getTitle());
        } else {
            cacheToRedis(info);
        }

        usedTitles.add(info.getTitle());
        return info;
    }

    /**
     * Helper
     * LastFM API 호출
     *
     * @param artist
     * @param title
     * @return
     * @throws IOException
     */
    private MusicResponse fetchFromLastfm(String artist, String title) throws IOException {
        log.info("[LASTFM API@MusicService.fetchFromLastfm] LastFM API used for artist = '{}', title='{}'", artist, title);

        String artistQuery = URLEncoder.encode(artist, StandardCharsets.UTF_8);
        String titleQuery = URLEncoder.encode(title, StandardCharsets.UTF_8);
        String apiURL = String.format(LASTFM_API_BASE, lastfmApi, artistQuery, titleQuery);

        HttpURLConnection connection = (HttpURLConnection) new URL(apiURL).openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        String body = readBody(connection, responseCode == 200);
        connection.disconnect();

        JSONObject musicJson = new JSONObject(body);
        JSONObject musicInfo = musicJson.optJSONObject("track");
        if (musicInfo == null || musicInfo.length() == 0) {
            log.warn("[LASTFM API@MusicService.fetchFromLastfm] LastFM API returned no results for artist = '{}', title='{}'", artist, title);
            return null;
        }

        return parseMusicResponse(musicInfo, artist, title);
    }

    /**
     * Helper
     * 기존 Music 객체를 새로운 MusicResponse정보로 교체
     *
     * @param music
     * @param info
     */
    private void applyMusicInfo(Music music, MusicResponse info) {
        music.setTitle(info.getTitle());
        music.setArtist(info.getArtist());
        music.setElbum(info.getAlbum());
    }

    /**
     * Helper
     * Redis에 key 있는지 조회, 있으면 True
     *
     * @param key
     * @return
     */
    private boolean hasRedisHash(String key) {
        try {
            log.info("[REDIS@MusicService.hasRedisHash] Check key type {}", key);
            String type = redisTemplate.type(key).code();
            if (!"hash".equals(type) && !"none".equals(type)) {
                log.warn("[REDIS@MusicService.hasRedisHash] Key {} is not hash type: {}", key, type);
                return false;
            }
            if ("none".equals(type)) {
                return false;
            }
            log.info("[REDIS@MusicService.hasRedisHash] Get hash size {}", key);
            Long size = redisTemplate.opsForHash().size(key);
            return size != null && size > 0;
        } catch (Exception e) {
            log.error("[REDIS@MusicService.hasRedisHash] Error checking key {}: {}", key, e.getMessage());
            return false;
        }
    }

    /**
     * Helper
     * Redis 키 생성
     *
     * @param title
     * @return
     */
    private String makeRedisKey(String title) {
        return REDIS_MUSIC_KEY_PREFIX + title;
    }

    /**
     * Helper
     * Redis에서 title로 정보 가져오기
     *
     * @param title
     * @return
     */
    private Optional<MusicResponse> getFromRedis(String title) {
        String key = makeRedisKey(title);
        try {
            log.info("[REDIS@MusicService.getFromRedis] Check key type {}", key);
            // 키 타입 확인
            String type = redisTemplate.type(key).code();
            if (!"hash".equals(type)) {
                log.debug("[REDIS@MusicService.getFromRedis] Key {} is not hash type: {}", key, type);
                return Optional.empty();
            }
            log.info("[REDIS@MusicService.getFromRedis] Load hash {}", key);
            Map<Object, Object> map = redisTemplate.opsForHash().entries(key);
            if (map == null || map.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(MusicResponse.fromRedis(map));
        } catch (Exception e) {
            log.error("[REDIS@MusicService.getFromRedis] Error getting music with key {}: {}", key, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Helper
     * Redis에 새로운 정보 저장
     *
     * @param info
     */
    private void cacheToRedis(MusicResponse info) {
        log.info("[REDIS@BookService.cacheToRedis] Save music to Redis title='{}'", info.getTitle());
        redisService.MusicToRedis(new MusicDto(info.getTitle(), info.getArtist(), info.getAlbum()));
    }

    /**
     * Helper
     * LastFM/Redis에서 얻은 BookResponse가 모든 필드를 채웠는지 검증
     *
     * @param info
     * @return
     */
    private boolean isComplete(MusicResponse info) {
        return info != null
                && notNullOrBlank(info.getTitle())
                && notNullOrBlank(info.getArtist())
                && notNullOrBlank(info.getAlbum());
    }

    private boolean notNullOrBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    /**
     * HttpURLConnection 응답 본문 읽기
     *
     * @param connection
     * @param successStream
     * @return
     * @throws IOException
     */
    private String readBody(HttpURLConnection connection, boolean successStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                successStream ? connection.getInputStream() : connection.getErrorStream(),
                StandardCharsets.UTF_8
        ))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    /**
     * Lastfm 응답 JSON을 BookResponse로 변환
     *
     * @param musicInfo
     * @param fallbackArtist
     * @param fallbackTitle
     * @return
     */
    private MusicResponse parseMusicResponse(JSONObject musicInfo, String fallbackArtist, String fallbackTitle) {
        // album image -> 가장 큰 사이즈 선택
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

        String trackName = musicInfo.optString("name", fallbackTitle);
        JSONObject artistObj = musicInfo.optJSONObject("artist");
        String artistName = artistObj != null ? artistObj.optString("name", fallbackArtist) : fallbackArtist;

        return new MusicResponse(trackName, artistName, imageUrl);
    }

}