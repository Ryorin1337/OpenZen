package shit.zen.utils.misc;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import shit.zen.ClientBase;
import shit.zen.utils.animation.Timer;

public class CallerTracker
extends ClientBase {
    private static final Timer timer;
    public static final List<String> callers;
    private static final String STACK_TRACE_LABEL;

    public static void track() {
        if (timer.hasPassed(1000L)) {
            timer.reset();
            try {
                StackTraceElement[] stackTraceElementArray = new RuntimeException(STACK_TRACE_LABEL).getStackTrace();
                StackTraceElement stackTraceElement = stackTraceElementArray[3];
                String string = stackTraceElement.getClassName();
                callers.add(string);
            } catch (Exception exception) {
                logger.catching(exception);
            }
        }
    }

    static {
        STACK_TRACE_LABEL = "Stack Trace";
        timer = new Timer();
        callers = new CopyOnWriteArrayList<>();
    }
}