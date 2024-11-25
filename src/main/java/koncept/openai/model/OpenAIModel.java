package koncept.openai.model;

public enum OpenAIModel {
    GPT_4O_MINI("gpt-4o-mini", true),
    GPT_4O("gpt-4o", false),
    GPT_4_TURBO("gpt-4-turbo", false),
    GPT_4("gpt-4", false),
    GPT_3_5_TURBO("gpt-3.5-turbo", false),
    GPT_4O_MINI_2024_07_18("gpt-4o-mini-2024-07-18", true),
    GPT_4O_2024_08_06("gpt-4o-2024-08-06", true),
    GPT_4O_2024_05_13("gpt-4o-2024-05-13", false),
    GPT_4_TURBO_PREVIEW("gpt-4-turbo-preview", false),
    GPT_4_TURBO_2024_04_09("gpt-4-turbo-2024-04-09", false),
    GPT_4_1106_PREVIEW("gpt-4-1106-preview", false),
    GPT_4_0613("gpt-4-0613", false),
    GPT_4_0125_PREVIEW("gpt-4-0125-preview", false),
    GPT_3_5_TURBO_16K("gpt-3.5-turbo-16k", false),
    GPT_3_5_TURBO_1106("gpt-3.5-turbo-1106", false),
    GPT_3_5_TURBO_0125("gpt-3.5-turbo-0125", false);

    private final String modelId;
    private final boolean supportJsonSchema;

    OpenAIModel(final String modelId, final boolean supportJsonSchema) {
        this.modelId = modelId;
        this.supportJsonSchema = supportJsonSchema;
    }

    public String getModelId() {
        return modelId;
    }

    public boolean supportJsonSchema() {
        return supportJsonSchema;
    }

}
