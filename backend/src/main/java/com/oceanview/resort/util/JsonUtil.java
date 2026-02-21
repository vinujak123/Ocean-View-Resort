package com.oceanview.resort.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for JSON serialization and deserialization using Gson
 */
public class JsonUtil {

    private static final Gson gson;

    static {
        // Configure Gson with LocalDate adapter
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class,
                        (JsonSerializer<LocalDate>) (src, typeOfSrc, context) -> context
                                .serialize(src.format(DateTimeFormatter.ISO_LOCAL_DATE)))
                .registerTypeAdapter(LocalDate.class,
                        (JsonDeserializer<LocalDate>) (json, typeOfT, context) -> LocalDate.parse(json.getAsString(),
                                DateTimeFormatter.ISO_LOCAL_DATE))
                .setPrettyPrinting()
                .create();
    }

    /**
     * Convert object to JSON string
     */
    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    /**
     * Convert JSON string to object
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    /**
     * Get Gson instance for advanced usage
     */
    public static Gson getGson() {
        return gson;
    }
}
