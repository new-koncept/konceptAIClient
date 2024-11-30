package koncept.openai.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SubmitToolOutputsRunRequest(
    @JsonProperty("tool_outputs")
    List<ToolOutput> toolOutputs,
    Boolean stream
) {
}
