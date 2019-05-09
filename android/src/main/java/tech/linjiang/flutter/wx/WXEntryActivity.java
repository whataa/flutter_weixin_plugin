package tech.linjiang.flutter.wx;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;


public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WxMiddleware.get().handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        WxMiddleware.get().handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {// 微信发送请求到你的应用
        finish();
    }

    @Override
    public void onResp(BaseResp baseResp) {// 应用请求微信的响应结果
        WxMiddleware.get().handleResult(baseResp);
        finish();
    }
}
