package koncept.openai.model.deserializer;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MapStringDeserializer extends JsonDeserializer<Map<String, Object>> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException, JsonProcessingException {
        String jsonString = p.getValueAsString();
        return objectMapper.readValue(jsonString, Map.class);
    }
}
