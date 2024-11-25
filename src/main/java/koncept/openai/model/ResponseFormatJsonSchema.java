package koncept.openai.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ResponseFormatJsonSchema(String name,
                                       boolean strict,
                                       Object schema) {
}
