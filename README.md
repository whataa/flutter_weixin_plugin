A lightweight flutter plugin for WeChat SDK.

## Dependencies

In your pubspec.yaml file:

```
dependencies:
  flutter_weixin_plugin: ^0.0.1
```

## Register

It's recommended to register in `runApp()` :

```
import 'package:flutter_weixin_plugin/wx_plugin.dart';

WxPlugin().register("your appId", "your appSecret");
```

## API

All APIs return `Future`, use `then` to get success callback and `catchError` to get failed callback.
When it fails, a `PlatformException` obj is returned, its `code` could be one of the following values:

```
CODE_INSTALL = -1; // WeChat is not installed.
CODE_FAILED = -2;  // failed.
CODE_DENIED = -3;  // user refuses auth.
CODE_CANCEL = -4;  // user clicks cancel.
```

#### 1. Auth(Login)

```
WxPlugin().auth()
      .then((value) {
        print("success: $value");
      }).catchError((value) {
        print("failed: value");
      });
```

Auth returns `WxAuth` object.

#### 2. Share

```
WxPlugin().shareText(...)
WxPlugin().shareImage(...)
WxPlugin().shareMusic(...)
WxPlugin().shareVideo(...)
WxPlugin().shareUrl(...)
WxPlugin().shareMiniProgram(...)
```
If share failed, please check `com.tencent.mm.opensdk.modelmsg.WXMediaMessage#checkArgs()`.

#### 3. Pay

```
WxPlugin().pay(...)
```

Pay returns `WxPay` object.

## TODO

- [ ] plugin for iOS

## LICENSE

Apache
