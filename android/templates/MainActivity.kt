package {{PACKAGE_NAME}}

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import com.lynx.tasm.LynxBooleanOption
import com.lynx.tasm.LynxGroup
import com.lynx.tasm.LynxView
import com.lynx.tasm.LynxViewBuilder
import com.nanofuxion.tamernavigation.stack.TamerNavHost
import {{PACKAGE_NAME}}.generated.GeneratedLynxExtensions
import {{PACKAGE_NAME}}.generated.GeneratedActivityLifecycle

class MainActivity : AppCompatActivity() {
    private val tamerNavSharedLynxGroup: LynxGroup by lazy {
        LynxGroup.LynxGroupBuilder()
            .setGroupName("TamerNav")
            .setID("tamer-nav-shared")
            .build()
    }

    private var lynxView: LynxView? = null
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private val backCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            GeneratedActivityLifecycle.onBackPressed { consumed ->
                if (!consumed) {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TamerNavHost.spokeBuilder = { ctx ->
            val vb = LynxViewBuilder()
            vb.setLynxGroup(tamerNavSharedLynxGroup)
            val provider = TemplateProvider(ctx)
            vb.setTemplateProvider(provider)
            vb.setEnableGenericResourceFetcher(LynxBooleanOption.TRUE)
            vb.setTemplateResourceFetcher(provider.templateResourceFetcher)
            vb.setGenericResourceFetcher(provider.genericResourceFetcher)
            GeneratedLynxExtensions.configureViewBuilder(vb)
            vb.build(ctx)
        }
        GeneratedActivityLifecycle.onCreate(intent)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        lynxView = buildLynxView()
        setContentView(lynxView)
        ViewCompat.setOnApplyWindowInsetsListener(lynxView!!) { view, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            view.updatePadding(bottom = if (imeVisible) imeHeight else 0)
            insets
        }
        GeneratedActivityLifecycle.onViewAttached(lynxView)
        GeneratedLynxExtensions.onHostViewChanged(lynxView)
        lynxView?.renderTemplateUrl("main.lynx.bundle", "")
        GeneratedActivityLifecycle.onCreateDelayed(handler)
        onBackPressedDispatcher.addCallback(this, backCallback)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        GeneratedActivityLifecycle.onWindowFocusChanged(hasFocus)
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        GeneratedActivityLifecycle.onNewIntent(intent)
    }

    override fun onPause() {
        super.onPause()
        GeneratedActivityLifecycle.onPause()
    }

    override fun onResume() {
        super.onResume()
        GeneratedActivityLifecycle.onResume()
    }

    override fun onDestroy() {
        GeneratedActivityLifecycle.onViewDetached()
        GeneratedLynxExtensions.onHostViewChanged(null)
        lynxView?.destroy()
        lynxView = null
        super.onDestroy()
    }

    private fun buildLynxView(): LynxView {
        val viewBuilder = LynxViewBuilder()
        viewBuilder.setLynxGroup(tamerNavSharedLynxGroup)
        val provider = TemplateProvider(this)
        viewBuilder.setTemplateProvider(provider)
        viewBuilder.setEnableGenericResourceFetcher(LynxBooleanOption.TRUE)
        viewBuilder.setTemplateResourceFetcher(provider.templateResourceFetcher)
        viewBuilder.setGenericResourceFetcher(provider.genericResourceFetcher)
        GeneratedLynxExtensions.configureViewBuilder(viewBuilder)
        return viewBuilder.build(this)
    }
}
