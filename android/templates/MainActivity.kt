package {{PACKAGE_NAME}}

import android.content.Context
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
import com.lynx.tasm.group.ILynxViewGroup
import com.lynx.tasm.group.LynxViewGroupBuilder
import com.lynx.xelement.XElementBehaviors
import com.nanofuxion.tamerinsets.TamerInsetsModule
import com.nanofuxion.tamernavigation.stack.TamerNavHost
import {{PACKAGE_NAME}}.generated.GeneratedLynxExtensions
import {{PACKAGE_NAME}}.generated.GeneratedActivityLifecycle

private object TamerNavLynxRuntime {
    init {
        android.util.Log.w(
            "TamerHeap",
            "Lynx does not share JS heap across LynxViews; module-singleton stores re-init per spoke. Use TamerStateSyncProvider from @tamer4lynx/tamer-router for cross-spoke continuity. See tamer-navigation README.",
        )
    }

    val group: LynxGroup = LynxGroup.LynxGroupBuilder()
        .setGroupName("TamerNav")
        .setID(LynxGroup.SINGNLE_GROUP)
        .setEnableJSGroupThread(true)
        .build()

    private val viewGroups = LinkedHashMap<String, ILynxViewGroup>()

    @Synchronized
    fun viewGroup(context: Context, src: String): ILynxViewGroup {
        val key = src.ifBlank { "main.lynx.bundle" }
        return viewGroups.getOrPut(key) {
            val appContext = context.applicationContext ?: context
            val provider = TemplateProvider(appContext)
            val groupBuilder = LynxViewGroupBuilder()
                .setContext(appContext)
                .setUrl(key)
                .setLynxGroup(group)
                .addBehaviors(XElementBehaviors().create())
            groupBuilder.setEnableGenericResourceFetcher(LynxBooleanOption.TRUE)
            groupBuilder.setTemplateResourceFetcher(provider.templateResourceFetcher)
            groupBuilder.setGenericResourceFetcher(provider.genericResourceFetcher)
            groupBuilder.build()
        }
    }

    fun configureBuilder(context: Context, viewBuilder: LynxViewBuilder, src: String) {
        val provider = TemplateProvider(context)
        val vg = viewGroup(context, src)
        viewBuilder.setLynxViewGroup(vg)
        viewBuilder.setLynxGroup(group)
        viewBuilder.setTemplateProvider(provider)
        viewBuilder.setEnableGenericResourceFetcher(LynxBooleanOption.TRUE)
        viewBuilder.setTemplateResourceFetcher(provider.templateResourceFetcher)
        viewBuilder.setGenericResourceFetcher(provider.genericResourceFetcher)
        android.util.Log.i(
            "TamerHeap",
            "configure src=$src group=${System.identityHashCode(group)} viewGroup=${System.identityHashCode(vg)}",
        )
    }
}

class MainActivity : AppCompatActivity() {
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
        TamerNavHost.configureSharedLynxGroup(TamerNavLynxRuntime.group)
        TamerNavHost.sourceSpokeBuilder = { ctx, src ->
            val vb = LynxViewBuilder()
            TamerNavLynxRuntime.configureBuilder(ctx, vb, src)
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
        lynxView?.renderTemplateUrl("main.lynx.bundle", initialDataWithInsetsSnapshot())
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

    /** Wraps current safe-area insets in initData JSON so the JS bundle's first React
     * render reads real insets via `lynx.__initData.__tamerInsetsSnapshot` instead of
     * starting at zero and snapping when `tamer-insets:change` arrives. */
    private fun initialDataWithInsetsSnapshot(): String {
        val snapshot = TamerInsetsModule.currentInsetsSnapshotJson() ?: return ""
        return "{\"__tamerInsetsSnapshot\":$snapshot}"
    }

    private fun buildLynxView(): LynxView {
        val viewBuilder = LynxViewBuilder()
        TamerNavLynxRuntime.configureBuilder(this, viewBuilder, "main.lynx.bundle")
        GeneratedLynxExtensions.configureViewBuilder(viewBuilder)
        return viewBuilder.build(this)
    }
}
