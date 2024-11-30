package koncept.openai.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import koncept.openai.model.deserializer.MapStringDeserializer;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Function(
    String name,
    @JsonDeserialize(using = MapStringDeserializer.class)
    Map<String, String> arguments
) {
}
