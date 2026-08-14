package config;

/**
 * Resolve a configuração de ambiente priorizando o que vier de fora
 * (system property "api.baseUrl" ou variável de ambiente API_BASE_URL),
 * com fallback para o data.yaml — assim o mesmo build roda tanto local
 * quanto no Jenkins, sem precisar recompilar nada.
 */
public final class Environment {

    private Environment() {
    }

    public static String getBaseUri() {
        String baseUrl = System.getProperty("api.baseUrl");

        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = System.getenv("API_BASE_URL");
        }

        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = Configuration.getBaseUri();
        }

        return baseUrl.trim();
    }
}
