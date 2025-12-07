package lab.systemdesign.urlshortnerservice.dao;

import lab.systemdesign.urlshortnerservice.model.UrlMapData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Optional;

@Repository
public interface UrlMapDataDao extends JpaRepository<UrlMapData, BigInteger> {

    Optional<UrlMapData> getUrlMapDataByShortUrlHash(String shortUrlHash);
}
