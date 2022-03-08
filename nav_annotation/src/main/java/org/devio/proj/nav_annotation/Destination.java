package org.devio.proj.nav_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解,将这个注解
 * 注释到我们的要路由的类上面
 * 这样我们就可以获取配置的节点(e.g Activity/Fragment/Dialog)
 * 然后利用代码生成节点配置，替换掉nav_graph.xml;
 */
@Target(ElementType.TYPE)//类作用域
@Retention(RetentionPolicy.CLASS)//编译期生效
public @interface Destination {

    /**
     * 页面在路由中的名称
     */
    String pageUrl();

    /**
     * 节点是不是默认首次启动页
     */
    boolean asStarter() default false;
}
