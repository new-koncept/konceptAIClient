package koncept.openai.function;

import java.util.List;

public class TestingTools {

    @ToolFunction(name = "instance_test")
    public String test(String input) {
        System.out.println("tadydydyda ");
        return  "OK_" + input;
    }

    @ToolFunction(name = "list_test")
    public List<String> listTest(String input) {
        System.out.println("list test ");
        return  List.of("OK_" + input);
    }
}
