package com.wh.gaea.control.requirement;

import java.awt.Desktop;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.wh.gaea.control.EditorEnvironment;
import com.wh.gaea.control.requirement.ViewControl.RequirementState;
import com.wh.swing.tools.MsgHelper;
import com.wh.tools.FileHelp;
import com.wh.tools.JsonHelp;

import wh.excel.simple.model.ExcelSwitchModel;

public abstract class RequirementController {
	/**
	 * 获取当前项目需求列表的主描述文件
	 * 
	 * @return 主描述文件对象
	 */
	public static File getRequirementMainFile() {
		return new File(EditorEnvironment.getProjectPath(EditorEnvironment.Requirement_Dir_Name), "main.rmm");
	}

	public static String[] getRequirementTypeNames() {
		List<String> names = new ArrayList<>();
		for (File file : getRequirementFiles()) {
			names.add(FileHelp.removeExt(file.getName()));
		}

		return names.toArray(new String[names.size()]);
	}

	/**
	 * 获取当前项目的所有需求文件，每个文件为一个需求分类
	 * 
	 * @return 需求文件对象数组
	 */
	public static File[] getRequirementFiles() {
		File path = EditorEnvironment.getProjectPath(EditorEnvironment.Requirement_Dir_Name);
		return path.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isFile()
						&& FileHelp.GetExt(file.getName()).equals(EditorEnvironment.Requirement_File_Extension);
			}
		});
	}

	/**
	 * 保存需求信息到文件
	 * 
	 * @param file 要保存的需求文件
	 * @param data 要保存的数据，包括当前版本、发布信息、需求列表
	 * @throws Exception
	 */
	public static void saveRequirement(File file, JSONObject data) throws Exception {
		JsonHelp.saveJson(file, data, null);
	}

	/**
	 * 保存一个分类需求信息到文件
	 * 
	 * @param file       要保存的目标文件
	 * @param curVersion 当前版本，可为null，此时取文件内的当前版本
	 * @param data       要保存的需求信息
	 * @throws Exception
	 */
	public static void saveRequirement(File file, JSONArray data, boolean needLoad) throws Exception {
		if (!file.getParentFile().exists())
			if (!file.getParentFile().mkdirs()) {
				MsgHelper.showMessage("建立目录[" + file.getParentFile().getAbsolutePath() + "]失败！");
				return;
			}

		JSONObject saveData = new JSONObject();
		if (file.exists()) {
			if (needLoad)
				saveData = (JSONObject) JsonHelp.parseCacheJson(file, null);
			if (!file.delete()) {
				MsgHelper.showMessage("删除需求列表文件失败，请手动删除后重试！");
				Desktop.getDesktop().open(file.getParentFile());
			}
		}

		HashMap<String, JSONObject> rows = new HashMap<>();
		if (saveData.has(RequirementDefines.KEY_DATA))
			for (Object object : saveData.getJSONArray(RequirementDefines.KEY_DATA)) {
				JSONObject row = (JSONObject) object;
				rows.put(row.getString(RequirementDefines.field_id), row);
			}

		for (Object object : data) {
			JSONObject row = (JSONObject) object;
			rows.put(row.getString(RequirementDefines.field_id), row);
		}

		data = new JSONArray();
		for (JSONObject row : rows.values()) {
			data.put(row);
		}

		saveData.put(RequirementDefines.KEY_DATA, data);

		saveRequirement(file, saveData);
	}

	/**
	 * 获取指定版本的需求信息列表，此方法不会获取当前版本及发布版本信息
	 * 
	 * @param version 要获取的版本号
	 * @return 需求信息列表
	 * @throws Exception
	 */
	public static JSONArray loadRequirement(String version, boolean used) throws Exception {
		return loadRequirement(version, RequirementState.rsAll, used);
	}

	public static JSONArray loadRequirement(String version, RequirementState requirementState, boolean used)
			throws Exception {
		File[] files = getRequirementFiles();
		if (files == null)
			return new JSONArray();

		JSONArray data = new JSONArray();
		for (File file : files) {
			String classType = FileHelp.removeExt(file.getName());
			JSONObject info = (JSONObject) JsonHelp.parseCacheJson(file, null);
			for (Object object : info.getJSONArray(RequirementDefines.KEY_DATA)) {
				JSONObject row = (JSONObject) object;
				row.put(RequirementDefines.field_class, classType);
				if (row.getString(RequirementDefines.field_version).equals(version)
						&& row.getBoolean(RequirementDefines.field_used) == used) {
					boolean closed = false;
					switch (requirementState) {
					case rsAll:
						data.put(row);
						break;
					case rsClose:
						closed = true;
					case rsNoClose:
						if (row.has(RequirementDefines.field_close)) {
							if (row.getBoolean(RequirementDefines.field_close) == closed) {
								data.put(row);
							}
						} else {
							if (!closed)
								data.put(row);
						}
						break;
					}
				}
			}
		}

		return data;
	}

	/**
	 * 获取需求文件信息
	 * 
	 * @param file 要获取的需求文件
	 * @return 需求信息，包含需求列表、当前版本、发布版本信息
	 * @throws Exception
	 */
	public static JSONObject loadRequirement(File file) throws Exception {
		return (JSONObject) JsonHelp.parseCacheJson(file, null);
	}

	/**
	 * 从excel中导入数据到需求文件
	 * 
	 * @param templateFile excel必须是系统定义的导入模板文件， 文件中必须包含待导入的数据
	 * @throws Exception
	 */
	public static void importRequirementFromExcel(File templateFile) throws Exception {

		ExcelSwitchModel excel = new ExcelSwitchModel();
		JSONArray dataset = (JSONArray) excel.exportTo(templateFile, "config", "data", false).data;
		HashMap<String, JSONObject> types = new HashMap<>();
		HashMap<String, HashMap<String, String>> rowMap = new HashMap<>();
		for (Object object : dataset) {
			JSONObject row = (JSONObject) object;
			String typeName = row.getString("分类");

			String id = row.getString(RequirementDefines.field_id);
			JSONObject data;
			HashMap<String, String> rowCheckMap;
			if (!types.containsKey(typeName)) {
				File file = getRequirementFile(typeName);
				data = loadRequirement(file);
				types.put(typeName, data);
				rowCheckMap = new HashMap<>();
				for (Object obj : data.getJSONArray(RequirementDefines.KEY_DATA)) {
					JSONObject existRow = (JSONObject) obj;
					String exId = existRow.getString(RequirementDefines.field_id);
					rowCheckMap.put(exId, exId);
				}
				rowMap.put(typeName, rowCheckMap);
			} else {
				data = types.get(typeName);
				rowCheckMap = rowMap.get(typeName);
			}

			String version = row.getString(RequirementDefines.field_version);

			if (data.has(RequirementDefines.KEY_PUBLISH)) {
				if (data.getJSONObject(RequirementDefines.KEY_PUBLISH).has(version)) {
					throw new Exception(version + "版本已经发布，不能导入数据！");
				}
			}

			if (rowCheckMap.containsKey(id)) {
				throw new Exception("id[" + id + "]已经存在需求类型为[" + typeName + "]的列表中，不能导入数据！");
			}

			rowCheckMap.put(id, id);

			data.getJSONArray(RequirementDefines.KEY_DATA).put(row);
		}

		for (String typeName : types.keySet()) {
			File file = getRequirementFile(typeName);
			saveRequirement(file, types.get(typeName));
		}
	}

	/**
	 * 将需求文件导出到excel
	 * 
	 * @param mapTemplateFile  参见下面函数定义
	 * @param dataTemplateFile 参见下面函数定义
	 * @param saveFile         参见下面函数定义
	 * @param requirementFile  需求列表文件
	 * @throws Exception
	 */
	public static void requirementExportToExcel(File mapTemplateFile, File dataTemplateFile, File saveFile,
			File requirementFile) throws Exception {
		JSONObject data = loadRequirement(requirementFile);
		JSONArray rows = data.getJSONArray(RequirementDefines.KEY_DATA);
		String typeName = getRequirementType(requirementFile);
		requirementExportToExcel(mapTemplateFile, dataTemplateFile, saveFile, typeName, rows);
	}

	/**
	 * 将需求信息导出到excel
	 * 
	 * @param mapTemplateFile  excel与数据源的映射模板文件
	 * @param dataTemplateFile excel的数据样式模板文件
	 * @param saveFile         保存excel数据的文件
	 * @param data             要导出的需求信息
	 * @throws Exception
	 */
	public static void requirementExportToExcel(File mapTemplateFile, File dataTemplateFile, File saveFile,
			String typeName, JSONArray data) throws Exception {
		ExcelSwitchModel excel = new ExcelSwitchModel();
		for (Object object : data) {
			JSONObject row = (JSONObject) object;
			if (typeName == null || typeName.isEmpty())
				row.put("分类", row.get(RequirementDefines.field_class));
			else
				row.put("分类", typeName);
		}

		JSONObject dataset = new JSONObject();
		dataset.put("default", data);
		excel.importFrom(mapTemplateFile, dataTemplateFile, saveFile, "config", "data", dataset);
	}

	public static File getRequirementFile(String typeName) {
		return new File(EditorEnvironment.getProjectPath(EditorEnvironment.Requirement_Dir_Name),
				typeName + "." + EditorEnvironment.Requirement_File_Extension);
	}

	public static void syncRenameRequirementFile(RequirementTypeInfo info) throws IOException {
		if (info.file != null && !info.name.equals(FileHelp.removeExt(info.file.getName())) && info.file.exists())
			if (!info.file.renameTo(new File(info.file.getParentFile(),
					info.name + "." + EditorEnvironment.Requirement_File_Extension)))
				throw new IOException("不能重命名需求类别文件【" + info.name + "】，将恢复原类别名称！");
	}

	public static String getRequirementType(File requirementFile) {
		return FileHelp.removeExt(requirementFile.getName());
	}

	public static class RequirementTypeInfo {
		public String memo;
		public String name;
		public File file;

		@Override
		public String toString() {
			return name;
		}
	}

	public static TreeMap<String, RequirementTypeInfo> getRequirementTypes() {
		File file = getRequirementMainFile();
		try {
			if (!file.exists())
				return new TreeMap<>();

			JSONObject data = (JSONObject) JsonHelp.parseCacheJson(file, null);
			if (data == null)
				return new TreeMap<>();

			if (!data.has(RequirementDefines.KEY_TYPE))
				return new TreeMap<>();

			TreeMap<String, RequirementTypeInfo> result = new TreeMap<>();
			for (Object object : data.getJSONArray(RequirementDefines.KEY_TYPE)) {
				JSONObject row = (JSONObject) object;
				RequirementTypeInfo info = new RequirementTypeInfo();
				info.name = row.getString("name");
				info.memo = row.getString("memo");
				info.file = getRequirementFile(info.name);
				result.put(info.name, info);
			}

			return result;
		} catch (Exception e) {
			e.printStackTrace();
			MsgHelper.showException(e);
		}
		return new TreeMap<>();
	}

	public static void setRequirementTypes(HashMap<String, String> types) {
		File file = getRequirementMainFile();
		try {
			JSONObject data = new JSONObject();
			if (file.exists())
				data = (JSONObject) JsonHelp.parseCacheJson(file, null);

			JSONArray typeDatas = new JSONArray();

			if (types != null)
				for (String name : types.keySet()) {
					JSONObject row = new JSONObject();
					row.put("name", name);
					row.put("memo", types.get(name));
					typeDatas.put(row);
				}

			data.put(RequirementDefines.KEY_TYPE, typeDatas);
			JsonHelp.saveJson(file, data, null);
		} catch (Exception e) {
			e.printStackTrace();
			MsgHelper.showException(e);
		}
	}

	public static JSONObject getRequirementPublishes() {
		File file = getRequirementMainFile();
		try {
			if (!file.exists())
				return new JSONObject();

			JSONObject data = (JSONObject) JsonHelp.parseCacheJson(file, null);
			if (data == null)
				return new JSONObject();

			if (!data.has(RequirementDefines.KEY_PUBLISH))
				return new JSONObject();

			JSONObject result = new JSONObject();
			for (Object object : data.getJSONArray(RequirementDefines.KEY_PUBLISH)) {
				result.put((String) object, (String) object);
			}

			return result;
		} catch (Exception e) {
			e.printStackTrace();
			MsgHelper.showException(e);
		}
		return new JSONObject();
	}

	public static void setRequirementPublishes(JSONObject publishes) {
		setRequirementPublishes(publishes, null, null);
	}

	public static void setRequirementPublishes(JSONObject publishes, String version, Date unPublishTime) {
		File file = getRequirementMainFile();
		try {
			JSONObject data = new JSONObject();
			if (file.exists())
				data = (JSONObject) JsonHelp.parseCacheJson(file, null);

			JSONArray publishDatas = new JSONArray();

			if (publishes != null && publishes.names() != null)
				for (Object obj : publishes.names()) {
					publishDatas.put(obj);
				}

			data.put(RequirementDefines.KEY_PUBLISH, publishDatas);

			if (unPublishTime != null && version != null) {
				JSONObject uns = new JSONObject();
				if (data.has(RequirementDefines.KEY_UNPUBLISH_TIME)) {
					uns = data.getJSONObject(RequirementDefines.KEY_UNPUBLISH_TIME);
				} else {
					uns = new JSONObject();
					data.put(RequirementDefines.KEY_UNPUBLISH_TIME, uns);
				}

				JSONArray untimes;
				if (uns.has(version)) {
					untimes = uns.getJSONArray(version);
				} else
					untimes = new JSONArray();
				untimes.put(unPublishTime);
			}
			JsonHelp.saveJson(file, data, null);
		} catch (Exception e) {
			e.printStackTrace();
			MsgHelper.showException(e);
		}
	}

	public static void setRequirementVersion(String[] versions) {
		File file = getRequirementMainFile();
		try {
			JSONObject data = new JSONObject();
			if (file.exists())
				data = (JSONObject) JsonHelp.parseCacheJson(file, null);

			HashMap<String, Object> versionMap = new HashMap<>();
			if (data.has(RequirementDefines.KEY_VERSION))
				for (Object object : data.getJSONArray(RequirementDefines.KEY_VERSION)) {
					versionMap.put((String) object, object);
				}

			for (String version : versions) {
				versionMap.put(version, version);
			}

			JSONArray publishDatas = new JSONArray();
			for (String version : versionMap.keySet()) {
				publishDatas.put(version);
			}

			data.put(RequirementDefines.KEY_VERSION, publishDatas);

			JsonHelp.saveJson(file, data, null);
		} catch (Exception e) {
			e.printStackTrace();
			MsgHelper.showException(e);
		}
	}

	public static HashMap<String, Object> getRequirementVersionMap() {
		HashMap<String, Object> result = new HashMap<>();
		for (Object version : getRequirementVersions()) {
			result.put((String) version, version);
		}
		return result;
	}

	public static void publish(String version) throws Exception {
		if (version == null || version.isEmpty()) {
			throw new Exception("请输入当前版本号！");
		}

		JSONObject data = RequirementController.getRequirementPublishes();
		data.put(version, version);
		RequirementController.setRequirementPublishes(data);
	}

	public static void unPublish(String version) throws Exception {
		if (version == null || version.isEmpty()) {
			throw new Exception("请输入当前版本号！");
		}

		JSONObject data = RequirementController.getRequirementPublishes();
		if (data.has(version))
			data.remove(version);
		RequirementController.setRequirementPublishes(data);
	}

	public static boolean isPublish(String version) {
		if (version == null || version.isEmpty()) {
			return false;
		}

		JSONObject data = RequirementController.getRequirementPublishes();
		return data.has(version);
	}

	public static Collection<String> getRequirementVersions() {
		File file = getRequirementMainFile();
		try {
			JSONObject data = new JSONObject();
			if (file.exists())
				data = (JSONObject) JsonHelp.parseCacheJson(file, null);

			if (data.has(RequirementDefines.KEY_VERSION)) {
				JSONArray result = data.getJSONArray(RequirementDefines.KEY_VERSION);
				TreeMap<String, Object> resultMap = new TreeMap<>();
				for (Object object : result) {
					resultMap.put((String) object, object);
				}
				return resultMap.keySet();
			}
		} catch (Exception e) {
			e.printStackTrace();
			MsgHelper.showException(e);
		}
		return new ArrayList<>();
	}

	public static String getRequirementMainVersion() {
		File file = getRequirementMainFile();
		try {
			if (!file.exists())
				return null;

			JSONObject data = (JSONObject) JsonHelp.parseCacheJson(file, null);
			if (data == null)
				return null;

			if (!data.has(RequirementDefines.KEY_MAIN_VERSION))
				return null;

			return data.getString(RequirementDefines.KEY_MAIN_VERSION);
		} catch (Exception e) {
			e.printStackTrace();
			MsgHelper.showException(e);
		}
		return null;
	}

	public static void setRequirementMainVersion(String version) {
		File file = getRequirementMainFile();
		try {
			JSONObject data = new JSONObject();
			if (file.exists())
				data = (JSONObject) JsonHelp.parseCacheJson(file, null);

			data.put(RequirementDefines.KEY_MAIN_VERSION, version);
			JsonHelp.saveJson(file, data, null);
		} catch (Exception e) {
			e.printStackTrace();
			MsgHelper.showException(e);
		}
	}

	public static void relateRequirementModel(JSONObject data, String nodeId) throws Exception {
		data.put(RequirementDefines.field_model, nodeId);
	}
	
	
	public static void addRequirement(JSONObject data, JSONArray saveData) throws Exception {
		saveData.put(data);
	}
	
	public static JSONObject newRequirement(String requirement, String[] rules, String id, String user, String version,
			boolean used, boolean closed, String requirementType, int requirementLevel, Date useTime, Date planTime,
			Date closeTime) throws Exception {

		if (user == null) {
			user = "无";
		}

		if (id == null) {
			throw new NullPointerException("必须输入需求编号！");
		}

		if (version == null || version.trim().isEmpty()) {
			version = "1.0";
		}
		
		if (requirementType == null || requirementType.trim().isEmpty())
			requirementType = "基本需求";

		switch (requirementType) {
		case "基本需求":
		case "增强需求":
		case "规划需求":
			break;
		default:
			requirementType = "基本需求";
			break;
		}
		JSONObject data = new JSONObject();
		data.put(RequirementDefines.field_id, id);
		data.put(RequirementDefines.field_requirement, requirement);
		data.put(RequirementDefines.field_used, used);
		data.put(RequirementDefines.field_close, closed);
		data.put(RequirementDefines.field_user, user);
		data.put(RequirementDefines.field_version, version);
		data.put(RequirementDefines.field_type, requirementType);
		data.put(RequirementDefines.field_level, requirementLevel);
		data.put(RequirementDefines.field_use_time,
				useTime == null ? null : new SimpleDateFormat(RequirementDefines.DATE_FORMAT).format(useTime));
		data.put(RequirementDefines.field_close_time,
				closeTime == null ? null : new SimpleDateFormat(RequirementDefines.DATE_FORMAT).format(closeTime));
		data.put(RequirementDefines.field_plan_time,
				planTime == null ? null : new SimpleDateFormat(RequirementDefines.DATE_FORMAT).format(planTime));

		if (rules != null && rules.length > 0)
			data.put(RequirementDefines.field_role, new JSONArray(rules));

		return data;
	}

}
