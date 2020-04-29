package com.wh.gaea.industry.info;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.wh.gaea.industry.interfaces.DynamicParamInfo;
import com.wh.gaea.industry.interfaces.IDInfo;
import com.wh.gaea.industry.interfaces.IMaterial;

public class MaterialInfo implements IMaterial, IDInfo {
	public String id;
	public String name;
	public String group;
	public String groupCode;
	public String unit;
	public String type;
	public String code;
	public String[] supplier;
	public String model;
	public String packageUnit;
	public String desc;

	public Map<String, DynamicParamInfo> paramMap = new ConcurrentHashMap<>();

	public MaterialInfo() {
		
	}
	
	public MaterialInfo(String id, String name, String unit, String model, ExtendParamInfo<DynamicParamInfo>[] paramMap) {
		this(id, name, unit, null, null, model, paramMap);
	}

	public MaterialInfo(String id, String name, String unit, String type, String code, String model,
			ExtendParamInfo<DynamicParamInfo>[] paramMap) {
		this(id, name, null, null, unit, type, code, null, model, null, null, paramMap);
	}

	public MaterialInfo(String id, String name, String group, String groupCode, String unit, String type, String code,
			String[] supplier, String model, String packageUnit, String desc, ExtendParamInfo<DynamicParamInfo>[] paramMap) {
		this.id = id;
		this.name = name;
		this.group = group;
		this.groupCode = groupCode;
		this.unit = unit;
		this.type = type;
		this.code = code;
		this.supplier = supplier;
		this.model = model;
		this.packageUnit = packageUnit;
		this.desc = desc;
		if (paramMap != null)
			for (ExtendParamInfo<DynamicParamInfo> extendParam : paramMap) {
				this.paramMap.put(extendParam.key, extendParam.value);				
			}

	}

	@Override
	public String toString() {
		return name + "[" + code + "]";
	}

	@Override
	public String id() {
		return id;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public Map<String, DynamicParamInfo> paramMap() {
		return paramMap;
	}

	@Override
	public String group() {
		return group;
	}

	@Override
	public String groupCode() {
		return groupCode;
	}

	@Override
	public String unit() {
		return unit;
	}

	@Override
	public String type() {
		return type;
	}

	@Override
	public String code() {
		return code;
	}

	@Override
	public String[] supplier() {
		return supplier;
	}

	@Override
	public String model() {
		return model;
	}

	@Override
	public String packageUnit() {
		return packageUnit;
	}

	@Override
	public String desc() {
		return desc;
	}
}