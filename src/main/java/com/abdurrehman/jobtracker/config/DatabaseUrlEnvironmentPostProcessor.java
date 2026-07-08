package com.abdurrehman.jobtracker.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Railway (and Heroku-style platforms) expose the database connection as
 * postgres://user:pass@host:port/db, but Spring's datasource needs
 * jdbc:postgresql://host:port/db with username/password as separate
 * properties. This runs before the context starts and normalizes whichever
 * raw URL is present (DATABASE_URL, or DB_URL if that's what got wired to
 * the raw value) into the properties Spring actually binds to.
 */
public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String rawUrl = environment.getProperty("DATABASE_URL");
        if (rawUrl == null || rawUrl.isBlank()) {
            rawUrl = environment.getProperty("DB_URL");
        }

        if (rawUrl == null || rawUrl.isBlank() || rawUrl.startsWith("jdbc:")) {
            return;
        }

        try {
            URI uri = new URI(rawUrl);
            String[] userInfo = uri.getUserInfo() != null ? uri.getUserInfo().split(":", 2) : new String[0];
            int port = uri.getPort() == -1 ? 5432 : uri.getPort();
            String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + port + uri.getPath();

            Map<String, Object> properties = new LinkedHashMap<>();
            properties.put("spring.datasource.url", jdbcUrl);
            if (userInfo.length > 0) {
                properties.put("spring.datasource.username", userInfo[0]);
            }
            if (userInfo.length > 1) {
                properties.put("spring.datasource.password", userInfo[1]);
            }

            environment.getPropertySources()
                    .addFirst(new MapPropertySource("normalizedDatabaseUrl", properties));
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Malformed database URL: " + rawUrl, e);
        }
    }
}
