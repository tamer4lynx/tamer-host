package {{PACKAGE_NAME}};

import com.lynx.tasm.provider.AbsTemplateProvider;

public class TemplateProvider extends AbsTemplateProvider {
    private final android.content.Context context;

    public TemplateProvider(android.content.Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public void loadTemplate(String url, final Callback callback) {
        new Thread(() -> {
            try {
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                try (java.io.InputStream is = context.getAssets().open(url)) {
                    byte[] buf = new byte[1024];
                    int n;
                    while ((n = is.read(buf)) != -1) {
                        baos.write(buf, 0, n);
                    }
                }
                callback.onSuccess(baos.toByteArray());
            } catch (java.io.IOException e) {
                callback.onFailed(e.getMessage());
            }
        }).start();
    }
}
