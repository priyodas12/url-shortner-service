package lab.systemdesign.urlshortnerservice.util;

import java.sql.Timestamp;
import java.time.Instant;

public class TimeUtil {

    public static Instant convertToInstant(Timestamp createdAt) {
        return Instant.ofEpochMilli(createdAt.getTime());
    }
}
