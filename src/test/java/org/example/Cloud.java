package org.example;

import com.avos.avoscloud.AVObject;

import cn.leancloud.EngineFunction;
import cn.leancloud.EngineHook;

public class Cloud {

	@EngineFunction
	public String hello(String name) {
		return "hello " + name;
	}
	
	@EngineHook
	public AVObject beforeSave(AVObject obj) {
		return obj;
	}
	
	@EngineHook
	public void afterSave(AVObject obj) {
		
	}

}
