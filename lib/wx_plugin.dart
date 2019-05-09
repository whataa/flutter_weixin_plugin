import 'dart:async';

import 'package:flutter/services.dart';

///
/// 1. 使用任何api前保证已经调用了register；
/// 2. 通过 then 或者 catchError 来得到api调用结果；
/// 3. 调用失败时catchError返回的参数类型是PlatformException，其code为CODE_INSTALL、CODE_FAILED、CODE_DENIED、CODE_CANCEL；
/// 4. 如果分享失败，请查看com.tencent.mm.opensdk.modelmsg.WXMediaMessage#checkArgs()对参数限制；
/// 5. 由于微信的调整，无法得到分享的正确结果：https://mp.weixin.qq.com/cgi-bin/announce?action=getannouncement&announce_id=11526372695t90Dn&version=&lang=zh_CN
///
class WxPlugin {
  static const MethodChannel _channel =
      const MethodChannel('tech.linjiang.flutter.wx/plugin');
  static WxPlugin instance = WxPlugin._();

  WxPlugin._();

  factory WxPlugin() {
    return instance;
  }

  Future register(String appID, String appSecret) {
    return _channel
        .invokeMethod("init", {"appID": appID, "appSecret": appSecret});
  }

  Future<WxAuth> auth() {
    return _channel.invokeMethod("auth").then((value) {
      print("auth: $value");
      return WxAuth.fromJson(value);
    });
  }

  Future shareText(Scene scene, String text) {
    return _channel.invokeMethod("share", {
      "type": "Text",
      "scene": _getSceneParam(scene),
      "text": text,
      "description": text
    });
  }

  Future shareImage(Scene scene, String pathOrUrl) {
    return _channel.invokeMethod("share", {
      "type": "Image",
      "scene": _getSceneParam(scene),
      "imagePath": pathOrUrl,
      "thumb": pathOrUrl
    });
  }

  Future shareMusic(Scene scene, String musicDataUrl,
      {String musicUrl, String title, String desc, String thumb}) {
    return _channel.invokeMethod("share", {
      "type": "Music",
      "scene": _getSceneParam(scene),
      "musicDataUrl": musicDataUrl,
      "musicUrl": musicUrl,
      "title": title,
      "description": desc,
      "thumb": thumb
    });
  }

  Future shareVideo(Scene scene, String videoUrl,
      {String title, String desc, String thumb}) {
    return _channel.invokeMethod("share", {
      "type": "Video",
      "scene": _getSceneParam(scene),
      "videoUrl": videoUrl,
      "title": title,
      "description": desc,
      "thumb": thumb
    });
  }

  Future shareUrl(Scene scene, String webPageUrl,
      {String title, String desc, String thumb}) {
    return _channel.invokeMethod("share", {
      "type": "Url",
      "scene": _getSceneParam(scene),
      "webPageUrl": webPageUrl,
      "title": title,
      "description": desc,
      "thumb": thumb
    });
  }

  Future shareMiniProgram(Scene scene, String userName, String path,
      {bool withShareTicket = false,
      MiniProgram type = MiniProgram.RELEASE,
      String webPageUrl,
      String title,
      String desc,
      String thumb}) {
    return _channel.invokeMethod("share", {
      "type": "Program",
      "scene": _getSceneParam(scene),
      "userName": userName,
      "path": path,
      "miniProgramType":
          type == MiniProgram.PREVIEW ? 2 : type == MiniProgram.TEST ? 1 : 0,
      "withShareTicket": withShareTicket,
      "webPageUrl": webPageUrl,
      "title": title,
      "description": desc,
      "thumb": thumb
    });
  }

  Future<WxPay> pay(String appId, String partnerId, String prepayId,
      String nonceStr, String timeStamp, String sign,
      {String signType, String extData}) {
    return _channel.invokeMethod("pay", {
      "appId": appId,
      "partnerId": partnerId,
      "prepayId": prepayId,
      "nonceStr": nonceStr,
      "timeStamp": timeStamp,
      "sign": sign,
      "signType": signType,
      "extData": extData,
    }).then((value) => WxPay.fromJson(value));
  }

  String _getSceneParam(Scene scene) {
    switch (scene) {
      case Scene.SESSION:
        return "session";
      case Scene.TIMELINE:
        return "timeline";
      case Scene.FAVORITE:
        return "favorite";
    }
    return null;
  }
}

enum Scene { SESSION, TIMELINE, FAVORITE }
enum MiniProgram { RELEASE, TEST, PREVIEW }

const int CODE_INSTALL = -1;
const int CODE_FAILED = -2;
const int CODE_DENIED = -3;
const int CODE_CANCEL = -4;

class WxAuth {
  String code;
  String state;
  String url;
  String lang;

  String accessToken;
  int expiresIn;
  String refreshToken;
  String openId;
  String scope;
  String unionId;

  String nickName;
  int sex;
  String province;
  String city;
  String country;
  String headImgUrl;
  List<String> privilege;

  WxAuth();

  factory WxAuth.fromJson(Map<dynamic, dynamic> json) {
    return WxAuth()
      ..code = json["code"]
      ..state = json["state"]
      ..url = json["url"]
      ..lang = json["lang"]
      ..accessToken = json["accessToken"]
      ..expiresIn = json["expiresIn"]
      ..refreshToken = json["refreshToken"]
      ..openId = json["openId"]
      ..scope = json["scope"]
      ..unionId = json["unionId"]
      ..nickName = json["nickName"]
      ..sex = json["sex"]
      ..province = json["province"]
      ..city = json["city"]
      ..country = json["country"]
      ..headImgUrl = json["headImgUrl"]
      ..privilege = json["privilege"].cast<String>();
  }
}

class WxPay {
  String prepayId;
  String returnKey;
  String extData;

  WxPay();

  factory WxPay.fromJson(Map<dynamic, dynamic> json) {
    return WxPay()
      ..prepayId = json["prepayId"]
      ..returnKey = json["returnKey"]
      ..extData = json["extData"];
  }
}
