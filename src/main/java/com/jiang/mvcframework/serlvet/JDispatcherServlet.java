package com.jiang.mvcframework.serlvet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JDispatcherServlet extends HttpServlet{

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		System.out.println("doPost");
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		System.out.println("JSpringMVC 已经启动" + config);
		//进入JSpringMVC核心环节
		// 1 加载我们的配置文件 applcation.xml，我们用applocation.properties代替
		doLoadConfig(config.getInitParameter("contextConfigLoaction"));
		// 2 扫描所有满足条件的@Controller @Service
		doScanner();
		// 3 把这些类初始化并装配到IOC容器种
		doInstance();
		// 4 进行依赖注入
		doAutoWried();
		// 5 构造HandlerMapping映射关系，将一个URL映射一个Method
		doInitHandlerMapping();
		// 6 等待用户请求，然后匹配URL，定位方法，反射执行
		// 7 返回结果
	}
	
	
	private void doLoadConfig(String location){
		
	}
	
	private void doScanner(){
		
	}
	
	private void doInstance(){
		
	}
	
	private void doAutoWried(){
		
	}
	
	private void doInitHandlerMapping(){
		
	}

}
