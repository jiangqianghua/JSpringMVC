package com.jiang.demo.mvc.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jiang.demo.service.IDemoService;
import com.jiang.mvcframework.annotation.JAutowired;
import com.jiang.mvcframework.annotation.JController;
import com.jiang.mvcframework.annotation.JRequestMapping;
import com.jiang.mvcframework.annotation.JRequestParam;

@JController
@JRequestMapping("/web/info")
public class DemoAction {

	@JAutowired 
	IDemoService demoService ;
	
	//http://127.0.0.1:8081/web/info/query.json?name=jiang
	@JRequestMapping("/query.json")
	public void query(HttpServletRequest request,
					HttpServletResponse resp/**,
					@JRequestParam("name") String name**/){
//		demoService.query(name);
		
		String name = request.getParameter("name");
		System.out.println("do query..." + name);
	}
}
