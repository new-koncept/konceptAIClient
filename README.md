# Koncept OpenAI Client

A lightweight and user-friendly Java library to interact with OpenAI's Assistant features. This library simplifies working with OpenAI's REST API, offering both low-level API access (OpenAIAPIClient) and high-level convenience methods (KonceptAIClient).

## Features

* Integration with OpenAI's assistants REST API.
* High-level methods for common workflows.
* Support for both synchronous and asynchronous operations.
* Flexible configuration with API key retrieval.

## Prerequisites

* Java 17 or later.
* An OpenAI API key.

## Getting Started

### Installation

Add the library as a dependency in your project. If you’re using Maven:

```
<dependency>
    <groupId>io.koncept</groupId>
    <artifactId>openai-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

### API Key Configuration

The library requires an OpenAI API key for authentication. The API key can be provided through one of the following methods:

1) Environment Variable: Set the OPENAI_API_KEY environment variable:

  `export OPENAI_API_KEY=your-api-key-here`


2) System Property: Pass the API key as a JVM system property:

  `java -Dopenai.api.key=your-api-key-here -jar your-app.jar`

3) application.properties File: Place an application.properties file in your resources directory:

  `openai.api.key=your-api-key-here`

The library will automatically retrieve the API key using the ApiKeyRetriever utility.

## Usage Examples

### High-Level Client (KonceptAIClient)

#### Send and Run a Message

```
import io.koncept.openai.KonceptAIClient;

public class Example {
    public static void main(String[] args) {
        KonceptAIClient client = KonceptAIClient.getInstance(true); // Enable HTTP tracing
        String threadId = client.createThread();
        String assistantId = "your-assistant-id";

        // Synchronous usage
        MyResponse response = client.sendAndRunMessage(
            "What is the weather today?", 
            threadId, 
            assistantId, 
            MyResponse.class
        );
        System.out.println("Response: " + response);

        // Asynchronous usage
        client.sendAndRunMessageAsync(
            "Tell me a joke", 
            threadId, 
            assistantId, 
            MyResponse.class
        ).thenAccept(res -> System.out.println("Async Response: " + res));
    }
}
```

### Low-Level Client (OpenAIAPIClient)

#### Create an Assistant

```
import io.koncept.openai.OpenAIAPIClient;
import io.koncept.openai.models.AssistantRequest;
import io.koncept.openai.models.AssistantResponse;

public class Example {
    public static void main(String[] args) {
        OpenAIAPIClient apiClient = OpenAIAPIClient.getInstance(true);
        AssistantRequest request = new AssistantRequest("Youtube content preparation",OpenAIModel.GPT_4O.getModelId(),"Make great outlines for youtube videos", null);
        AssistantResponse response = apiClient.createAssistant(request);
        System.out.println("Assistant created with id: " + response);
    }
}
```

## Error Handling

The library provides custom exception **OpenAIClientIntegrationException** to help with error handling

```
try {
    // Your API calls here
} catch (OpenAIClientIntegrationException e) {
    System.err.println("API Error: " + e.getMessage());
} catch (OpenAIClientException e) {
    System.err.println("Client Error: " + e.getMessage());
}
```

## Contributing

Create a feature branch.
Submit a pull request with a detailed explanation.

## License

This project is licensed under the MIT License. See the LICENSE file for details.