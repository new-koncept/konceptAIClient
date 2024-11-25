package koncept.openai.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AssistantRequest(
    String name,
    String model,
    String instructions,
    @JsonProperty("response_format")
    AssistantsApiResponseFormatOption responseFormat
    ) {
}


