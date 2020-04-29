package com.wh.gaea.plugin.aps.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.wh.gaea.plugin.aps.executer.RuleExprExecuter;
import com.wh.gaea.plugin.aps.executer.RuleExprParser;
import com.wh.gaea.plugin.aps.interfaces.IRule;
import com.wh.gaea.plugin.aps.rule.connector.RuleConnector;
import com.wh.gaea.plugin.aps.rule.connector.RuleTarget;

public class Rule implements IRule {

    public Class<? extends RuleConnector> connectorType = null;

    public RuleType type = RuleType.rtGroup;
    public RuleScope scope = RuleScope.rsGlobal;
    public RulePart part = RulePart.rpAllotMachine;
    public RulePeriod period = RulePeriod.rsNotApply;
    public String id;
    public String name;
    public String desc;
    public String expr;
    public int level = 0;

    public Object attatchObject = null;

    public RuleExprs exprs = new RuleExprs();

    RuleExprExecuter executer;
    RuleExprParser.ParamNameInfos paramNameInfos;

    public Rule() {
        executer = new RuleExprExecuter(this);
        paramNameInfos = executer.findParams();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public RuleType getRuleType() {
        return type;
    }

    @Override
    public RuleScope getRuleScope() {
        return scope;
    }

    @Override
    public RulePart getRulePart() {
        return part;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public RuleExprs getRuleExprs() {
        return exprs;
    }

    @Override
    public String getRuleExpr() {
        return expr;
    }

    public JSONObject toJson() {
        JSONObject result = new JSONObject();
        result.put("type", type.name());
        result.put("scope", scope.name());
        result.put("part", part.name());
        result.put("period", period.name());
        result.put("id", id);
        result.put("name", name);
        result.put("desc", desc);
        result.put("level", level);
        result.put("exprs", exprs.toJson());
        result.put("expr", expr);

        return result;
    }

    @Override
    public void load(JSONObject data) {
        fromJson(data);
    }

    public interface  IComputeProc {
        void getUserParameter(String key, String value);
        Object getSystemParameter(RuleTarget target, String groupName, String attrName);
    }

    protected void setParamValue(Map<String, String> map, RuleTarget target, IComputeProc computeProc, List<RuleExprExecuter.ExprParam<?>> paramValues){
        if (map.size() > 0)
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String[] keys = entry.getKey().split("\\.");
                Object value = computeProc.getSystemParameter(target, keys[1], keys[2]);
                RuleExprExecuter.ExprParam<Object> paramInfo = new RuleExprExecuter.ExprParam<>(entry.getKey(), value);
                paramValues.add(paramInfo);
            }
    }

    @SuppressWarnings("unchecked")
	@Override
    public <T> T compute(IComputeProc computeProc) throws Exception {
        List<RuleExprExecuter.ExprParam<?>> paramValues = new ArrayList<>();

        if (paramNameInfos != null) {
            setParamValue(paramNameInfos.devices, RuleTarget.ftDevice, computeProc, paramValues);
            setParamValue(paramNameInfos.moulds, RuleTarget.ftModule, computeProc, paramValues);
            setParamValue(paramNameInfos.materials, RuleTarget.ftMaterial, computeProc, paramValues);
        }

        Object r = executer.execute(paramValues.toArray(new RuleExprExecuter.ExprParam[paramValues.size()]));
        return (T) r;
    }

    public void fromJson(JSONObject data) {
        if (data.has("type"))
            type = RuleType.valueOf(data.getString("type"));
        if (data.has("scope"))
            scope = RuleScope.valueOf(data.getString("scope"));
        if (data.has("part"))
            part = RulePart.valueOf(data.getString("part"));
        if (data.has("period"))
            period = RulePeriod.valueOf(data.getString("period"));
        if (data.has("id"))
            id = data.getString("id");
        if (data.has("name"))
            name = data.getString("name");
        if (data.has("desc"))
            desc = data.getString("desc");
        if (data.has("level"))
            level = data.getInt("level");
        if (data.has("expr"))
            expr = data.getString("expr");
        if (data.has("exprs")) {
            exprs = RuleExprs.fromJson(data.getJSONArray("exprs"));
        } else {
            exprs = new RuleExprs();
        }
    }

    public static String[] names() {
        return new String[]{"规则名称", "优先级", "规则ID", "规则类型", "作用域", "应用范围", "规则表达式", "规则", "说明"};
    }

    public Object[] values() {
        return new Object[]{name, level, id, type, scope, period, expr, exprs, desc};
    }

    public Object valueOfName(String name) {
        switch (name.toUpperCase().trim()) {
            case "规则名称":
                return name;
            case "优先级":
                return level;
            case "规则ID":
                return id;
            case "规则类型":
                return type;
            case "作用域":
                return scope;
            case "规则表达式":
                return expr;
            case "说明":
                return desc;
            case "规则":
                return exprs;
            case "应用范围":
                return period;
            default:
                return part;
        }
    }

    public void setValue(String name, Object value) {
        switch (name.toUpperCase().trim()) {
            case "规则名称":
                this.name = value.toString();
                break;
            case "优先级":
                level = (int) value;
                break;
            case "规则ID":
                id = value.toString();
                break;
            case "规则表达式":
                expr = value == null ? null : value.toString();
                break;
            case "规则类型":
                type = (RuleType) value;
                break;
            case "作用域":
                scope = (RuleScope) value;
                break;
            case "应用范围":
                period = (RulePeriod) value;
                break;
            case "规则":
                JSONArray json = null;
                if (value instanceof JSONArray) {
                    json = (JSONArray) value;
                } else if (value != null && !value.toString().isEmpty()) {
                    json = new JSONArray(value.toString());
                }
                if (json != null)
                    exprs = RuleExprs.fromJson(json);
                else {
                    exprs = new RuleExprs();
                }
                break;
            case "说明":
                desc = value == null ? null : value.toString();
                break;
            default:
                part = RulePart.fromCode((int) value);
                break;
        }
    }

}