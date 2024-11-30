package koncept.openai;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import koncept.exception.OpenAIClientIntegrationException;
import koncept.openai.model.AssistantRequest;
import koncept.openai.model.AssistantResponse;
import koncept.openai.model.Message;
import koncept.openai.model.MessageResponse;
import koncept.openai.model.MessagesListResponse;
import koncept.openai.model.RunRequest;
import koncept.openai.model.RunResponse;
import koncept.openai.model.SubmitToolOutputsRunRequest;
import koncept.openai.model.SubmitToolOutputsRunResponse;
import koncept.openai.model.ThreadResponse;

/**
 * KonceptAIClient is a singleton class that provides methods to interact with the OpenAI API.
 * It offers functionalities needed for OpenAI assistants - create assistants, create threads, send messages, and run message operations.
 */
public class OpenAIAPIClient {

    private static volatile OpenAIAPIClient instance;
    private final HttpClient httpClient;
    private final String apiKey;
    private final boolean traceHttpRequests;

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(OpenAIAPIClient.class.getName());
    private static final String THREADS_URL = "https://api.openai.com/v1/threads";
    private static final String ASSISTANTS_URL = "https://api.openai.com/v1/assistants";

    private OpenAIAPIClient(final boolean traceHttpRequests) {
        this.apiKey = ApiKeyRetriever.getApiKey();
        this.httpClient = HttpClient.newHttpClient();
        this.traceHttpRequests = traceHttpRequests;
    }

    /**
     * Returns the singleton instance of the KonceptAIClient.
     *
     * @param traceHttpRequests Indicates whether HTTP requests should be logged for tracing raw HTTP communication with openAI.
     * @return The singleton instance of the KonceptAIClient.
     */
    public static OpenAIAPIClient getInstance(final boolean traceHttpRequests) {
        if (instance == null) {
            synchronized (OpenAIAPIClient.class) {
                if (instance == null) {
                    instance = new OpenAIAPIClient(traceHttpRequests);
                }
            }
        }
        return instance;
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Creates an assistant by sending a POST request to the specified assistants URL.
     *
     * @param assistantRequest The {@link AssistantRequest} object containing the details of the assistant to be created.
     * @return An {@link AssistantResponse} object containing the details of the created assistant.
     */
    public AssistantResponse createAssistant(final AssistantRequest assistantRequest) {
        try {
            AssistantResponse assistantResponse = sendPostRequest(ASSISTANTS_URL, assistantRequest, AssistantResponse.class);
            LOGGER.info(() -> "Assistant created with id: " + assistantResponse.id());
            return assistantResponse;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new thread by sending a POST request to the specified threads URL.
     * Logs the creation and returns the response containing thread details.
     *
     * @return A {@link ThreadResponse} object containing the details of the created thread.
     * @throws RuntimeException if an error occurs while sending the request or processing the response.
     */
    public ThreadResponse createThread() {
        try {
            ThreadResponse threadResponse = sendPostRequest(THREADS_URL, null, ThreadResponse.class);
            LOGGER.info(() -> "Thread created with id: " + threadResponse.id());
            return threadResponse;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sends a message to a specified thread.
     *
     * @param message  The {@link Message} object containing the details of the message to be sent.
     * @param threadId The ID of the thread to which the message will be sent.
     * @return A {@link MessageResponse} object containing the details of the message.
     * @throws RuntimeException if an error occurs while sending the request or processing the response.
     */
    public MessageResponse sendMessage(final Message message, final String threadId) {
        String url = THREADS_URL + "/" + threadId + "/messages";
        try {
            MessageResponse messageResponse = sendPostRequest(url, message, MessageResponse.class);
            LOGGER.info(() -> "Message sent with id: " + messageResponse.id());
            return messageResponse;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves a list of messages from a specified thread.
     *
     * @param threadId The ID of the thread from which messages are to be retrieved.
     * @return A {@link MessagesListResponse} object containing the list of messages.
     * @throws RuntimeException if an error occurs while sending the request or processing the response.
     */
    public MessagesListResponse getMessages(final String threadId) {
        String url = THREADS_URL + "/" + threadId + "/messages";
        try {
            MessagesListResponse messagesListResponse = sendGetRequest(url, MessagesListResponse.class);
            LOGGER.info(() -> "Messages retrieved for thread with id: " + threadId);
            return messagesListResponse;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Executes a run message operation by sending a POST request to the run endpoint associated with the specified thread ID.
     *
     * @param runRequest The {@link RunRequest} object containing the details of the run to be executed.
     * @param threadId   The ID of the thread to which the run belongs.
     * @return A {@link RunResponse} object containing the details of the executed run.
     * @throws RuntimeException if an error occurs while sending the request or processing the response.
     */
    public RunResponse runMessage(final RunRequest runRequest, final String threadId) {
        String url = THREADS_URL + "/" + threadId + "/runs";
        try {
            RunResponse runResponse = sendPostRequest(url, runRequest, RunResponse.class);
            LOGGER.info(() -> "Run executed for thread with id: " + threadId);
            return runResponse;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Retrieves the details of a specific run based on the given thread ID and run ID.
     *
     * @param threadId The ID of the thread to which the run belongs.
     * @param runId    The ID of the run to be retrieved.
     * @return A {@link RunResponse} object containing the details of the requested run.
     * @throws RuntimeException if an error occurs while sending the request or processing the response.
     */
    public RunResponse getRun(final String threadId, final String runId) {
        String url = THREADS_URL + "/" + threadId + "/runs/" + runId;
        try {
            RunResponse runResponse = sendGetRequest(url, RunResponse.class);
            LOGGER.info(() -> "Run retrieved for thread with id: " + threadId);
            return runResponse;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Submits tool outputs for a specific thread and run.
     *
     * @param submitToolOutputsRunRequest The {@link SubmitToolOutputsRunRequest} object containing
     *                                    the tool outputs to be submitted.
     * @param threadId                    The ID of the thread for which tool outputs are to be submitted.
     * @param runId                       The ID of the run associated with the tool outputs.
     * @return A {@link SubmitToolOutputsRunResponse} object containing the response of the submitted tool outputs.
     * @throws RuntimeException if an error occurs while sending the request or processing the response.
     */
    public SubmitToolOutputsRunResponse submitToolOutputs(final SubmitToolOutputsRunRequest submitToolOutputsRunRequest,
                                                          final String threadId,
                                                          final String runId) {
        String url = THREADS_URL + "/" + threadId + "/runs/" + runId + "/submit_tool_outputs";
        try {
            SubmitToolOutputsRunResponse submitToolOutputsRunResponse = sendPostRequest(url,
                submitToolOutputsRunRequest,
                SubmitToolOutputsRunResponse.class
            );
            LOGGER.info(() -> "Tool outputs submitted for thread with id: " + threadId + "with id: " + submitToolOutputsRunResponse.id());
            return submitToolOutputsRunResponse;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private <T, R> R sendPostRequest(final String url, final T requestBody, final Class<R> responseClass) throws IOException, InterruptedException {
        String requestBodyString = requestBody == null ? "" : objectMapper.writeValueAsString(requestBody);
        HttpRequest httpRequest = createRequest(url, requestBodyString, HttpMethod.POST);
        return sendRequest(httpRequest, responseClass);
    }

    private <R> R sendGetRequest(final String url, final Class<R> responseClass) throws IOException, InterruptedException {
        HttpRequest httpRequest = createRequest(url, "", HttpMethod.GET);
        return sendRequest(httpRequest, responseClass);
    }

    private HttpRequest createRequest(final String url, final String requestBody, final HttpMethod method) {
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(requestBody);
        return HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + apiKey)
            .header("OpenAI-Beta", "assistants=v2")
            .header("Content-Type", "application/json")
            .method(method.name(), bodyPublisher)
            .build();
    }

    private <R> R sendRequest(final HttpRequest httpRequest, Class<R> responseClass)
        throws IOException, InterruptedException {
        if (traceHttpRequests) {
            logRequest(httpRequest);
        }
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        if (traceHttpRequests) {
            logResponse(response);
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new OpenAIClientIntegrationException(
                "HTTP error: " + response.statusCode(),
                response.statusCode(),
                response.body()
            );
        }
        return objectMapper.readValue(response.body(), responseClass);
    }

    private void logRequest(HttpRequest httpRequest) {
        LOGGER.info(() -> "HTTP Request:\n" +
            "URI: " + httpRequest.uri() + "\n" +
            "Method: " + httpRequest.method() + "\n" +
            "Headers: " + httpRequest.headers() + "\n" +
            "Body: " + (httpRequest.bodyPublisher().isPresent() ? "Body content present" : "No body"));
    }

    private void logResponse(final HttpResponse<String> response) {
        LOGGER.info(() -> "HTTP Response:\n" +
            "Status Code: " + response.statusCode() + "\n" +
            "Headers: " + response.headers() + "\n" +
            "Body: " + response.body());
    }


}
