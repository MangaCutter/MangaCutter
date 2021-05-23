package net.macu.util;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Scriptable;

public class JSEngine {
    public static Object evaluate(String code) throws EvaluatorException {
        Context context = Context.enter();
        Scriptable s = context.initSafeStandardObjects();
        try {
            return context.evaluateString(s, code, "<cmd>", 1, null);
        } finally {
            Context.exit();
        }
    }
}
