package koncept.openai.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AssistantResponse(
    String id,
    String object,
    @JsonProperty("created_at")
    long createdAt,
    String name,
    String description,
    String model,
    String instructions,
    List<String> tools,
    @JsonProperty("file_ids")
    List<String> fileIds,
    Map<String, Object> metadata) {
}