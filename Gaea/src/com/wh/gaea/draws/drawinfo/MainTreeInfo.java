package com.wh.gaea.draws.drawinfo;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wh.gaea.control.EditorEnvironment;
import com.wh.gaea.draw.IUINode;
import com.wh.gaea.form.Defines;
import com.wh.gaea.interfaces.IDrawInfoDefines;
import com.wh.tools.ImageUtils;
import com.wh.tools.JsonHelp;

public class MainTreeInfo extends TreeInfo{
	public String typeName(){
		return IDrawInfoDefines.MainTree_Name;
	}
	
	public MainTreeInfo(IUINode node) {
		super(node);
		width = "200px";
		height = "100%";
	}

	public void drawNode(Graphics g, Rectangle rect){
		if (icon == null){
			try {
				icon = ImageUtils.loadImage(new File(Defines.Java_Dir_Icon_Resource.getAbsolutePath(), "nocheck.png"));
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
		
		int left = rect.x + 5;
		AtomicInteger top = new AtomicInteger(rect.y + 5);
		try {
			File file = EditorEnvironment.getMainNavTreeFile();
			if (!file.exists())
				return;
			
			JSONArray data = (JSONArray) JsonHelp.parseCacheJson(file, null);
			drawView(rect, g, data, left, top, rect.width, rect.y + rect.height);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public JSONObject toJson(boolean needAll) throws JSONException{
		JSONObject json = super.toJson(needAll);
		json.put("sharedata", "");
		return json;
	}
	
    public String sharedata;

}