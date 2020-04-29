package wh.role.interfaces;

import org.json.JSONObject;

public interface ISerializationJson {
	JSONObject toJson();

	void fromJson(JSONObject json);

	String getKey();
}