package koncept.openai.function;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

public class ToolRegistry {

    private static final Map<String, Method> toolMethods = new HashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        initializeTools();
    }

    private static void initializeTools() {
        Reflections reflections = new Reflections(
            new ConfigurationBuilder()
                .forPackages("")
                .addScanners(Scanners.MethodsAnnotated)
        );

        Set<Method> annotatedMethods = reflections.getMethodsAnnotatedWith(ToolFunction.class);
        annotatedMethods.forEach(method -> {
            ToolFunction annotation = method.getAnnotation(ToolFunction.class);
            toolMethods.put(annotation.name(), method);
        });
        System.out.println(annotatedMethods.size() + " tools registered");
    }

    public static Method getTool(String name) {
        return toolMethods.get(name);
    }

    public static Object invokeTool(String toolName, Map<String, Object> parametersMap) {
        Method method = getTool(toolName);
        if (method == null) {
            throw new RuntimeException("Tool not found: " + toolName);
        }
        try {
            Object targetInstance = null;
            if (!java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
                targetInstance = method.getDeclaringClass().getDeclaredConstructor().newInstance();
            }

            int parameterCount = method.getParameterCount();
            if (parameterCount == 0) {
                return method.invoke(targetInstance);
            }

            Object[] args = Arrays.stream(method.getParameters())
                .map(parameter -> {
                    String paramName = parameter.getAnnotation(NamedParam.class).value();
                    Object paramValue = parametersMap.get(paramName);
                    if (paramValue == null) {
                        throw new IllegalArgumentException("Missing required parameter: " + paramName);
                    }
                    if (parameter.getType().isInstance(paramValue)) {
                        return paramValue;
                    }
                    return convertArgument(paramValue.toString(), parameter.getType());
                })
                .toArray();

            return method.invoke(targetInstance, args);

        } catch (Exception e) {
            throw new RuntimeException("Error invoking tool: " + toolName + ", Parameters: " + parametersMap, e);
        }
    }


    private static Object convertArgument(String value, Class<?> targetType) {
        if (value == null) {
            if (targetType.isPrimitive()) {
                throw new IllegalArgumentException("Cannot convert null to primitive type: " + targetType.getName());
            }
            return null;
        }
        try {
            if (targetType == String.class) {
                return value;
            } else if (targetType == int.class || targetType == Integer.class) {
                return Integer.parseInt(value);
            } else if (targetType == double.class || targetType == Double.class) {
                return Double.parseDouble(value);
            } else if (targetType == boolean.class || targetType == Boolean.class) {
                return Boolean.parseBoolean(value);
            } else if (targetType.isEnum()) {
                return Enum.valueOf((Class<Enum>) targetType, value);
            } else if (Collection.class.isAssignableFrom(targetType)) {
                return objectMapper.readValue(value, objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
            } else {
                return objectMapper.readValue(value, targetType);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert value: \"" + value + "\" to type: " + targetType.getName(), e);
        }
    }
}
