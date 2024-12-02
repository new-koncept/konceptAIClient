package koncept.openai.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ToolCall(
    String id,
    String type,
    Function function
) {}
