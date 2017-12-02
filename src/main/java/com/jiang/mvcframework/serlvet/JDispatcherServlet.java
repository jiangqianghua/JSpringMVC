package com.jiang.mvcframework.serlvet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jiang.mvcframework.annotation.JAutowired;
import com.jiang.mvcframework.annotation.JController;
import com.jiang.mvcframework.annotation.JRequestMapping;
import com.jiang.mvcframework.annotation.JService;

public class JDispatcherServlet extends HttpServlet{

	private Properties p = new Properties();
	private List<String> classes = new ArrayList<String>();
	private Map<String, Object> ioc = new HashMap<String, Object>();
	private Map<String, Method> handlerMapping = new HashMap<String,Method>();
	private Map<String,String> iocMapping = new HashMap<String,String>();
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		System.out.println("doPost");
		try{
			doDispatch(req, resp);
		}catch(Exception e)
		{
			resp.getWriter().write("500 Exception,Detail:\r\n"+Arrays.toString(e.getStackTrace()));
		}
	}
	/**
	 * 执行具体方法
	 * @param req
	 * @param resp
	 * @throws IOException
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 */
	private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		if(handlerMapping.isEmpty())
		{
			resp.getWriter().write("404 not Found");
			return;
		}
		
		String url = req.getRequestURI();
		String	contextPath = req.getContextPath();
		url = url.replace(contextPath, "").replaceAll("/+", "/");
		int splitPointOne = url.lastIndexOf("/");
		String action  = url.substring(0,splitPointOne).trim();
		boolean isPattern = false ;
		for(Entry<String, Method> mapping:handlerMapping.entrySet()){
			if(mapping.getKey().equals(url)){
				System.out.println(mapping.getValue());
				mapping.getValue().invoke(ioc.get(iocMapping.get(action)), req,resp);
				isPattern = true;
				break;
			}
		}
		
		if(!isPattern)
		{
			resp.getWriter().write("404 not Found");
		}
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		System.out.println("JSpringMVC 已经启动" + config);
		//进入JSpringMVC核心环节
		// 1 加载我们的配置文件 applcation.xml，我们用applocation.properties代替
		doLoadConfig(config.getInitParameter("contextConfigLoaction"));
		// 2 扫描所有满足条件的@Controller @Service
		doScanner(p.getProperty("scanPackge"));
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
		InputStream fis = this.getClass().getClassLoader().getResourceAsStream(location);
		try {
			p.load(fis);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			fis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void doScanner(String packget){
		//  从class文件目录下获取class文件
		URL url = this.getClass().getClassLoader().getResource("/"+packget.replaceAll("\\.", "/"));
		File dir = new File(url.getFile());
		for(File file:dir.listFiles()){
			if(file.isDirectory()){
				doScanner(packget+"."+file.getName());
			}
			else
			{
				String className = packget+"."+file.getName().replace(".class", "");
				classes.add(className);
				System.out.println("packgetname = "+className);
			}
		}
	}
	
	private void doInstance(){
		if(classes.isEmpty()) return ; 
		try
		{
			for(String className:classes){
				Class<?> clazz = Class.forName(className);
				if(clazz.isAnnotationPresent(JController.class)){
					// 保存到ioc容器中 map
					String beanName =lowerFirst(clazz.getSimpleName());
					ioc.put(beanName,clazz.newInstance());
					System.out.println("ioc input "+beanName);
				}
				else if(clazz.isAnnotationPresent(JService.class)){
					
					JService service = clazz.getAnnotation(JService.class);
					String beanName = service.value();// 获取Service注解上取的名字
					if(!"".equals(beanName.trim())){
						ioc.put(beanName	, clazz.newInstance());
					}
					else{
						beanName =lowerFirst(clazz.getSimpleName());
						ioc.put(beanName, clazz.newInstance());
					}
					System.out.println("ioc input "+beanName);
					Class<?>[] interfaces = clazz.getInterfaces();
					for(Class<?> i:interfaces){
						beanName =lowerFirst(i.getSimpleName());
						ioc.put(beanName, clazz.newInstance());
						System.out.println("ioc interface input "+beanName);
					}
				}
				else
				{
					continue;
				}
			}
		}
		catch(Exception e)
		{
			
		}
	}
	
	private void doAutoWried(){
		if(ioc.isEmpty()) return ;
		for(Entry<String, Object> entry:ioc.entrySet()){
			//获取所有属性
			Field[] fields = entry.getValue().getClass().getDeclaredFields();
			for(Field field:fields){
				if(!field.isAnnotationPresent(JAutowired.class)) continue;
				JAutowired autowired = field.getAnnotation(JAutowired.class);
				String beanName =autowired.value().trim();
				if("".equals(beanName)){
					beanName = field.getType().getName();
				}
				// 私有属性，也需要注入
				field.setAccessible(true);
				
				try {
					field.set(entry.getValue(), ioc.get(beanName));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					continue;
				}
			}
		}
	}
	
	private void doInitHandlerMapping(){
		if(ioc.isEmpty()) return ;
		for(Entry<String, Object> entry:ioc.entrySet()){
			Class<?> clazz = entry.getValue().getClass();
			// 只有加了JController才有RequestMapping
			if(!clazz.isAnnotationPresent(JController.class))
				continue;
			String url ="";
			// 获取类的url
			if(clazz.isAnnotationPresent(JRequestMapping.class)){
				JRequestMapping requestMapping = clazz.getAnnotation(JRequestMapping.class);
				url = requestMapping.value();
				iocMapping.put(url, entry.getKey());
			}
			// 拼接方法的url
			Method[] methods = clazz.getMethods();
			for(Method method:methods){
				if(!method.isAnnotationPresent(JRequestMapping.class))
					continue;
				JRequestMapping requestMapping = method.getAnnotation(JRequestMapping.class);
				String mulr = ("/"+url+requestMapping.value()).replaceAll("/+", "/");
				handlerMapping.put(mulr, method);
				System.out.println("mapping " + mulr + " : "+method);
			}
		}
	}
	
	/**
	 * 首字母小写
	 * @param str
	 * @return
	 */
	private String lowerFirst(String str)
	{
		char[] chars = str.toCharArray();
		chars[0] += 32;
		return String.valueOf(chars);
	}

}
