package koncept.openai.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SubmitToolOutputs(
    @JsonProperty("tool_calls")
    List<ToolCall> toolCalls
) {
}
