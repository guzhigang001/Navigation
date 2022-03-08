package org.devio.proj.navigatorrouter;

/**
 * @Author :ggxz
 * @Date: 2022/3/6
 * @Desc:
 */
public class Destination {
    public String pageUrl;  //页面url
    public int id;          //路由节点（页面）的id
    public boolean asStarter;//是否作为路由的第一个启动页
    public String destType;//路由节点(页面)的类型,activity,dialog,fragment
    public String clzName;//全类名
}
