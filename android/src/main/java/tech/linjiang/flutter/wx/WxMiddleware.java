package tech.linjiang.flutter.wx;

import android.content.Intent;
import android.text.TextUtils;

import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelmsg.ShowMessageFromWX;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.util.HashMap;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

class WxMiddleware {

    private static final WxMiddleware INSTANCE = new WxMiddleware();
    private HashMap<String, AbsHandler> handlers = new HashMap<>();
    private IWXAPI api;
    private String appID;
    private String appSecret;

    private WxMiddleware() {

    }
    public static WxMiddleware get() {
        return INSTANCE;
    }

    public void handleEvent(final MethodCall call, MethodChannel.Result result) {
        final String type = call.method;
        final AbsHandler handler;
        if (TextUtils.equals(type, Constant.ACTION_INIT)) {
            handleInitEventAndResult(call, result);
            return;
        } else {
            if (!api.isWXAppInstalled()) {
                result.error(""+Constant.CODE_INSTALL, null, null);
                return;
            }
        }
        if (TextUtils.equals(type, Constant.ACTION_AUTH)) {
            handler = new ImplAuthHandler(result, appID, appSecret);
        } else if (TextUtils.equals(type, Constant.ACTION_SHARE)) {
            handler = new ImplShareHandler(result);
        } else if (TextUtils.equals(type, Constant.ACTION_PAY)) {
            handler = new ImplPayHandler(result);
        } else {
            handler = null;
        }
        if (handler != null) {
            final String key = genUniqueID(type);
            Utils.execute(new Runnable() {
                @Override
                public void run() {
                    boolean success = handler.handleEvent(api, key, call);
                    Utils.log("handleEvent: %s %s success=%b", type, key, success);
                    if (success) {
                        if (!(handler instanceof ImplShareHandler)) {
                            handlers.put(key, handler);
                        } else {
                            handler.handleResult(new ShowMessageFromWX.Resp());
                        }
                    } else {
                        handler.handleResult(null);
                    }
                }
            });

        }
    }

    public void handleResult(BaseResp baseResp) {
        String key;
        if (baseResp instanceof SendAuth.Resp) {
            key = ((SendAuth.Resp) baseResp).state;
        } else {
            key = baseResp.transaction;
        }
        Utils.log("handleResult: %s %s %s", key, baseResp.errCode, baseResp.errStr);

        AbsHandler handler = handlers.get(key);
        if (handler != null) {
            handler.handleResult(baseResp);
            handlers.remove(key);
        }
    }

    public void handleIntent(Intent intent, IWXAPIEventHandler handler) {
        api.handleIntent(intent, handler);
    }

    private void handleInitEventAndResult(MethodCall call, MethodChannel.Result result) {
        String appID = call.argument("appID");
        String appSecret = call.argument("appSecret");
        Utils.log("handleInitEventAndResult: appID=%s, appSecret=%s", appID, appSecret);
        if (!TextUtils.isEmpty(appID) && !TextUtils.isEmpty(appSecret)) {
            this.appID = appID;
            this.appSecret = appSecret;
            api = WXAPIFactory.createWXAPI(Utils.getContext(), appID, true);
            api.registerApp(appID);
            result.success(true);
        } else {
            result.error(""+Constant.CODE_FAILED, null, false);
        }
    }

    private String genUniqueID(String type) {
        return type + "#" + System.currentTimeMillis();
    }
}
