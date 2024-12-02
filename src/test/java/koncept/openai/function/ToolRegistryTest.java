package koncept.openai.function;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class ToolRegistryTest {

    @ToolFunction(name = "static_tool")
    public static String staticTool(@NamedParam("paramName") String paramName) {
        return "expectedResult with " + paramName;
    }

    @ToolFunction(name = "instance_tool")
    public static String instanceTool(@NamedParam("paramName") String paramName) {
        return "expectedResult with " + paramName;
    }

    @ToolFunction(name = "instance_tool_with_list")
    public static String instanceToolWithList(@NamedParam("paramName") List<String> paramName) {
        return "expectedResult with " + paramName;
    }

    @ToolFunction(name = "instance_tool_with_list_enum")
    public static String instanceToolWithListEnum(@NamedParam("paramName") List<TestEnum> paramName) {
        return "expectedResult with " + paramName;
    }

    @Test
    public void testInvokeToolWithValidStaticMethod() {
        Map<String, Object> parametersMap = new HashMap<>();
        parametersMap.put("paramName", "expectedValue");
        String result = (String) ToolRegistry.invokeTool("static_tool", parametersMap);
        assertEquals("expectedResult with expectedValue", result);
    }

    @Test
    public void testInvokeToolWithValidInstanceMethod() {
        Map<String, Object> parametersMap = new HashMap<>();
        parametersMap.put("paramName", "expectedValue");
        String result = (String) ToolRegistry.invokeTool("instance_tool", parametersMap);
        assertEquals("expectedResult with expectedValue", result);
    }

    @Test
    public void testInvokeToolWithValidInstanceMethodWithList() {
        Map<String, Object> parametersMap = new HashMap<>();
        parametersMap.put("paramName", "[\"VALUE1\", \"VALUE2\"]");
        String result = (String) ToolRegistry.invokeTool("instance_tool_with_list", parametersMap);
        assertEquals("expectedResult with [VALUE1, VALUE2]", result);
    }

    @Test
    public void testInvokeToolWithValidInstanceMethodWithListEnum() {
        Map<String, Object> parametersMap = new HashMap<>();
        parametersMap.put("paramName", "[\"VALUE1\", \"VALUE2\"]");
        String result = (String) ToolRegistry.invokeTool("instance_tool_with_list", parametersMap);
        assertEquals("expectedResult with [VALUE1, VALUE2]", result);
    }

    @Test
    public void testInvokeToolMethodNotFound() {
        Map<String, Object> parametersMap = new HashMap<>();

        assertThrows(RuntimeException.class, () -> {
            ToolRegistry.invokeTool("nonExistentTool", parametersMap);
        });
    }

    public enum TestEnum {
        VALUE1, VALUE2, VALUE3
    }
}