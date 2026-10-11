package pl.archiwum.seasonalleaves;

import net.minecraft.client.multiplayer.ClientLevel;

import java.lang.reflect.Method;
import java.util.Locale;

/**
 * Reads Serene Seasons without linking against its changing API at compile time.
 * If Serene Seasons is absent or its API cannot be read, leaves are left visible.
 */
public final class SeasonReader {
    private static volatile float cachedShedFraction;
    private static volatile String cachedSubSeason = "";

    private SeasonReader() {}

    public static float update(ClientLevel level) {
        if (level == null) {
            cachedSubSeason = "";
            cachedShedFraction = 0.0f;
            return cachedShedFraction;
        }
        cachedShedFraction = readShedFraction(level);
        return cachedShedFraction;
    }

    public static float shedFraction() {
        return cachedShedFraction;
    }

    public static String subSeason() {
        return cachedSubSeason;
    }

    private static float readShedFraction(ClientLevel level) {
        try {
            Class<?> helper = Class.forName("sereneseasons.api.season.SeasonHelper");
            Object state = null;
            for (Method method : helper.getMethods()) {
                if (!method.getName().equals("getSeasonState") || method.getParameterCount() != 1) continue;
                if (method.getParameterTypes()[0].isInstance(level)) {
                    state = method.invoke(null, level);
                    break;
                }
            }
            if (state == null) return 0.0f;

            String season = enumString(invokeNoArgs(state, "getSeason"));
            String subSeason = enumString(invokeNoArgs(state, "getSubSeason"));
            cachedSubSeason = subSeason.toUpperCase(Locale.ROOT);
            String combined = (season + " " + subSeason).toUpperCase(Locale.ROOT);

            if (combined.contains("MID_WINTER") || combined.contains("LATE_WINTER") || combined.contains("WINTER")) {
                if (combined.contains("EARLY_WINTER")) return 0.70f;
                return 1.0f;
            }
            if (combined.contains("LATE_AUTUMN")) {
                return readSubSeasonProgress(state);
            }
            if (combined.contains("MID_AUTUMN")) return 0.0f;
            if (combined.contains("EARLY_AUTUMN")) return 0.0f;
            if (combined.contains("EARLY_SPRING")) {
                return 1.0f - readSubSeasonProgress(state);
            }
            if (combined.contains("SPRING")) return 0.0f;
            return 0.0f;
        } catch (Throwable ignored) {
            cachedSubSeason = "";
            return 0.0f;
        }
    }

    private static Object invokeNoArgs(Object target, String name) {
        try {
            Method method = target.getClass().getMethod(name);
            return method.invoke(target);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static String enumString(Object value) {
        return value == null ? "" : value.toString();
    }

    private static float readSubSeasonProgress(Object state) {
        Object duration = invokeNoArgs(state, "getSubSeasonDuration");
        Object cycleTicks = invokeNoArgs(state, "getSeasonCycleTicks");
        if (duration instanceof Number durationNumber && cycleTicks instanceof Number ticksNumber
                && durationNumber.longValue() > 0) {
            long durationTicks = durationNumber.longValue();
            long ticksIntoCycle = Math.floorMod(ticksNumber.longValue(), durationTicks);
            return (float) ticksIntoCycle / durationTicks;
        }
        return 0.5f;
    }

    public static float poplarSaturationMultiplier() {
        return switch (cachedSubSeason) {
            case "EARLY_SPRING" -> 0.60f;
            case "MID_SPRING" -> 0.70f;
            case "LATE_SPRING" -> 0.78f;
            case "EARLY_SUMMER" -> 0.82f;
            case "MID_SUMMER" -> 0.85f;
            case "LATE_SUMMER" -> 0.92f;
            case "EARLY_AUTUMN" -> 0.96f;
            case "MID_AUTUMN" -> 1.00f;
            case "LATE_AUTUMN" -> 0.95f;
            case "EARLY_WINTER" -> 0.70f;
            case "MID_WINTER" -> 0.50f;
            case "LATE_WINTER" -> 0.55f;
            default -> 1.00f;
        };
    }
}
