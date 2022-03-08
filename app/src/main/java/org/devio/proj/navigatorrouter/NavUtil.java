package org.devio.proj.navigatorrouter;

import android.content.ComponentName;
import android.content.Context;
import android.content.res.AssetManager;
import android.view.Menu;
import android.view.MenuItem;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.ActivityNavigator;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.NavGraphNavigator;
import androidx.navigation.NavigatorProvider;
import androidx.navigation.fragment.DialogFragmentNavigator;


public class NavUtil {

    /**
     * key：pageUrl value：Destination
     */
    private static HashMap<String, Destination> destinationHashMap;

    /**
     * 由于我们删除掉mobile_navigation.xml文件，那我们就需要自己处理解析流程，然后把节点和各个类进行关联
     * 赋值给NavGraph
     *
     * @param activity             上下文
     * @param controller           控制器
     * @param childFragmentManager 必须是childFragmentManager 源码中创建FragmentNavigator和DialogNavigator都是用的它
     * @param containerId          activity.xml中装载NavHostFragment的id
     */
    public static void buildNavGraph(FragmentActivity activity,
                                     @NonNull NavController controller,
                                     FragmentManager childFragmentManager,
                                     int containerId) {


        //获取json文件内容
        String content = parseFile(activity, "destination.json");

        //json文件映射成实体HashMap
        destinationHashMap = JSON.parseObject(content, new TypeReference<HashMap<String, Destination>>() {
        }.getType());


        /**
         * 创建NavGraph  它是解析mobile_navigation.xml文件后，存储所有节点的Destination
         *  我们解析的Destination节点，最终都要存入NavGraph中
         */
        // 获取Navigator管理器中的Map 添加Destination
        NavigatorProvider navigatorProvider = controller.getNavigatorProvider();
        //创建NavGraphNavigator 跳转类
        NavGraphNavigator navigator = new NavGraphNavigator(navigatorProvider);
        // 最终目的是创建navGraph
        NavGraph navGraph = new NavGraph(navigator);


        //创建我们自定义的FragmentNavigator
        HiFragmentNavigator hiFragmentNavigator = new HiFragmentNavigator(activity, childFragmentManager, containerId);
        //添加到Navigator管理器中
        navigatorProvider.addNavigator(hiFragmentNavigator);

        //获取所有value数据
        Iterator<Destination> iterator = destinationHashMap.values().iterator();

        while (iterator.hasNext()) {
            Destination destination = iterator.next();
            if (destination.destType.equals("activity")) {
                //如果是activity类型，上节源码中分析，它的必要参数是ComponentName

                ActivityNavigator activityNavigator = navigatorProvider.getNavigator(ActivityNavigator.class);
                //通过activityNavigator得到ActivityNavigator.Destination
                ActivityNavigator.Destination node = activityNavigator.createDestination();
                node.setId(destination.id);
                node.setComponentName(new ComponentName(activity.getPackageName(), destination.clzName));

                //添加到我们的navGraph对象中 它存储了所有的节点
                navGraph.addDestination(node);
            } else if ((destination.destType.equals("fragment"))) {
                HiFragmentNavigator.Destination node = hiFragmentNavigator.createDestination();
                node.setId(destination.id);
                node.setClassName(destination.clzName);

                navGraph.addDestination(node);
            } else if (destination.destType.equals("dialog")) {
                DialogFragmentNavigator dialogFragmentNavigator = navigatorProvider.getNavigator(DialogFragmentNavigator.class);
                DialogFragmentNavigator.Destination node = dialogFragmentNavigator.createDestination();
                node.setId(destination.id);
                node.setClassName(destination.clzName);

                navGraph.addDestination(node);
            }

            //如果当前节点
            if (destination.asStarter) {
                navGraph.setStartDestination(destination.id);
            }
        }
        // 视图navGraph和controller 相关联
        controller.setGraph(navGraph);

    }

    private static String parseFile(Context context, String fileName) {

        AssetManager assetManager = context.getAssets();
        StringBuilder builder = null;
        try {
            InputStream inputStream = assetManager.open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            builder = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            inputStream.close();
            reader.close();
            return builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * main_tabs_config.json 通常由服务器下发，告知我们那些menu需要展示
     * 自定义BottomBar的目的是 让Tab和Destination建立映射关系
     * 根据pageUrl断定那个menu对应那个Destination
     *
     * 也就是bottom_nav_menu.xml文件 中的配置 按照对应要求 改成json文件后端下发
     */
    public static void builderBottomBar(BottomNavigationView navView) {
        String content = parseFile(navView.getContext(), "main_tabs_config.json");
        BottomBar bottomBar = JSON.parseObject(content, BottomBar.class);
        List<BottomBar.Tab> tabs = null;
        tabs = Objects.requireNonNull(bottomBar).tabs;

        Menu menu = navView.getMenu();
        for (BottomBar.Tab tab : tabs) {
            if (!tab.enable)
                continue;
            Destination destination = destinationHashMap.get(tab.pageUrl);
            if (destinationHashMap.containsKey(tab.pageUrl)) {//pageUrl对应不上 则表示无此页面
                //对应页面节点的destination.id要和menuItem  id对应
                if (destination!=null){
                    MenuItem menuItem = menu.add(0, destination.id, tab.index, tab.title);
                    menuItem.setIcon(R.drawable.ic_home_black_24dp);
                }
            }
        }
    }
}
