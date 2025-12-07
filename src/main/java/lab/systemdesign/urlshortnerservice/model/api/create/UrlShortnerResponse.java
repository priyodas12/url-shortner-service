package lab.systemdesign.urlshortnerservice.model.api.create;

import lombok.Data;

import java.time.Instant;


@Data
public class UrlShortnerResponse {

    private String sourceUrl;
    private String shortUrlHash;
    private Instant createdAt;
    private Instant expiresAt;
    private String errorMessage;
}
