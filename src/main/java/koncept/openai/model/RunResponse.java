package koncept.openai.model;


import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RunResponse(
    String id,
    String object,
    @JsonProperty("created_at")
    long createdAt,
    @JsonProperty("assistant_id")
    String assistantId,
    @JsonProperty("thread_id")
    String threadId,
    String status,
    @JsonProperty("started_at")
    Long startedAt,
    @JsonProperty("expires_at")
    Long expiresAt,
    @JsonProperty("cancelled_at")
    Long cancelledAt,
    @JsonProperty("failed_at")
    Long failedAt,
    @JsonProperty("completed_at")
    Long completedAt,
    @JsonProperty("required_action")
    RequiredAction requiredAction,
    @JsonProperty("last_error")
    String lastError,
    String model,
    String instructions,
    List<Object> tools,
    @JsonProperty("file_ids")
    List<String> fileIds,
    Map<String, Object> metadata) {
}
