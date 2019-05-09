package tech.linjiang.flutter.wx;

import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

abstract class AbsHandler {

    private MethodChannel.Result callback;

    public AbsHandler(MethodChannel.Result callback) {
        this.callback = callback;
    }

    protected void callbackSuccess(Object object) {
        callback.success(object);
    }

    protected void callbackFailed(int code) {
        callback.error(""+code, null, null);
    }

    public abstract void handleResult(BaseResp resp);

    // on workThread
    public abstract boolean handleEvent(IWXAPI api, String key, MethodCall call);
}
