# JSpringMVC
手写简易的SpringMVC框架
步骤
 1 加载我们的配置文件 applcation.xml，我们用applocation.properties代替

 2 扫描所有满足条件的@Controller @Service

 3 把这些类初始化并装配到IOC容器种

 4 进行依赖注入

 5 构造HandlerMapping映射关系，将一个URL映射一个Method

 6 等待用户请求，然后匹配URL，定位方法，反射执行
 
 7 返回结果
