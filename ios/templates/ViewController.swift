import UIKit
import Lynx
import tamerinsets
import tamerrouter
#if canImport(tamernavigation)
import tamernavigation
#endif

/// Same instance for root and TamerNav stack spokes (`TamerNavHost.applySpokeBuilder`).
private enum TamerNavLynxRuntime {
    static let _warnOnce: Void = {
        NSLog("[TamerHeap] WARNING: Lynx does not share JS heap across LynxViews; module-singleton stores re-init per spoke. Use TamerStateSyncProvider from @tamer4lynx/tamer-router for cross-spoke continuity. See tamer-navigation README.")
    }()

    static let sharedGroup: LynxGroup = {
        let option = LynxGroupOption()
        option.enableJSGroupThread = true
        return LynxGroup(name: "TamerNav", with: option)
    }()

    private static var viewGroups: [String: LynxViewGroup] = [:]

    static func viewGroup(src: String, provider: LynxProvider) -> LynxViewGroup {
        let key = src.isEmpty ? "main.lynx.bundle" : src
        if let existing = viewGroups[key] {
            return existing
        }
        let group = LynxViewGroup(url: key, templateFetcher: provider)
        group.group = sharedGroup
        group.enableGenericResourceFetcher = .true
        group.config = LynxConfig(provider: provider)
        group.templateResourceFetcher = provider
        group.genericResourceFetcher = provider
        viewGroups[key] = group
        return group
    }

    static func configureBuilder(_ builder: LynxViewBuilder, src: String, provider: LynxProvider) {
        _ = _warnOnce
        let vg = viewGroup(src: src, provider: provider)
        builder.lynxViewGroup = vg
        builder.group = sharedGroup
        builder.enableGenericResourceFetcher = .true
        builder.config = LynxConfig(provider: provider)
        builder.templateResourceFetcher = provider
        builder.genericResourceFetcher = provider
        NSLog("[TamerHeap] configure src=%@ group=%p viewGroup=%p", src, Unmanaged.passUnretained(sharedGroup).toOpaque(), Unmanaged.passUnretained(vg).toOpaque())
    }
}

class ViewController: UIViewController {
  private var lynxView: LynxView?

  override func viewDidLoad() {
    super.viewDidLoad()
    view.backgroundColor = .black
    edgesForExtendedLayout = .all
    extendedLayoutIncludesOpaqueBars = true
    additionalSafeAreaInsets = .zero
    view.insetsLayoutMarginsFromSafeArea = false
    view.preservesSuperviewLayoutMargins = false
    viewRespectsSystemMinimumLayoutMargins = false
  }

  override func viewDidLayoutSubviews() {
    super.viewDidLayoutSubviews()
    guard view.bounds.width > 0, view.bounds.height > 0 else { return }
    if lynxView == nil {
      setupLynxView()
    } else {
      applyFullscreenLayout(to: lynxView!)
    }
  }

  override func viewSafeAreaInsetsDidChange() {
    super.viewSafeAreaInsetsDidChange()
    TamerInsetsModule.reRequestInsets()
  }

  override var preferredStatusBarStyle: UIStatusBarStyle { .lightContent }

  private func buildLynxView() -> LynxView {
    let bounds = view.bounds
    let lv = LynxView { builder in
      let provider = LynxProvider()
#if canImport(tamernavigation)
      TamerNavLynxRuntime.configureBuilder(builder, src: "main.lynx.bundle", provider: provider)
#else
      builder.enableGenericResourceFetcher = .true
      builder.config = LynxConfig(provider: provider)
      builder.templateResourceFetcher = provider
      builder.genericResourceFetcher = provider
#endif
      builder.screenSize = bounds.size
      builder.fontScale = 1.0
    }
    lv.autoresizingMask = [.flexibleWidth, .flexibleHeight]
    lv.insetsLayoutMarginsFromSafeArea = false
    lv.preservesSuperviewLayoutMargins = false
    applyFullscreenLayout(to: lv)
    return lv
  }

  private func setupLynxView() {
#if canImport(tamernavigation)
    TamerNavHost.configureSharedGroup(TamerNavLynxRuntime.sharedGroup)
    TamerNavHost.configureSpokeBuilder = { builder, src in
      let provider = LynxProvider()
      TamerNavLynxRuntime.configureBuilder(builder, src: src, provider: provider)
    }
#endif
    let lv = buildLynxView()
    view.addSubview(lv)
    TamerInsetsModule.attachHostView(lv)
    TamerRouterNativeModule.attachHostView(lv)
#if canImport(tamernavigation)
    TamerNavHost.attachRoot(lv, presenter: self)
#endif
    lv.loadTemplate(fromURL: "main.lynx.bundle", initData: Self.initialDataWithInsetsSnapshot())
    self.lynxView = lv
  }

  /// Wraps the current safe-area insets in a `LynxTemplateData` so the JS bundle's
  /// first React render reads real insets via `lynx.__initData.__tamerInsetsSnapshot`
  /// instead of starting at zero and snapping when `tamer-insets:change` arrives.
  private static func initialDataWithInsetsSnapshot() -> LynxTemplateData? {
    guard let snapshot = TamerInsetsModule.currentInsetsSnapshotJson() else { return nil }
    return LynxTemplateData(json: "{\"__tamerInsetsSnapshot\":\(snapshot)}")
  }

  private func applyFullscreenLayout(to lynxView: LynxView) {
    let bounds = view.bounds
    let size = bounds.size
    lynxView.frame = bounds
    lynxView.updateScreenMetrics(withWidth: size.width, height: size.height)
    lynxView.updateViewport(withPreferredLayoutWidth: size.width, preferredLayoutHeight: size.height, needLayout: true)
    lynxView.preferredLayoutWidth = size.width
    lynxView.preferredLayoutHeight = size.height
    lynxView.layoutWidthMode = .exact
    lynxView.layoutHeightMode = .exact
  }
}
