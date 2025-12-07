package lab.systemdesign.urlshortnerservice.controller;


import lab.systemdesign.urlshortnerservice.model.Modifier;
import lab.systemdesign.urlshortnerservice.model.api.create.UrlShortnerRequest;
import lab.systemdesign.urlshortnerservice.model.api.create.UrlShortnerResponse;
import lab.systemdesign.urlshortnerservice.model.api.search.ShortUrlSearchRequest;
import lab.systemdesign.urlshortnerservice.model.api.search.ShortUrlSearchResponse;
import lab.systemdesign.urlshortnerservice.service.UrlMapDataService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/url-shortner")
@RestController
public class UrlShortnerController {

    private final static Logger log = LoggerFactory.getLogger(UrlShortnerController.class);

    private final UrlMapDataService urlMapDataService;

    @Autowired
    public UrlShortnerController(UrlMapDataService urlMapDataService) {
        this.urlMapDataService = urlMapDataService;
    }


    @GetMapping("/search")
    public ResponseEntity<ShortUrlSearchResponse> searchShortUrl(@RequestBody ShortUrlSearchRequest request) {
        log.info("searchShortUrl | {}", request);
        try {
            Modifier modifier = Modifier.valueOf(request.getModifier());
            var response = urlMapDataService.getUrlMapData(request.getShortUrl(), modifier);
            if (ObjectUtils.isNotEmpty(response) && StringUtils.isBlank(response.getErrorMessage())) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        }
        catch (Exception e) {
            log.error("searchShortUrl | {}", ExceptionUtils.getStackTrace(e));
            var badResponse = ShortUrlSearchResponse
                    .builder()
                    .errorMessage(e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(badResponse);
        }
    }

    @PostMapping("/")
    public ResponseEntity<UrlShortnerResponse> generateShortUrl(@RequestBody UrlShortnerRequest request) {
        log.info("saveShortUrl | {}", request);
        try {
            UrlShortnerResponse urlShortnerResponse = urlMapDataService.createUrlMapData(request);
            return ResponseEntity.ok().body(urlShortnerResponse);
        }
        catch (Exception e) {
            log.error("saveShortUrl | {}", ExceptionUtils.getStackTrace(e));
            var badResponse = new UrlShortnerResponse();
            badResponse.setErrorMessage(e.getMessage());
            badResponse.setSourceUrl(request.getSourceUrl());
            badResponse.setShortUrlHash(null);
            return ResponseEntity.badRequest().body(badResponse);
        }
    }
}
