package koncept.openai.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RequiredAction(
    String type,
    @JsonProperty("submit_tool_outputs")
    SubmitToolOutputs submitToolOutputs) {
}
