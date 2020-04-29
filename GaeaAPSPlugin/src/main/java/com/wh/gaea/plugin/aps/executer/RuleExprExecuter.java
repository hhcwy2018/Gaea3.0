package com.wh.gaea.plugin.aps.executer;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.wh.gaea.plugin.aps.interfaces.IRule;
import com.wh.gaea.plugin.aps.rule.RuleExprs;
import com.wh.tools.ScriptExecuter;

public class RuleExprExecuter {
    public static class ExprParam<T> {
        public String key;
        public T value;

        public ExprParam(String key, T value) {
            this.key = key;
            this.value = value;
        }
    }

    IRule rule;

    public static final String getMaterialParamName(String materialId, String attrName) {
        return "#{" + RuleExprParser.Material_Prex + "." + materialId + "." + attrName + "}";
    }

    public static final String getDeviceParamName(String materialId, String attrName) {
        return "#{" + RuleExprParser.Device_Prex + "." + materialId + "." + attrName + "}";
    }

    public static final String getMouldParamName(String materialId, String attrName) {
        return "#{" + RuleExprParser.Mould_Prex + "." + materialId + "." + attrName + "}";
    }

    public RuleExprExecuter(IRule rule) {
        this.rule = rule;
    }

    public String getExpr() {
        String expr = rule.getRuleExpr();
        if (expr != null)
            expr = expr.trim();

        if (expr == null || expr.isEmpty()) {
            RuleExprs ruleExprs = rule.getRuleExprs();
            if (ruleExprs == null || ruleExprs.size() == 0)
                return null;

            expr = new RuleExprParser(ruleExprs).parse();
        }

        return expr;
    }

    public RuleExprParser.ParamNameInfos findParams(){
        String expr = rule.getRuleExpr();
        if (expr == null || expr.trim().isEmpty())
            return new RuleExprParser(rule.getRuleExprs()).parseParameterName();
        else
            return RuleExprParser.parseParameterName(expr);
    }

    public Object execute(ExprParam<?>[] args) throws Exception {
        String expr = getExpr();

        Pattern pattern = Pattern.compile("[\\$,#]\\{(.+)\\}");
        Matcher matcher = pattern.matcher(expr);
        Map<String, Object> paramMap = new HashMap<>();
        if (args != null && args.length > 0) {
            for (ExprParam<?> exprParam : args) {
                paramMap.put(exprParam.key, exprParam.value);
            }
        }

        while (matcher.find()) {
            String key = matcher.group(1);
            Object v = paramMap.get(key);
            if (v != null)
                matcher.replaceAll(v.toString());
            else
                matcher.replaceAll("");
        }

        return executeJs(expr);
    }

    public Object executeJs(String js) throws Exception {
        return ScriptExecuter.execute(js);
    }
}
