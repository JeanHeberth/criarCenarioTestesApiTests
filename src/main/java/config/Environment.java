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

    /**
     * URL base da API OFICIAL do Zephyr Scale (não a nossa API) - usada só
     * pela verificação e2e que confirma, direto na fonte, que o caso de
     * teste publicado realmente existe.
     */
    public static String getZephyrBaseUri() {
        String baseUrl = System.getProperty("zephyr.baseUrl");

        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = System.getenv("ZEPHYR_BASE_URL");
        }

        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = Configuration.getZephyrBaseUri();
        }

        return baseUrl.trim();
    }

    /**
     * Token de API do Zephyr Scale (Zephyr Scale > API Access Tokens - é
     * diferente do token de API do Jira). Sem default: se não vier de fora,
     * o teste que depende dele deve falhar/pular explicitamente, nunca
     * seguir com um valor fake.
     */
    public static String getZephyrApiToken() {
        String token = System.getProperty("zephyr.apiToken");

        if (token == null || token.isBlank()) {
            token = System.getenv("ZEPHYR_API_TOKEN");
        }

        return token == null ? null : token.trim();
    }
}
