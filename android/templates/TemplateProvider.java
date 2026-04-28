package {{PACKAGE_NAME}};

import com.lynx.tasm.provider.AbsTemplateProvider;
import com.lynx.tasm.resourceprovider.LynxResourceCallback;
import com.lynx.tasm.resourceprovider.LynxResourceRequest;
import com.lynx.tasm.resourceprovider.LynxResourceResponse;
import com.lynx.tasm.resourceprovider.LynxResourceResponse.ResponseState;
import com.lynx.tasm.resourceprovider.generic.LynxGenericResourceFetcher;
import com.lynx.tasm.resourceprovider.template.LynxTemplateResourceFetcher;
import com.lynx.tasm.resourceprovider.template.TemplateProviderResult;

public class TemplateProvider extends AbsTemplateProvider {
    private final android.content.Context context;
    public final LynxGenericResourceFetcher genericResourceFetcher;
    public final LynxTemplateResourceFetcher templateResourceFetcher;

    public TemplateProvider(android.content.Context context) {
        this.context = context.getApplicationContext();
        this.genericResourceFetcher = new LynxGenericResourceFetcher() {
            @Override
            public void fetchResource(LynxResourceRequest request, LynxResourceCallback<byte[]> callback) {
                loadBytesAsync(request.getUrl(), callback);
            }

            @Override
            public void fetchResourcePath(LynxResourceRequest request, LynxResourceCallback<String> callback) {
                callback.onResponse(LynxResourceResponse.onFailed(new java.io.IOException("Asset path lookup is not supported")));
            }
        };
        this.templateResourceFetcher = new LynxTemplateResourceFetcher() {
            @Override
            public void fetchTemplate(LynxResourceRequest request, LynxResourceCallback<TemplateProviderResult> callback) {
                loadBytesAsync(request.getUrl(), new LynxResourceCallback<byte[]>() {
                    @Override
                    public void onResponse(LynxResourceResponse<byte[]> response) {
                        if (response.getState() == ResponseState.SUCCESS && response.getData() != null) {
                            callback.onResponse(LynxResourceResponse.onSuccess(TemplateProviderResult.fromBinary(response.getData())));
                        } else {
                            Throwable error = response.getError() != null ? response.getError() : new java.io.IOException("Template load failed");
                            callback.onResponse(LynxResourceResponse.onFailed(error));
                        }
                    }
                });
            }

            @Override
            public void fetchSSRData(LynxResourceRequest request, LynxResourceCallback<byte[]> callback) {
                loadBytesAsync(request.getUrl(), callback);
            }
        };
    }

    @Override
    public void loadTemplate(String url, final Callback callback) {
        new Thread(() -> {
            try {
                callback.onSuccess(loadBytes(url));
            } catch (java.io.IOException e) {
                callback.onFailed(e.getMessage());
            }
        }).start();
    }

    private void loadBytesAsync(String url, LynxResourceCallback<byte[]> callback) {
        new Thread(() -> {
            try {
                callback.onResponse(LynxResourceResponse.onSuccess(loadBytes(url)));
            } catch (java.io.IOException e) {
                callback.onResponse(LynxResourceResponse.onFailed(e));
            }
        }).start();
    }

    private byte[] loadBytes(String url) throws java.io.IOException {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        try (java.io.InputStream is = context.getAssets().open(normalizeAssetPath(url))) {
            byte[] buf = new byte[1024];
            int n;
            while ((n = is.read(buf)) != -1) {
                baos.write(buf, 0, n);
            }
        }
        return baos.toByteArray();
    }

    private String normalizeAssetPath(String url) {
        if (url == null) return "";
        String s = url.trim();
        int query = s.indexOf('?');
        if (query >= 0) s = s.substring(0, query);
        while (s.startsWith("/")) s = s.substring(1);
        return java.nio.file.Paths.get(s).normalize().toString().replace('\\', '/');
    }
}
