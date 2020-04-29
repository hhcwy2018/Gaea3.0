package com.wh.tools;

import org.json.JSONArray;
import org.json.JSONObject;

import com.wh.encrypt.MD5;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.ComputerSystem;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.software.os.OperatingSystem;

public class HardwareInformation {
	static SystemInfo si;
	static HardwareAbstractionLayer hal;
	static {
		si = new SystemInfo();
		hal = si.getHardware();
	}

	public void setOperatingSystemInfo(JSONObject data) {
		OperatingSystem os = si.getOperatingSystem();
		data.put("os", os.toString());
	}

	public void setComputerSystem(JSONObject data) {
		ComputerSystem computerSystem = hal.getComputerSystem();
		
		JSONObject value = new JSONObject();
		value.put("manufacturer", computerSystem.getManufacturer());
		value.put("model", computerSystem.getModel());
		value.put("serialnumber", computerSystem.getSerialNumber());
		data.put("mainboard", value);
    }

	public void setProcessor(JSONObject data) {
    	CentralProcessor processor = hal.getProcessor();
		JSONObject value = new JSONObject();
		value.put("identifier", processor.getProcessorIdentifier().getIdentifier());
		value.put("cpuid", processor.getProcessorIdentifier().getProcessorID());
        data.put("cpu", value);
    }

	public void setDisk(JSONObject data) {
    	HWDiskStore[] diskStores = hal.getDiskStores();
    	JSONArray disks= new JSONArray();
    	
        for (HWDiskStore disk : diskStores) {
        	JSONObject diskInfo = new JSONObject();
            diskInfo.put("name", disk.getName());
            diskInfo.put("model", disk.getModel());
            diskInfo.put("serial", disk.getSerial());
            diskInfo.put("size", disk.getSize());
            disks.put(diskInfo);
        }
        
        data.put("disk", disks);
    }
    
	public void setNetwork(JSONObject data) {
		NetworkIF[] networkIFs = hal.getNetworkIFs();
		JSONArray nets = new JSONArray();
        for (NetworkIF net : networkIFs) {
        	JSONObject netInfo = new JSONObject();
        	netInfo.put("mac", net.getMacaddr());
        	nets.put(netInfo);
        }
        
        data.put("net", nets);
    }
    
	public JSONObject getFeatureCode() {
		
		JSONObject data = new JSONObject();
		setOperatingSystemInfo(data);
		setComputerSystem(data);
		setDisk(data);
		setProcessor(data);
		setNetwork(data);

		return data;
	}

	public String getFeatureMD5() {
		
		JSONObject data = getFeatureCode();

		return MD5.encode(data.toString());
	}

}
