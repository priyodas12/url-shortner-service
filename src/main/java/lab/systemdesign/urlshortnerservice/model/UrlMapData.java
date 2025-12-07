package lab.systemdesign.urlshortnerservice.model;


import jakarta.persistence.*;
import lombok.Data;

import java.math.BigInteger;
import java.sql.Timestamp;

@Entity
@Table(name = "url_map_data")
@Data
public class UrlMapData {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private BigInteger id;

    @Column(name = "source_url")
    private String sourceUrl;

    @Column(name = "short_url_hash")
    private String shortUrlHash;

    @Column(name = "created_by")
    private Modifier lastAccessedBy;

    @Column(name = "visit_count")
    private BigInteger visitCount;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "expire_at")
    private Timestamp expiresAt;

    @Column(name = "last_accessed_at")
    private Timestamp lastAccessedAt;

}
