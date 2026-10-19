package com.template.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Centralized configuration manager.
 * Priority: System property (-Dkey=value) > Environment variable > config.properties
 */
public final class ConfigManager {

    private static final Properties properties = new Properties();

    static {
        try (InputStream input = ConfigManager.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                properties.load(input);
            } else {
                System.err.println("WARN: config.properties file not found on classpath.");
            }
        } catch (IOException e) {
            System.err.println("ERROR: Could not load config.properties: " + e.getMessage());
        }
    }

    private ConfigManager() {
        // Prevent instantiation
    }

    public static String getProperty(String key, String defaultValue) {
        String sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.isBlank()) {
            return sysProp;
        }

        String envVar = System.getenv(key.replace('.', '_').toUpperCase());
        if (envVar != null && !envVar.isBlank()) {
            return envVar;
        }

        return properties.getProperty(key, defaultValue);
    }

    public static String getProperty(String key) {
        return getProperty(key, "");
    }

    public static String getBaseUrl() {
        return getProperty("baseUrl", "http://localhost:8080");
    }

    public static String getApiBaseUrl() {
        return getProperty("apiBaseUrl", "http://localhost:8080/api");
    }

    public static String getBrowser() {
        return getProperty("browser", "chrome");
    }

    public static boolean isHeadless() {
        return Boolean.parseBoolean(getProperty("headless", "false"));
    }

    public static int getExplicitTimeout() {
        return Integer.parseInt(getProperty("timeout.explicit", "10"));
    }
}
