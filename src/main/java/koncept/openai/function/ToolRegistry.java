package koncept.openai.function;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

public class ToolRegistry {

    private static final Map<String, Method> toolMethods = new HashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Map<String, Object> toolInstances = new HashMap<>();

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

    public static <T> T invokeTool(String toolName, Object... args) {
        Method method = getTool(toolName);
        if (method == null) {
            throw new RuntimeException("Tool not found: " + toolName);
        }
        try {
            Object targetInstance = null;
            if (!java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
                targetInstance = method.getDeclaringClass().getDeclaredConstructor().newInstance();
            }
            Object invoke = method.invoke(targetInstance, args);
            return (T) invoke;

        } catch (Exception e) {
            throw new RuntimeException("Error invoking tool: " + toolName, e);
        }
    }

    public static <T> T invokeTool(String toolName, Map<String, String> parametersMap) {
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
                return (T) method.invoke(targetInstance);
            }

            // Resolve and convert arguments
            Object[] array = Arrays.stream(method.getParameters())
                .map(parameter -> {
                    String paramName = parameter.getAnnotation(NamedParam.class).value();

                    String paramValue = parametersMap.get(paramName);
                    if (paramValue == null) {
                        throw new IllegalArgumentException("Missing required parameter: " + paramName);
                    }
                    return convertArgument(paramValue, parameter.getType());
                })
                .toArray();

            return (T) method.invoke(targetInstance, array);

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
            } else if (targetType == long.class || targetType == Long.class) {
                return Long.parseLong(value);
            } else if (targetType == float.class || targetType == Float.class) {
                return Float.parseFloat(value);
            } else if (targetType == short.class || targetType == Short.class) {
                return Short.parseShort(value);
            } else if (targetType == char.class || targetType == Character.class) {
                if (value.length() != 1) {
                    throw new IllegalArgumentException("Cannot convert string to char: \"" + value + "\"");
                }
                return value.charAt(0);
            } else if (targetType.isEnum()) {
                return Enum.valueOf((Class<Enum>) targetType, value);
            } else if (Collection.class.isAssignableFrom(targetType)) {
                JavaType javaType = objectMapper.getTypeFactory()
                    .constructCollectionType((Class<? extends Collection>) targetType, String.class);
                return objectMapper.readValue(value, javaType);
            } else {
                return objectMapper.readValue(value, targetType); // Fallback for custom objects
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert value: \"" + value + "\" to type: " + targetType.getName(), e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error during conversion for value: \"" + value + "\" to type: " + targetType.getName(), e);
        }
    }

}
