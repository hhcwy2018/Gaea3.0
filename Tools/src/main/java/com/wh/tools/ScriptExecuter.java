package com.wh.tools;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public abstract class ScriptExecuter {
    public static class ExecuteParam {
        public String name;
        public Object value;
    }

    public static Object execute(String js, ExecuteParam... args) throws Exception {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("javascript");
        Object[] values = null;
        if (args != null && args.length > 0) {
            String names = null;
            values = new Object[args.length];
            int index = 0;
            for (ExecuteParam param : args) {
                if (names == null)
                    names = param.name;
                else
                    names += "," + param.name;

                values[index++] = param.value;
            }
            engine.eval("function callUserJS(" + names + "){" +
                    "return" + js +
                    "}");
        } else
            engine.eval("function callUserJS(){" +
                    "return" + js +
                    "}");
        Invocable in = (Invocable) engine;
        if (values == null)
            return in.invokeFunction("callUserJS");
        else
            return in.invokeFunction("callUserJS", values);

    }
}

