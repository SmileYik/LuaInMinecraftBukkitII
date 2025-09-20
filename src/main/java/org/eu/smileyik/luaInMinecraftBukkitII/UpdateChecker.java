package org.eu.smileyik.luaInMinecraftBukkitII;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Data;
import lombok.ToString;
import org.eu.smileyik.simpledebug.DebugLogger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Logger;

public class UpdateChecker {
    private static final String RELEASE_URL = "https://api.github.com/repos/SmileYik/LuaInMinecraftBukkitII/releases";

    public List<Node> getReleases() {
        try {
            URL url = new URL(RELEASE_URL);
            URLConnection conn = url.openConnection();
            try (
                    InputStream inputStream = conn.getInputStream();
                    InputStreamReader reader = new InputStreamReader(inputStream);
            ) {
                Gson gson = new Gson();
                return gson.fromJson(reader, new TypeToken<List<Node>>() {}.getType());
            }
        } catch (Exception e) {
            DebugLogger.debug("Error while getting releases");
            DebugLogger.debug(e);
        }
        return Collections.emptyList();
    }

    public long getCurrentBuildTimestamp() {
        try (InputStream manifestStream = UpdateChecker.class.getResourceAsStream("/META-INF/MANIFEST.MF")) {
            if (manifestStream == null) {
                return 0;
            }

            Manifest manifest = new Manifest(manifestStream);
            Attributes attributes = manifest.getMainAttributes();
            String value = attributes.getValue("Build-Timestamp");
            return Long.parseLong(value);
        } catch (Exception e) {
            return 0;
        }
    }

    public void checkForUpdates(Logger logger) {
        logger.info("Checking for updates...");
        long currentBuildTimestamp = getCurrentBuildTimestamp();
        if (currentBuildTimestamp == 0) {
            logger.warning("Cannot determine current build timestamp, failed to check for updates.");
            return;
        }
        Node target = null;
        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            Future<List<Node>> submit = executor.submit(this::getReleases);
            List<Node> releases = submit.get(60, TimeUnit.SECONDS);
            for (Node node : releases) {
                if (!node.isDraft() && !node.isPrerelease()
                        && node.getPublishedTimestamp() > currentBuildTimestamp) {
                    target = node;
                    break;
                }
            }
        } catch (ExecutionException | InterruptedException | TimeoutException | CancellationException e) {
            logger.warning("Error checking for updates. " + e);
            DebugLogger.debug(e);
            return;
        }
        String msg;
        if (target == null) {
            msg = "No newer version available.";
        } else {
            msg = "Found new release tag `" + target.getTag_name() +
                    "`, published at " + target.getPublished_at() +
                    ": " + target.html_url + "\n" +
                    target.getName() + "\n" +
                    target.getBody() + "\n";
        }
        logger.info(msg);
    }

    /**
     * 更新节点
     */
    @Data
    @ToString
    public static final class Node {
        private String body;
        private String tag_name;
        private String name;
        private boolean draft;
        private boolean prerelease;
        private String published_at;
        private String html_url;

        public long getPublishedTimestamp() {
            try {
                Instant instant = Instant.parse(published_at);
                return instant.toEpochMilli();
            } catch (DateTimeParseException e) {
                return 0;
            }
        }
    }
}
