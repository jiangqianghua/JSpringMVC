package com.jiang.demo.mvc.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jiang.demo.service.IDemoService;
import com.jiang.mvcframework.annotation.JAutowired;
import com.jiang.mvcframework.annotation.JController;
import com.jiang.mvcframework.annotation.JRequestMapping;
import com.jiang.mvcframework.annotation.JRequestParam;

@JController
@JRequestMapping("/web")
public class DemoAction {

	@JAutowired 
	IDemoService demoService ;
	
	@JRequestMapping("/query.json")
	public void query(HttpServletRequest request,
					HttpServletResponse resp,
					@JRequestParam("name") String name){
		demoService.query(name);
	}
}
