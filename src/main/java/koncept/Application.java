package koncept;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import koncept.openai.function.NamedParam;
import koncept.openai.function.ToolFunction;
import koncept.openai.function.ToolRegistry;
import koncept.openai.model.Message;
import koncept.openai.model.ThreadResponse;

public class Application {

    private static final String CHESS_ASSISTANT_ID = "asst_aNtjbsWo4GhZAWFyEfstY0oS";

    public static void main(String[] args) throws InvocationTargetException, IllegalAccessException {
        KonceptAIClient client = KonceptAIClient.getInstance(true);

        ThreadResponse thread = client.createThread();
        Object s = client.sendAndRunMessage("What would be the best move in this position? 8/5k2/3p4/1p1Pp2p/pP2Pp1P/P4P1K/8/8 b - - 99 50", thread.id(), CHESS_ASSISTANT_ID, Object.class);
//        System.out.println(s);

    }

    @ToolFunction(name = "evaluate_chess_position")
    public String evaluateChessPosition(@NamedParam("position") String position, @NamedParam("color") String color) {
        return "Although it looks lost, white is completely safe";
    }


    @ToolFunction(name = "testing_test")
    public void test(@NamedParam("parameter") String parameter) {
        System.out.println( "test from the method + " + parameter);
    }

    @ToolFunction(name = "parametrized")
    public void parametrized(@NamedParam("message") Message message) {
        System.out.println( "test from the paaara + " + message.role());
    }
}
