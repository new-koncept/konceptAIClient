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
    <groupId>koncept</groupId>
    <artifactId>KonceptAIClient</artifactId>
    <version>0.1.0</version>
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

#### Tool invocation

The ToolFunction annotation enables dynamic function invocation based on assistant responses.
To use this feature, annotate your methods with @ToolFunction, specifying a unique name. For example:

```
@ToolFunction(name = "get_available_pets")
public Set<Pet> getAvailablePets(@NamedParam("types") List<PetType> types, @NamedParam("minimal_experience") RequiredExperience minimalExperience) {
    return availablePets.stream()
        .filter(typePredicate(types))
        .filter(minimalExperiencePredicate(minimalExperience))
        .collect(Collectors.toSet());
}

```
When an assistant response requests this tool, the library maps parameters dynamically (e.g., "types": ["DOG", "CAT"] and "minimal_experience": "MEDIUM")
and invokes the function. The output is then processed and returned to the assistant.
This seamless integration supports robust workflows for applications that rely on real-time assistant interactions.

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