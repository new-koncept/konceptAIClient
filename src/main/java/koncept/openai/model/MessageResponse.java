package koncept.openai.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MessageResponse(
    String id,
    String object,
    @JsonProperty("created_at")
    long createdAt,
    @JsonProperty("thread_id")
    String threadId,
    String role,
    List<Content> content,
    @JsonProperty("file_ids")
    List<String> fileIds,
    @JsonProperty("assistant_id")
    String assistantId,
    @JsonProperty("run_id")
    String runId,
    Map<String, Object> metadata) {

    public record Content(
        String type,
        Text text) {

        public record Text(
            String value,
            List<Object> annotations) {
        }
    }
}