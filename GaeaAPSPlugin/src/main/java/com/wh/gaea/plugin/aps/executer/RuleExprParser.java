package com.wh.gaea.plugin.aps.executer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;

import com.wh.gaea.plugin.aps.rule.RuleExpr;
import com.wh.gaea.plugin.aps.rule.RuleExprLogicType;
import com.wh.gaea.plugin.aps.rule.RuleExprOperation;
import com.wh.gaea.plugin.aps.rule.RuleExprs;

public class RuleExprParser {
    public static final String Device_Prex = "device";
    public static final String Mould_Prex = "mould";
    public static final String Material_Prex = "material";

    public static class ParamNameInfos {
        public Map<String, String> devices = new ConcurrentHashMap<>();
        public Map<String, String> moulds = new ConcurrentHashMap<>();
        public Map<String, String> materials = new ConcurrentHashMap<>();
        public Map<String, String> users = new ConcurrentHashMap<>();
    }

    RuleExprs ruleExprs;

    public RuleExprParser(RuleExprs ruleExprs) {
        this.ruleExprs = ruleExprs;
    }

    public String parseLogicType(RuleExprLogicType logicType) {
        if (logicType == null)
            return "";

        switch (logicType) {

            case ltAnd:
                return " && ";
            case ltOr:
                return " || ";
            case ltLeftPair:
                return "(";
            case ftRightPair:
                return ")";
            default:
                return "";
        }
    }

    public String parseOperation(RuleExprOperation operation) {
        if (operation == null)
            return null;

        switch (operation) {

            case foEqual:
                return "#{key} == #{value}";
            case foUnequal:
                return "#{key} != #{value}";
            case foIn:
                return "#{key} in #{value}";
            case foNotIn:
                return "!(#{key} in #{value})";
            case foGreate:
                return "#{key} > #{value}";
            case foLess:
                return "#{key} < #{value}";
            case foGreateEqual:
                return "#{key} >= #{value}";
            case foLessEqual:
                return "#{key} <= #{value}";
            default:
                return "";
        }
    }

    public String parseLeftValueMapper(RuleExpr expr, String operation) {
        String result = "#{";
        switch (expr.type) {

            case ftDevice:
                result = Device_Prex;
                break;
            case ftModule:
                result = Mould_Prex;
                break;
            case ftMaterial:
                result = Material_Prex;
                break;
        }

        result += "." + expr.item.id;
        result += "." + expr.attr.id;
        result += "}";

        return operation.replace("#{key}", result);
    }

    int varIndex = 0;

    public String parseRightValueMapper(RuleExpr expr, String operation) {
        if (expr.value == null || (expr.value instanceof String && ((String) expr.value).trim().isEmpty())) {
            return operation.replace("#{value}", "");
        }

        String result = expr.value == null ? "" : expr.value.toString();
        String defs = "";
        try {
            if (result != null && !result.isEmpty()) {
                JSONArray data = new JSONArray(result);
                String def = "a" + (varIndex++);
                defs = "var " + def + ";";
                int index = 0;
                for (Object v : data) {
                    defs += def + "[" + (index++) + "]=" + v.toString() + ";";
                }
                return defs + operation.replace("#{value}", def) + ";";
            } else
                return operation.replace("#{value}", result) + ";";
        } catch (Exception e) {
            return operation.replace("#{value}", result) + ";";
        }
    }

    protected static void findParams(String prex, String expr, Map<String, String> paramMap) {
        Pattern pattern = Pattern.compile("(" + prex + "\\{(.+)\\})");
        Matcher matcher = pattern.matcher(expr);

        while (matcher.find()) {
            String v = matcher.group(1);
            String k = matcher.group(2);
            paramMap.put(k, v);
        }
    }

    public ParamNameInfos parseParameterName() {
        return parseParameterName(parse());
    }

    public static ParamNameInfos parseParameterName(String expr) {
        ParamNameInfos infos = new ParamNameInfos();

        Map<String, String> paramMap = new HashMap<>();
        findParams("#", expr, paramMap);
        for (Map.Entry<String, String> entry: paramMap.entrySet()) {
            int index = entry.getKey().indexOf(".");
            String type = entry.getKey().substring(0, index);
            switch (type){
                case Device_Prex:
                    infos.devices.put(entry.getKey(), entry.getValue());
                    break;
                case Mould_Prex:
                    infos.moulds.put(entry.getKey(), entry.getValue());
                    break;
                case Material_Prex:
                    infos.materials.put(entry.getKey(), entry.getValue());
                    break;
            }
        }
        findParams("\\$", expr, infos.users);

        return infos;
    }

    public String parse(RuleExpr expr) {
        String result = parseLogicType(expr.leftLogic);
        String operation = parseOperation(expr.operation);
        if (operation == null || operation.isEmpty()) {
            return result + parseLogicType(expr.rightLogic);
        } else {
            operation = parseLeftValueMapper(expr, operation);
            operation = parseRightValueMapper(expr, operation);
            result += operation + parseLogicType(expr.rightLogic);
            return result;
        }
    }

    public String parse() {
        String result = "";
        for (RuleExpr expr : ruleExprs) {
            String exprStr = parse(expr);
            result += exprStr;
        }

        return result;
    }
}
