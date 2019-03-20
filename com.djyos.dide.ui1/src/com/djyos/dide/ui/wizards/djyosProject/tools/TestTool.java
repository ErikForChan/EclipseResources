package com.djyos.dide.ui.wizards.djyosProject.tools;

import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

public class TestTool {

	public static void test_json() {
		JSONObject object = new JSONObject();
	    //string
	    try {
			object.put("string","string");
			  //int
		    object.put("int",2);
		    //boolean
		    object.put("boolean",true);
		    //array
		    List<Integer> integers = Arrays.asList(1,2,3);
		    object.put("list",integers);
		    //null
//		    object.put("null",null);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//	​
//	    System.out.println(object);
	    try {
			System.out.println(object.get("list"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
