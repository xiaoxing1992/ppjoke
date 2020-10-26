package com.mooc.ppjoke

import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mooc.libcommon.utils.StatusBar
import com.mooc.ppjoke.model.Destination
import com.mooc.ppjoke.model.User
import com.mooc.ppjoke.ui.login.UserManager
import com.mooc.ppjoke.utils.AppConfig
import com.mooc.ppjoke.utils.NavGraphBuilder
import com.mooc.ppjoke.view.AppBottomBar

/**
 * App 主页 入口
 *
 *
 * 1.底部导航栏 使用AppBottomBar 承载
 * 2.内容区域 使用WindowInsetsNavHostFragment 承载
 *
 *
 * 3.底部导航栏 和 内容区域的 切换联动 使用NavController驱动
 * 4.底部导航栏 按钮个数和 内容区域destination个数。由注解处理器NavProcessor来收集,生成assetsdestination.json。而后我们解析它。
 */
class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private var navController: NavController? = null
    private var navView: AppBottomBar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        //由于 启动时设置了 R.style.launcher 的windowBackground属性
        //势必要在进入主页后,把窗口背景清理掉
        setTheme(R.style.AppTheme)

        //启用沉浸式布局，白底黑字
        StatusBar.fitSystemBar(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navView = findViewById(R.id.nav_view)
        val fragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        navController = NavHostFragment.findNavController(fragment!!)
        NavGraphBuilder.build(this, navController, fragment.id)
        navView?.setOnNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        val destConfig = AppConfig.getDestConfig()
        val iterator: Iterator<Map.Entry<String, Destination>> = destConfig.entries.iterator()
        //遍历 target destination 是否需要登录拦截
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val value = entry.value
            if (value != null && !UserManager.get().isLogin && value.needLogin && value.id == menuItem.itemId) {
                UserManager.get().login(this).observe(this,object : Observer<User?> {
                    override fun onChanged(t: User?) {
                        run { navView!!.selectedItemId = menuItem.itemId }
                    }
                })
                return false
            }
        }
        navController!!.navigate(menuItem.itemId)
        return !TextUtils.isEmpty(menuItem.title)
    }

    override fun onBackPressed() {
//        boolean shouldIntercept = false;
//        int homeDestinationId = 0;
//
//        Fragment fragment = getSupportFragmentManager().getPrimaryNavigationFragment();
//        String tag = fragment.getTag();
//        int currentPageDestId = Integer.parseInt(tag);
//
//        HashMap<String, Destination> config = AppConfig.getDestConfig();
//        Iterator<Map.Entry<String, Destination>> iterator = config.entrySet().iterator();
//        while (iterator.hasNext()) {
//            Map.Entry<String, Destination> next = iterator.next();
//            Destination destination = next.getValue();
//            if (!destination.asStarter && destination.id == currentPageDestId) {
//                shouldIntercept = true;
//            }
//
//            if (destination.asStarter) {
//                homeDestinationId = destination.id;
//            }
//        }
//
//        if (shouldIntercept && homeDestinationId > 0) {
//            navView.setSelectedItemId(homeDestinationId);
//            return;
//        }
//        super.onBackPressed();

        //当前正在显示的页面destinationId
        val currentPageId = navController!!.currentDestination!!.id

        //APP页面路导航结构图  首页的destinationId
        val homeDestId = navController!!.graph.startDestination

        //如果当前正在显示的页面不是首页，而我们点击了返回键，则拦截。
        if (currentPageId != homeDestId) {
            navView!!.selectedItemId = homeDestId
            return
        }

        //否则 finish，此处不宜调用onBackPressed。因为navigation会操作回退栈,切换到之前显示的页面。
        finish()
    }
}