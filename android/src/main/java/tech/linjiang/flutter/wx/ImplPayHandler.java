package tech.linjiang.flutter.wx;

import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.modelpay.PayResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

class ImplPayHandler extends AbsHandler {


    public ImplPayHandler(MethodChannel.Result callback) {
        super(callback);
    }

    @Override
    public void handleResult(BaseResp resp) {
        if (resp == null) {
            callbackFailed(Constant.CODE_FAILED);
            return;
        }
        if (resp.errCode != BaseResp.ErrCode.ERR_OK) {
            int code;
            if (resp.errCode == BaseResp.ErrCode.ERR_USER_CANCEL) {
                code = Constant.CODE_CANCEL;
            } else {
                code = Constant.CODE_FAILED;
            }
            callbackFailed(code);
        } else {
            WxPayResult result = new WxPayResult();
            result.prepayId = ((PayResp)resp).prepayId;
            result.returnKey = ((PayResp)resp).returnKey;
            result.extData = ((PayResp)resp).extData;
            callbackSuccess(result.toMap());
        }
    }

    @Override
    public boolean handleEvent(IWXAPI api, String key, MethodCall call) {
        String appId = call.argument("appId"); // 此ID不一定必须为申请的appId
        String partnerId = call.argument("partnerId");
        String prepayId = call.argument("prepayId");
        String nonceStr = call.argument("nonceStr");
        String timeStamp = call.argument("timeStamp");
        String sign = call.argument("sign");
        String signType = call.argument("signType");// optional
        String extData = call.argument("extData");  // optional
        String packageValue = "Sign=WXPay";

        PayReq request = new PayReq();
        request.appId = appId;
        request.partnerId = partnerId;
        request.prepayId= prepayId;
        request.packageValue = packageValue;
        request.nonceStr= nonceStr;
        request.timeStamp= timeStamp;
        request.sign= sign;
        request.signType= signType;
        request.extData= extData;
        request.transaction = key;
        return api.sendReq(request);
    }

    public static class WxPayResult {
        public String prepayId;
        public String returnKey;
        public String extData;

        public Map toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("prepayId", prepayId);
            map.put("returnKey", returnKey);
            map.put("extData", extData);
            return map;
        }
    }
}
