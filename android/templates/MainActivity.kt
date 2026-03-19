package {{PACKAGE_NAME}}

import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import com.lynx.tasm.LynxView
import com.lynx.tasm.LynxViewBuilder
import {{PACKAGE_NAME}}.generated.GeneratedLynxExtensions
import {{PACKAGE_NAME}}.generated.GeneratedActivityLifecycle

class MainActivity : AppCompatActivity() {
    private var lynxView: LynxView? = null
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) maybeClearFocusedInput(ev)
        return super.dispatchTouchEvent(ev)
    }

    private fun maybeClearFocusedInput(ev: MotionEvent) {
        val focused = currentFocus
        if (focused is EditText) {
            val loc = IntArray(2)
            focused.getLocationOnScreen(loc)
            val x = ev.rawX.toInt()
            val y = ev.rawY.toInt()
            if (x < loc[0] || x > loc[0] + focused.width || y < loc[1] || y > loc[1] + focused.height) {
                focused.clearFocus()
                (getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager)
                    ?.hideSoftInputFromWindow(focused.windowToken, 0)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        GeneratedActivityLifecycle.onBackPressed { consumed ->
            if (!consumed) {
                runOnUiThread { super.onBackPressed() }
            }
        }
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
        viewBuilder.setTemplateProvider(TemplateProvider(this))
        GeneratedLynxExtensions.configureViewBuilder(viewBuilder)
        return viewBuilder.build(this)
    }
}
