package config;

import java.io.InputStream;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

public class Configuration {
    private static final Map<String, Object> config;

    static {
        Yaml yaml = new Yaml();

        InputStream inputStream = Configuration.class
                .getClassLoader()
                .getResourceAsStream("data.yaml");

        if (inputStream == null) {
            throw new RuntimeException("Arquivo data.yaml não encontrado no classpath.");
        }

        config = yaml.load(inputStream);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getSection(String section) {
        return (Map<String, Object>) config.get(section);
    }

    public static String getBaseUri() {
        return (String) getSection("api").get("baseUri");
    }

    public static int getReadinessTimeoutSeconds() {
        return (Integer) getSection("api").get("readinessTimeoutSeconds");
    }

    public static String getJiraTaskKeyValido() {
        return (String) getSection("jira").get("taskKeyValido");
    }

    public static String getJiraTaskKeyInvalido() {
        return (String) getSection("jira").get("taskKeyInvalido");
    }
}
