package lab.systemdesign.urlshortnerservice.service;

import lab.systemdesign.urlshortnerservice.model.api.create.UrlShortnerRequest;
import lab.systemdesign.urlshortnerservice.model.api.create.UrlShortnerResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

class UrlMapDataServiceTest {

    private final UrlMapDataService urlMapDataService = Mockito.mock(UrlMapDataService.class);

    @DisplayName("test - createUrlMapData()")
    @Test
    void generateShortCode() {
        UrlShortnerRequest request = createTestData();

        UrlShortnerResponse response = new UrlShortnerResponse();
        response.setShortUrlHash(UUID.randomUUID().toString().replace("-", "").substring(0, 7));
        response.setCreatedAt(Instant.now());
        response.setExpiresAt(Instant.now().plusSeconds(3600));
        Mockito.when(urlMapDataService.createUrlMapData(Mockito.any(UrlShortnerRequest.class))).thenReturn(response);

        UrlShortnerResponse mockResponse = urlMapDataService.createUrlMapData(request);


        Assertions.assertNotNull(mockResponse, "Response should not be null");
        Assertions.assertNotNull(mockResponse.getCreatedAt(), "CreatedAt should not be null");
        Assertions.assertNotNull(mockResponse.getShortUrlHash(), "ShortUrlHash should not be null");
        Assertions.assertTrue(
                mockResponse.getExpiresAt().isAfter(response.getCreatedAt()),
                "ExpiresAt should be after CreatedAt"
        );
    }

    private UrlShortnerRequest createTestData() {
        var req = new UrlShortnerRequest();
        req.setSourceUrl("https://google.com/1/test/" + UUID.randomUUID());
        req.setExpiresInHours(BigInteger.ONE);
        return req;
    }
}