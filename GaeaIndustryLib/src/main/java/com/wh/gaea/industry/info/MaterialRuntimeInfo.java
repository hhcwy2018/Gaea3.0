package com.wh.gaea.industry.info;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.wh.gaea.industry.interfaces.DynamicParamInfo;

public class MaterialRuntimeInfo {
    public String id;
    public String name;
    public String group;
    public String groupCode;
    public String unit;
    public String type;
    public String code;
    public String[] supplier;
    public String model;
    public String desc;
    public String packageUnit;

    public Map<String, DynamicParamInfo> paramDefines = new ConcurrentHashMap<>();
    public Map<String, Object> paramMap = new ConcurrentHashMap<>();

    public MaterialRuntimeInfo() {

    }

}
