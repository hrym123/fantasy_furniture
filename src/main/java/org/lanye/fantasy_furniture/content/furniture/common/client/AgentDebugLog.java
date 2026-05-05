package org.lanye.fantasy_furniture.content.furniture.common.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import net.minecraftforge.fml.loading.FMLPaths;

/**
 * 调试会话 NDJSON 日志（写入工程根 {@code debug-cac4e1.log}）。
 */
public final class AgentDebugLog {

    private static final String SESSION = "cac4e1";

    private AgentDebugLog() {}

    // #region agent log
    public static void log(String hypothesisId, String location, String message, String dataJsonObject) {
        Path gameDir = FMLPaths.GAMEDIR.get();
        Path logFile = gameDir.getParent().resolve("debug-cac4e1.log").normalize();
        long ts = System.currentTimeMillis();
        String line = String.format(
                Locale.ROOT,
                "{\"sessionId\":\"%s\",\"timestamp\":%d,\"hypothesisId\":\"%s\",\"location\":\"%s\",\"message\":\"%s\",\"data\":%s}%n",
                SESSION,
                ts,
                escapeJson(hypothesisId),
                escapeJson(location),
                escapeJson(message),
                dataJsonObject == null || dataJsonObject.isBlank() ? "{}" : dataJsonObject);
        try {
            Files.writeString(logFile, line, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ignored) {
            // debug only
        }
    }

    private static String escapeJson(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
    // #endregion
}
