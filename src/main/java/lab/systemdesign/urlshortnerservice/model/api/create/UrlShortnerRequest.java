package lab.systemdesign.urlshortnerservice.model.api.create;


import jakarta.validation.Valid;
import lombok.Data;

import java.math.BigInteger;

@Data
public class UrlShortnerRequest {

    @Valid
    private String sourceUrl;
    private BigInteger expiresInHours;
}
