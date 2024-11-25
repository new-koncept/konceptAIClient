package koncept.jsonschema;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.Option;
import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfig;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaVersion;

public class SchemaTransformer {

    private static final SchemaGenerator schemaGenerator;

    static {
        SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON);
        SchemaGeneratorConfigBuilder without = configBuilder
            .with(
                Option.EXTRA_OPEN_API_FORMAT_VALUES,
                Option.FORBIDDEN_ADDITIONAL_PROPERTIES_BY_DEFAULT)
            .without(
                Option.FLATTENED_ENUMS_FROM_TOSTRING,
                Option.SCHEMA_VERSION_INDICATOR);
        without.forFields().withRequiredCheck(field -> true);
        SchemaGeneratorConfig build = without.build();
        schemaGenerator = new SchemaGenerator(build);
    }

    public static <T> ObjectNode toJSONSchema(final Class<T> mappedClass) {
        return schemaGenerator.generateSchema(mappedClass);
    }

}
