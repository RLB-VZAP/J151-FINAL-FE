package za.ac.vzap.trytons.frontend.util;

public final class APIConfig {

    private static final String DEFAULT_BASE_URL = "http://localhost:8080/J151-FINAL-BE/api";

    private APIConfig() {
    }

    public static String getBaseUrl() {
        String configuredUrl = firstNonBlank(
                System.getProperty("api.base.url"),
                System.getenv("TRYTONS_API_BASE_URL")
        );

        String baseUrl = configuredUrl == null ? DEFAULT_BASE_URL : configuredUrl;
        return baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }
}
