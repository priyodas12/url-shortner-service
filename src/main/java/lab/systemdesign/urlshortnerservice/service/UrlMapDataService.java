package lab.systemdesign.urlshortnerservice.service;

import io.micrometer.common.util.StringUtils;
import jakarta.validation.Valid;
import lab.systemdesign.urlshortnerservice.dao.UrlMapDataDao;
import lab.systemdesign.urlshortnerservice.model.Modifier;
import lab.systemdesign.urlshortnerservice.model.UrlMapData;
import lab.systemdesign.urlshortnerservice.model.api.create.UrlShortnerRequest;
import lab.systemdesign.urlshortnerservice.model.api.create.UrlShortnerResponse;
import lab.systemdesign.urlshortnerservice.model.api.search.ShortUrlSearchResponse;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Optional;

import static lab.systemdesign.urlshortnerservice.util.TimeUtil.convertToInstant;

@Service
public class UrlMapDataService {

    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = ALPHABET.length();

    private final static Logger log = LoggerFactory.getLogger(UrlMapDataService.class);

    private final UrlMapDataDao urlMapDataDao;

    @Autowired
    public UrlMapDataService(UrlMapDataDao urlMapDataDao) {
        this.urlMapDataDao = urlMapDataDao;
    }

    private static String encodeBase62(long num) {
        StringBuilder sb = new StringBuilder();
        while (num > 0) {
            int rem = (int) (num % BASE);
            sb.append(ALPHABET.charAt(rem));
            num /= BASE;
        }
        return sb.reverse().toString();
    }

    public ShortUrlSearchResponse getUrlMapData(String shortUrl, Modifier accessor) {
        log.info("getUrlMapData | Getting url map data for {}", shortUrl);

        Optional<UrlMapData> urlMapDataOptional = urlMapDataDao.getUrlMapDataByShortUrlHash(shortUrl);
        if (urlMapDataOptional.isPresent()) {
            UrlMapData urlMapData = urlMapDataOptional.get();
            if (ObjectUtils.isEmpty(urlMapData.getExpiresAt()) || (urlMapData.getExpiresAt().getTime() > Timestamp.from(Instant.now()).getTime())) {
                log.info("getUrlMapData | Url map data has expired for {}", shortUrl);
                return ShortUrlSearchResponse.builder()
                        .errorMessage("getUrlMapData | Url map data has expired for " + shortUrl)
                        .build();
            }
            ShortUrlSearchResponse response = convertToShortUrlSearchResponse(urlMapData);
            updateVisitCount(urlMapData, accessor);
            return response;
        } else {
            log.error("getUrlMapData | UrlMapDataDao.getUrlMapDataByShortUrlHash returned null");
            return ShortUrlSearchResponse.builder()
                    .errorMessage("getUrlMapData | empty returned for " + shortUrl)
                    .build();
        }
    }

    private void updateVisitCount(UrlMapData urlMapData, Modifier modifier) {
        log.info("updateVisitCount | Incrementing visit count: {} for {}", urlMapData.getVisitCount(), urlMapData.getShortUrlHash());
        urlMapData.setVisitCount(urlMapData.getVisitCount().add(BigInteger.valueOf(1)));
        urlMapData.setLastAccessedBy(StringUtils.isEmpty(String.valueOf(modifier)) ? Modifier.ADMIN : modifier);
        urlMapData.setLastAccessedAt(Timestamp.from(Instant.now()));
        urlMapDataDao.save(urlMapData);
    }

    public UrlShortnerResponse createUrlMapData(UrlShortnerRequest request) {
        log.info("createUrlMapData | Creating url map data for {}", request.getSourceUrl());
        UrlMapData urlMapData = convertToUrlMapData(request);
        UrlMapData urlMapDataResponse = urlMapDataDao.save(urlMapData);
        return convertUrlShortnerResponse(urlMapDataResponse);
    }

    private UrlShortnerResponse convertUrlShortnerResponse(UrlMapData savedUrlMapData) {
        log.info("convertUrlShortnerResponse | converting savedUrlMapData to urlShortnerResponse {}", savedUrlMapData.getSourceUrl());
        UrlShortnerResponse response = new UrlShortnerResponse();
        response.setShortUrlHash(savedUrlMapData.getShortUrlHash());
        response.setSourceUrl(savedUrlMapData.getSourceUrl());
        response.setCreatedAt(convertToInstant(savedUrlMapData.getCreatedAt()));
        response.setExpiresAt(convertToInstant(savedUrlMapData.getExpiresAt()));
        return response;
    }

    private ShortUrlSearchResponse convertToShortUrlSearchResponse(UrlMapData savedUrlMapData) {
        log.info("convertToShortUrlSearchResponse | converting savedUrlMapData to ShortUrlSearchResponse {}", savedUrlMapData.getSourceUrl());
        return ShortUrlSearchResponse.builder()
                .sourceUrl(savedUrlMapData.getSourceUrl())
                .shortUrl(savedUrlMapData.getShortUrlHash())
                .errorMessage(null)
                .build();
    }

    private UrlMapData convertToUrlMapData(UrlShortnerRequest request) {
        log.info("convertToUrlMapData | Converting incoming UrlShortnerRequest {}", request);
        UrlMapData urlMapData = new UrlMapData();
        urlMapData.setSourceUrl(request.getSourceUrl());
        urlMapData.setShortUrlHash(generateShortUrlHash(request.getSourceUrl()));
        urlMapData.setCreatedAt(Timestamp.from(Instant.now()));
        urlMapData.setUpdatedAt(Timestamp.from(Instant.now()));
        urlMapData.setVisitCount(BigInteger.ZERO);
        urlMapData.setLastAccessedAt(null);
        urlMapData.setExpiresAt(Timestamp.from(Instant.now().plus(1, ChronoUnit.HOURS)));
        return urlMapData;
    }

    private String generateShortUrlHash(@Valid String sourceUrl) {
        long millis = Instant.now().toEpochMilli();
        byte[] timeBytes = ByteBuffer.allocate(8).putLong(millis).array();
        byte[] shortTimeBytes = Arrays.copyOfRange(timeBytes, 2, 8);


        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        }
        catch (NoSuchAlgorithmException e) {
            log.error("generateShortUrlHash | Exception while creating messageDigest :{}", ExceptionUtils.getRootCauseMessage(e));
            throw new RuntimeException(e);
        }
        byte[] urlHash = Arrays.copyOfRange(digest.digest(sourceUrl.getBytes(StandardCharsets.UTF_8)), 0, 10);

        byte[] combined = new byte[16];
        System.arraycopy(shortTimeBytes, 0, combined, 0, 6);
        System.arraycopy(urlHash, 0, combined, 6, 10);

        try {
            MessageDigest sha256Digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = sha256Digest.digest(combined);
            long value = new BigInteger(1, encodedHash).longValue();
            return encodeBase62(value);
        }
        catch (NoSuchAlgorithmException e) {
            log.error("generateShortUrlHash | Exception while creating base62 encoded value: {}", ExceptionUtils.getRootCauseMessage(e));
            throw new RuntimeException(e);
        }
    }
}
