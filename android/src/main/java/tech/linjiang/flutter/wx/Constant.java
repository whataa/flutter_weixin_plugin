package tech.linjiang.flutter.wx;

public interface Constant {
    String ACTION_INIT = "init";
    String ACTION_AUTH = "auth";
    String ACTION_SHARE = "share";
    String ACTION_PAY = "pay";


    String SCOPE_AUTH = "snsapi_userinfo";

    String URL_TOKEN = "https://api.weixin.qq.com/sns/oauth2/access_token" +
            "?appid=%s" +
            "&secret=%s" +
            "&code=%s" +
            "&grant_type=authorization_code";

    String URL_USER = "https://api.weixin.qq.com/sns/userinfo" +
            "?access_token=%s" +
            "&openid=%s";


    int CODE_INSTALL = -1;
    int CODE_FAILED = -2;
    int CODE_DENIED = -3;
    int CODE_CANCEL = -4;

}


