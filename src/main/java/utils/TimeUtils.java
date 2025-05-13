package utils;

import java.time.Duration;
import java.time.LocalDateTime;

public class TimeUtils {
    public static String getTempoMancante(LocalDateTime scadenza) {
        LocalDateTime now = LocalDateTime.now();
        if (scadenza.isBefore(now)) return "Scaduta";
        Duration diff = Duration.between(now, scadenza);
        long giorni = diff.toDays();
        long ore = diff.minusDays(giorni).toHours();
        return (giorni > 0 ? giorni + " giorni " : "") + ore + " ore";
    }
}
