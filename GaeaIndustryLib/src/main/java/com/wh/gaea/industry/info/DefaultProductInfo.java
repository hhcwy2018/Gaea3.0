package com.wh.gaea.industry.info;

import java.util.Map;

import com.wh.gaea.industry.interfaces.IProduct;

public class DefaultProductInfo implements IProduct {
	
	public Integer count;
	public String materialId;
	public Map<String, Object> extendParams;

	public DefaultProductInfo() {
	}

	public DefaultProductInfo(int count, String materialId, ExtendParamInfo<Object>[] extendParams) {
		this.count = count;
		this.materialId = materialId;
		if (extendParams != null) {
			for (ExtendParamInfo<Object> entry : extendParams) {
				this.extendParams.put(entry.key, entry.value);
			}
		}
	}

	@Override
	public String materalId() {
		return materialId;
	}

	@Override
	public Integer count() {
		return count;
	}

	@Override
	public Map<String, Object> extendParams() {
		return extendParams;
	}

}