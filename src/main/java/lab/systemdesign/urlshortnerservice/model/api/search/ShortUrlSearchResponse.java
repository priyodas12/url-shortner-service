package lab.systemdesign.urlshortnerservice.model.api.search;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ShortUrlSearchResponse {

    private String shortUrl;
    private String sourceUrl;
    private String errorMessage;
}
