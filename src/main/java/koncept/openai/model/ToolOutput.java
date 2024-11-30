package koncept.openai.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ToolOutput(
    @JsonProperty("tool_call_id")
    String toolCallId,
    String output
) {
}
