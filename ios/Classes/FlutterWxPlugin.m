#import "FlutterWxPlugin.h"

@implementation FlutterWxPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"tech.linjiang.flutter.wx/plugin"
            binaryMessenger:[registrar messenger]];
  FlutterWxPlugin* instance = [[FlutterWxPlugin alloc] init];
  [registrar addMethodCallDelegate:instance channel:channel];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {

}

@end
