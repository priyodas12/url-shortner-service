package lab.systemdesign.urlshortnerservice.model.api.search;

import lombok.Data;

@Data
public class ShortUrlSearchRequest {

    private String shortUrl;
    private String modifier;
}
