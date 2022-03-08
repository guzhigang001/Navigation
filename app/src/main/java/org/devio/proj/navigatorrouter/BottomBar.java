package org.devio.proj.navigatorrouter;

import java.util.List;

/**
 * @Author :ggxz
 * @Date: 2022/3/6
 * @Desc:
 */
public class BottomBar {

    /**
     * selectTab : 0
     * tabs : [{"size":24,"enable":true,"index":0,"pageUrl":"main/tabs/home","title":"Home"},{"size":24,"enable":true,"index":1,"pageUrl":"main/tabs/dashborad","title":"Dashboard"},{"size":40,"enable":true,"index":2,"pageUrl":"main/tabs/notification","title":"Notification"}]
     */

    public int selectTab;//默认选中下标
    public List<Tab> tabs;

    public static class Tab {
        /**
         * size : 24  按钮的大小
         * enable : true 是否可点击 不可点击则隐藏
         * index : 0 在第几个Item上
         * pageUrl : main/tabs/home   和路由节点配置相同，不存在则表示无此页面
         * title : Home  按钮文本
         */

        public int size;
        public boolean enable;
        public int index;
        public String pageUrl;
        public String title;
    }
}
