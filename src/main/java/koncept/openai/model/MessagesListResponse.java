package koncept.openai.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MessagesListResponse(
    String object,
    List<MessageResponse> data,
    @JsonProperty("first_id")
    String firstId,
    @JsonProperty("last_id")
    String lastId,
    @JsonProperty("has_more")
    boolean hasMore) {
}
