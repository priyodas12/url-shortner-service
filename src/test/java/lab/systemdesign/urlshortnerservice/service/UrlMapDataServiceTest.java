package lab.systemdesign.urlshortnerservice.service;

import lab.systemdesign.urlshortnerservice.model.api.create.UrlShortnerRequest;
import lab.systemdesign.urlshortnerservice.model.api.create.UrlShortnerResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.math.BigInteger;
import java.util.UUID;


class UrlMapDataServiceTest {

    @MockitoSpyBean
    private UrlMapDataService urlMapDataService;

    @Test
    void generateShortCode() {
        UrlShortnerResponse response = urlMapDataService.createUrlMapData(createTestData());
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getCreatedAt());
        Assertions.assertNotNull(response.getShortUrlHash());
        Assertions.assertTrue(response.getExpiresAt().isAfter(response.getCreatedAt()));
    }

    private UrlShortnerRequest createTestData() {
        var req = new UrlShortnerRequest();
        req.setSourceUrl("https://google.com/1/test/" + UUID.randomUUID());
        req.setExpiresInHours(BigInteger.ONE);
        return req;
    }
}