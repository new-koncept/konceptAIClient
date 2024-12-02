package koncept;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import koncept.jsonschema.SchemaTransformer;
import koncept.openai.OpenAIAPIClient;
import koncept.openai.function.ToolRegistry;
import koncept.openai.model.AssistantRequest;
import koncept.openai.model.AssistantResponse;
import koncept.openai.model.AssistantsApiResponseFormatOption;
import koncept.openai.model.Message;
import koncept.openai.model.MessageResponse;
import koncept.openai.model.MessagesListResponse;
import koncept.openai.model.OpenAIModel;
import koncept.openai.model.RequiredAction;
import koncept.openai.model.ResponseFormatJsonSchema;
import koncept.openai.model.RunRequest;
import koncept.openai.model.RunResponse;
import koncept.openai.model.SubmitToolOutputsRunRequest;
import koncept.openai.model.ThreadResponse;
import koncept.openai.model.ToolCall;
import koncept.openai.model.ToolOutput;

public class KonceptAIClient {

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(KonceptAIClient.class.getName());

    private static volatile KonceptAIClient instance;
    private final OpenAIAPIClient openAIAPIClient;

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private KonceptAIClient(boolean traceHttpRequests) {
        this.openAIAPIClient = OpenAIAPIClient.getInstance(traceHttpRequests);
    }

    /**
     * Returns a singleton instance of the KonceptAIClient.
     * If the instance is not already created, it initializes a new instance with the specified
     * HTTP request tracing setting.
     *
     * @param traceHttpRequests Indicates whether HTTP requests should be traced.
     * @return The singleton instance of KonceptAIClient.
     */
    public static KonceptAIClient getInstance(boolean traceHttpRequests) {
        if (instance == null) {
            synchronized (KonceptAIClient.class) {
                if (instance == null) {
                    instance = new KonceptAIClient(traceHttpRequests);
                }
            }
        }
        return instance;
    }

    /**
     * Retrieves the raw OpenAI API client used for direct interactions with the OpenAI backend.
     *
     * @return The instance of OpenAIAPIClient associated with this client, enabling low-level API operations.
     */
    public OpenAIAPIClient getRawClient() {
        return this.openAIAPIClient;
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Creates an assistant with the specified response type.
     *
     * @param name                The name of the assistant to be created.
     * @param instructions        The instructions for the assistant.
     * @param model               The OpenAIModel to be used by the assistant.
     * @param responseFormatClass The class indicating the response format.
     * @param <T>                 The type of the response expected from the assistant.
     * @return The AssistantResponse object containing the details of the created assistant.
     * @throws RuntimeException if the model does not support JSON schema as a response format.
     */
    public <T> AssistantResponse createAssistantWithResponseType(final String name,
                                                                 final String instructions,
                                                                 final OpenAIModel model,
                                                                 final Class<T> responseFormatClass) {
        if (!model.supportJsonSchema()) {
            throw new RuntimeException("Model {} does not support json schema as a response format");
        }
        ObjectNode jsonSchema = SchemaTransformer.toJSONSchema(responseFormatClass);
        ResponseFormatJsonSchema responseFormatJsonSchema = new ResponseFormatJsonSchema(responseFormatClass.getSimpleName(), true, jsonSchema);
        AssistantsApiResponseFormatOption responseFormat = new AssistantsApiResponseFormatOption("json_schema", responseFormatJsonSchema);
        AssistantRequest assistantRequest = new AssistantRequest(name, model.getModelId(), instructions, responseFormat);
        AssistantResponse assistantResponse = openAIAPIClient.createAssistant(assistantRequest);
        LOGGER.info(() -> "Assistant created with id: " + assistantResponse.id());
        return assistantResponse;
    }

    /**
     * Sends a message to the specified thread, initiates a run for the given assistant,
     * waits for the run to complete, and returns the assistant's response parsed into the specified class.
     *
     * @param content       The content of the message to be sent.
     * @param threadId      The ID of the thread to which the message is to be sent.
     * @param assistantId   The ID of the assistant to be run.
     * @param responseClass The class type to which the response from the assistant should be parsed.
     * @return The response from the assistant parsed into an instance of the specified class.
     * @throws RuntimeException if an error occurs while sending the message, initiating the run, waiting for completion, or parsing the response.
     */
    public <T> T sendAndRunMessage(final String content,
                                   final String threadId,
                                   final String assistantId,
                                   final Class<T> responseClass) {
        Message message = new Message("user", content);
        MessageResponse messageResponse = openAIAPIClient.sendMessage(message, threadId);
        LOGGER.info(() -> "Message sent with id: " + messageResponse.id());
        try {
            RunRequest runRequest = new RunRequest(assistantId);
            RunResponse runResponseDTO = openAIAPIClient.runMessage(runRequest, threadId);

            String runId = runResponseDTO.id();
            waitUntilRunIsFinished(threadId, runId, 10);
            MessagesListResponse messagesListResponseDTO = openAIAPIClient.getMessages(threadId);
            return objectMapper.readValue(
                messagesListResponseDTO.data().stream().filter(d -> "assistant".equals(d.role())).findFirst().get().content().get(0).text().value(),
                responseClass);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in sendAndRunMessage", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Sends a message to a specified thread, initiates a run for a given assistant,
     * waits for the run to complete, and returns the assistant's response parsed into the specified class asynchronously.
     *
     * @param content       The content of the message to be sent.
     * @param threadId      The ID of the thread to which the message is to be sent.
     * @param assistantId   The ID of the assistant to be run.
     * @param responseClass The class type to which the response from the assistant should be parsed.
     * @param <T>           The type of the response.
     * @return A CompletableFuture holding the response from the assistant parsed into an instance of the specified class.
     */
    public <T> CompletableFuture<T> sendAndRunMessageAsync(
        String content,
        String threadId,
        String assistantId,
        Class<T> responseClass) {
        Message message = new Message("user", content);
        return CompletableFuture.runAsync(() -> openAIAPIClient.sendMessage(message, threadId))
            .thenComposeAsync(ignored -> runMessageAsync(threadId, assistantId))
            .thenComposeAsync(runResponse -> {
                String runId = runResponse.id();
                return waitUntilRunIsFinishedAsync(threadId, runId)
                    .thenApplyAsync(ignored2 -> runId);
            })
            .thenComposeAsync(ignored -> getMessagesAsync(threadId))
            .thenApplyAsync(messagesListResponseDTO -> {
                try {
                    String assistantMessage = messagesListResponseDTO.data()
                        .stream()
                        .filter(d -> "assistant".equals(d.role()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("No assistant message found"))
                        .content()
                        .get(0)
                        .text()
                        .value();
                    return objectMapper.readValue(assistantMessage, responseClass);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error parsing assistant response", e);
                    throw new RuntimeException(e);
                }
            })
            .exceptionally(e -> {
                LOGGER.log(Level.SEVERE, "Error in sendAndRunMessageAsync", e);
                throw new RuntimeException(e);
            });
    }

    /**
     * Creates a new thread by delegating the call to the OpenAI API client.
     *
     * @return A ThreadResponse object containing the details of the created thread.
     */
    public ThreadResponse createThread() {
        return openAIAPIClient.createThread();
    }

    private CompletableFuture<RunResponse> runMessageAsync(final String threadId, final String assistantId) {
        RunRequest runRequest = new RunRequest(assistantId);
        return CompletableFuture.supplyAsync(() -> openAIAPIClient.runMessage(runRequest, assistantId));
    }

    private CompletableFuture<Void> waitUntilRunIsFinishedAsync(final String threadId, final String runId) {
        return CompletableFuture.runAsync(() -> {
            try {
                waitUntilRunIsFinished(threadId, runId, 10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private CompletableFuture<MessagesListResponse> getMessagesAsync(final String threadId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return openAIAPIClient.getMessages(threadId);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error fetching messages asynchronously", e);
                throw new RuntimeException(e);
            }
        });
    }

    private boolean isRunDone(final String threadId, final String runId) {
        RunResponse runResponseDTO;
        try {
            runResponseDTO = openAIAPIClient.getRun(threadId, runId);
            String runStatus = runResponseDTO.status();
            if (runResponseDTO.requiredAction() != null) {
                processRequiredActions(threadId, runId, runResponseDTO.requiredAction());
            }
            LOGGER.info(() -> "Current status of run " + runId + " at thread " + threadId + " is: " + runStatus);
            System.out.println("Status of your run is currently " + runStatus);
            return isRunStateFinal(runStatus);
        } catch (Exception e) {
            LOGGER.severe(() -> "Failed to get run info, retrying..." + e);
            return false;
        }
    }

    private void processRequiredActions(String threadId, String runId, RequiredAction requiredAction) {
        Map<String, CompletableFuture<Object>> futuresMap = requiredAction
            .submitToolOutputs()
            .toolCalls()
            .stream()
            .collect(Collectors.toMap(
                ToolCall::id,
                call -> CompletableFuture.supplyAsync(() -> invokeToolFunction(call))
            ));
        CompletableFuture.allOf(
            futuresMap.values().toArray(new CompletableFuture[0])
        ).join();

        List<ToolOutput> toolOutputs = futuresMap.entrySet().stream().
            map(es -> new ToolOutput(es.getKey(), es.getValue().join().toString()))
            .collect(Collectors.toList());
        SubmitToolOutputsRunRequest submitToolOutputsRunResponse = new SubmitToolOutputsRunRequest(toolOutputs, false);
        openAIAPIClient.submitToolOutputs(submitToolOutputsRunResponse, threadId, runId);
    }

    private Object invokeToolFunction(ToolCall call) {
        return ToolRegistry.invokeTool(call.function().name(), call.function().arguments());
    }

    private boolean isRunStateFinal(final String runStatus) {
        return List.of("cancelled", "failed", "completed", "expired").contains(runStatus);
    }


    private void waitUntilRunIsFinished(final String threadId, final String runId, final int maxRetries) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final AtomicInteger attemptCounter = new AtomicInteger(0);
        Runnable checkTask = () -> {
            if (isRunDone(threadId, runId) || attemptCounter.incrementAndGet() >= maxRetries) {
                latch.countDown();
                scheduler.shutdown();
            }
        };
        scheduler.scheduleAtFixedRate(checkTask, 0, 3, TimeUnit.SECONDS);
        latch.await();
    }
}
