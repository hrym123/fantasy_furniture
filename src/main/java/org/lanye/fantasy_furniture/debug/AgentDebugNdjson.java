package org.lanye.fantasy_furniture.debug;

import com.google.gson.JsonObject;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/** T007 调试：向工作区根目录 {@code debug-65612f.log} 追加 NDJSON（Gradle {@code run} 下 cwd 常为 {@code run/}）。 */
public final class AgentDebugNdjson {

    private static final Object LOCK = new Object();

    private AgentDebugNdjson() {}

    public static void append(String hypothesisId, String location, String message, JsonObject data) {
        // #region agent log
        try {
            JsonObject line = new JsonObject();
            line.addProperty("sessionId", "65612f");
            line.addProperty("timestamp", System.currentTimeMillis());
            line.addProperty("hypothesisId", hypothesisId);
            line.addProperty("location", location);
            line.addProperty("message", message);
            if (data != null) {
                line.add("data", data);
            }
            Path path = resolveLogPath();
            synchronized (LOCK) {
                Files.writeString(
                        path,
                        line + "\n",
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND);
            }
        } catch (Throwable ignored) {
        }
        // #endregion
    }

    private static Path resolveLogPath() {
        Path cwd = Path.of(System.getProperty("user.dir", "."));
        if (cwd.endsWith("run")) {
            Path parent = cwd.getParent();
            if (parent != null) {
                return parent.resolve("debug-65612f.log");
            }
        }
        return cwd.resolve("debug-65612f.log");
    }
}
