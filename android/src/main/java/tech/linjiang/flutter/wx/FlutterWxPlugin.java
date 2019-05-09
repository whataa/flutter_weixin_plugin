package tech.linjiang.flutter.wx;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * FlutterWxPlugin
 */
public class FlutterWxPlugin implements MethodCallHandler {
    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "tech.linjiang.flutter.wx/plugin");
        channel.setMethodCallHandler(new FlutterWxPlugin());
        Utils.init(registrar.context());
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        WxMiddleware.get().handleEvent(call, result);
    }
}
