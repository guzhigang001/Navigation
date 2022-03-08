package org.devio.proj.navigatorrouter

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import org.devio.proj.navigatorrouter.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
//
//        val navController = findNavController(R.id.nav_host_fragment_activity_main)
//        // Passing each menu ID as a set of Ids because each
//        // menu should be considered as top level destinations.
//        val appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
//            )
//        )
//        setupActionBarWithNavController(navController, appBarConfiguration)
//        navView.setupWithNavController(navController)


        //寻找路由控制器对象 ，它是我们路由唯一的出口
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        //NavHostFragment 容器
        val fragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main)
        NavUtil.buildNavGraph(
            this,
            navController,
            fragment!!.childFragmentManager,
            //容器 id
            R.id.nav_host_fragment_activity_main
        )

        //创建底部按钮 删除app:menu="@menu/bottom_nav_menu" 配置
        NavUtil.builderBottomBar(navView)

        //跳转itemId就是我们在builderBottomBar中 MenuItem的 destination.id --> menuItem = menu.add(0, destination.id, tab.index, tab.title);的
        navView.setOnItemSelectedListener { item ->
            navController.navigate(item.itemId)
            true
        }
    }
}