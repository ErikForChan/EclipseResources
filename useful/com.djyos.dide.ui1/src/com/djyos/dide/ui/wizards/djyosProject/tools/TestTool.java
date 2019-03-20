package com.djyos.dide.ui.wizards.djyosProject.tools;

import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
//import org.json.JSONObject;
import org.json.JSONObject;

import com.djyos.dide.ui.helper.DideHelper;

public class TestTool {

	public static void test_json() {
		JSONObject object = new JSONObject();
		List<Integer> integers = Arrays.asList(1,2,3);
		try {
			object.put("list", integers);
			DideHelper.showErrorMessage(object.get("list").toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
