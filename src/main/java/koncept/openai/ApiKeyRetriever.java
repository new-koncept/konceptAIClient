package koncept.openai;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

public class ApiKeyRetriever {

    static {
        ApiKeyRetriever retriever = new ApiKeyRetriever();
        apiKey = retriever.retrieveApiKey();
    }

    private static final String SYSTEM_PROPERTY_KEY = "koncept.openai.api.key";
    private static final String ENV_VARIABLE_KEY = "OPENAI_API_KEY";
    private static final String CONFIG_FILE = "application.properties";

    private static String apiKey;

    private ApiKeyRetriever() {
        apiKey = retrieveApiKey();
    }

    public static String getApiKey() {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("API key is missing! Configure it using a system property, environment variable, or application.properties file.");
        }
        return apiKey;
    }

    private String retrieveApiKey() {
        final String systemPropertyKey = System.getProperty(SYSTEM_PROPERTY_KEY);
        if (systemPropertyKey != null && !systemPropertyKey.isEmpty()) {
            return systemPropertyKey;
        }

        final String envVariableKey = System.getenv(ENV_VARIABLE_KEY);
        if (envVariableKey != null && !envVariableKey.isEmpty()) {
            return envVariableKey;
        }

        Optional<String> configFileKey = readFromConfigFile();
        if (configFileKey.isPresent()) {
            return configFileKey.get();
        }
        throw new IllegalStateException("API key is not configured! Please set it via a system property, environment variable, or configuration file.");
    }

    private Optional<String> readFromConfigFile() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                return Optional.empty();
            }
            Properties properties = new Properties();
            properties.load(input);
            return Optional.ofNullable(properties.getProperty(SYSTEM_PROPERTY_KEY));
        } catch (IOException e) {
            throw new RuntimeException("Error reading configuration file: " + CONFIG_FILE, e);
        }
    }
}
