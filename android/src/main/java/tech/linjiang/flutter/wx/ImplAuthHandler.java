package tech.linjiang.flutter.wx;

import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

class ImplAuthHandler extends AbsHandler {

    private String appID;
    private String appSecret;

    public ImplAuthHandler(MethodChannel.Result callback, String appID, String appSecret) {
        super(callback);
        this.appID = appID;
        this.appSecret = appSecret;
    }

    @Override
    public void handleResult(final BaseResp resp) {
        if (resp == null) {
            callbackFailed(Constant.CODE_FAILED);
            return;
        }
        if (resp.errCode != BaseResp.ErrCode.ERR_OK) {
            int code;
            if (resp.errCode == BaseResp.ErrCode.ERR_AUTH_DENIED) {
                code = Constant.CODE_DENIED;
            } else if (resp.errCode == BaseResp.ErrCode.ERR_USER_CANCEL) {
                code = Constant.CODE_CANCEL;
            } else {
                code = Constant.CODE_FAILED;
            }
            callbackFailed(code);
        }
        Utils.execute(new Runnable() {
            @Override
            public void run() {
                WxAuthResult result = getTokenAndUserInfo((SendAuth.Resp) resp);
                if (result != null) {
                    callbackSuccess(result.toMap());
                } else {
                    callbackFailed(Constant.CODE_FAILED);
                }
            }
        });
    }

    @Override
    public boolean handleEvent(IWXAPI api, String key, MethodCall call) {
        SendAuth.Req req = new SendAuth.Req();
        req.scope = Constant.SCOPE_AUTH;
        req.state = key;
        return api.sendReq(req);
    }

    private WxAuthResult getTokenAndUserInfo(SendAuth.Resp resp) {
        WxAuthResult result = new WxAuthResult();
        result.code = resp.code;
        result.state = resp.state;
        result.url = resp.url;
        result.lang = resp.lang;
        result.country = resp.country;
        try {
            byte[] originToken = Utils.httpGet(String.format(Constant.URL_TOKEN, appID, appSecret, result.code));
            String tokenJson = new String(originToken, Charset.forName("UTF-8"));
            JSONObject tokenObject = new JSONObject(tokenJson);
            if (tokenObject.has("errcode")) {
                throw new RuntimeException("Failed get access_token: " + Utils.getJsonStringSafely(tokenObject, "errmsg"));
            }

            result.accessToken = Utils.getJsonStringSafely(tokenObject, "access_token");
            result.expiresIn = Utils.getJsonIntSafely(tokenObject, "expires_in");
            result.refreshToken = Utils.getJsonStringSafely(tokenObject, "refresh_token");
            result.openId = Utils.getJsonStringSafely(tokenObject, "openid");
            result.scope = Utils.getJsonStringSafely(tokenObject, "scope");
            result.unionId = Utils.getJsonStringSafely(tokenObject, "unionid");

            byte[] originUser = Utils.httpGet(String.format(Constant.URL_USER, result.accessToken, result.openId));
            String userJson = new String(originUser, Charset.forName("UTF-8"));
            JSONObject userObject = new JSONObject(userJson);

            result.nickName = Utils.getJsonStringSafely(userObject, "nickname");
            result.sex = Utils.getJsonIntSafely(userObject, "sex");
            result.province = Utils.getJsonStringSafely(userObject, "province");
            result.city = Utils.getJsonStringSafely(userObject, "city");
            result.headImgUrl = Utils.getJsonStringSafely(userObject, "headimgurl");
            JSONArray userArray = Utils.getJsonArraySafely(userObject, "privilege");
            if (userArray != null) {
                int size = userArray.length();
                result.privilege = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    String value = Utils.getJsonStringSafely(userArray, i);
                    if (value != null) {
                        result.privilege.add(value);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = null;
        }
        return result;
    }

    public static class WxAuthResult {
        public String code;
        public String state;
        public String url;
        public String lang;

        public String accessToken;
        public int expiresIn;
        public String refreshToken;
        public String openId;
        public String scope;
        public String unionId;

        public String nickName;
        public int sex;
        public String province;
        public String city;
        public String country;
        public String headImgUrl;
        public List<String> privilege;

        public Map toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("code", code);
            map.put("state", state);
            map.put("url", url);
            map.put("lang", lang);

            map.put("accessToken", accessToken);
            map.put("expiresIn", expiresIn);
            map.put("refreshToken", refreshToken);
            map.put("openId", openId);
            map.put("scope", scope);
            map.put("unionId", unionId);

            map.put("nickName", nickName);
            map.put("sex", sex);
            map.put("province", province);
            map.put("city", city);
            map.put("country", country);
            map.put("headImgUrl", headImgUrl);
            map.put("privilege", privilege);
            return map;
        }
    }
}
