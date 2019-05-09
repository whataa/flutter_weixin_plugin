package tech.linjiang.flutter.wx;

import android.text.TextUtils;

import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXMiniProgramObject;
import com.tencent.mm.opensdk.modelmsg.WXMusicObject;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.modelmsg.WXVideoObject;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;

import java.io.IOException;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

/*
        title   desc    thumb
文字      x       v       x
图片      x       x       v
音乐      v       v       v
视频      v       v       v
网页      v       v       v
程序      v       v       v

 */
class ImplShareHandler extends AbsHandler {

    public ImplShareHandler(MethodChannel.Result callback) {
        super(callback);
    }

    /**
     * see https://mp.weixin.qq.com/cgi-bin/announce?action=getannouncement&announce_id=11526372695t90Dn&version=&lang=zh_CN
     * <p>
     * 分享接口调用后，不再返回用户是否分享完成事件，即原先的cancel事件和success事件将统一为success事件。
     *
     * @param resp
     */
    @Override
    public void handleResult(BaseResp resp) {
        if (resp != null) {
            callbackSuccess(null);
        } else {
            callbackFailed(Constant.CODE_FAILED);
        }
    }

    @Override
    public boolean handleEvent(final IWXAPI api, final String key, final MethodCall call) {
        String shareType = call.argument("type");
        String title = call.argument("title");
        String description = call.argument("description");
        String thumb = call.argument("thumb");  // optional：支持http、本地路径
        String sceneType = call.argument("scene");

        WXMediaMessage.IMediaObject mediaType = null;
        switch (shareType) {
            case "Text":
                mediaType = genTextObject(call);
                break;
            case "Image":
                mediaType = genImageObject(call);
                break;
            case "Music":
                mediaType = genMusicObject(call);
                break;
            case "Video":
                mediaType = genVideoObject(call);
                break;
            case "Url":
                mediaType = genUrlObject(call);
                break;
            case "Program":
                mediaType = genProgramObject(call);
                break;
        }
        if (mediaType == null) {
            return false;
        }

        int scene = -1;
        switch (sceneType) {
            case "session":
                scene = SendMessageToWX.Req.WXSceneSession;
                break;
            case "timeline":
                scene = SendMessageToWX.Req.WXSceneTimeline;
                break;
            case "favorite":
                scene = SendMessageToWX.Req.WXSceneFavorite;
                break;
        }
        if (scene == -1) {
            callbackFailed(Constant.CODE_FAILED);
            return false;
        }

        WXMediaMessage message = new WXMediaMessage();
        message.mediaObject = mediaType;
        message.title = title;
        message.description = description;
        message.thumbData = getImageBytes(thumb);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.message = message;
        req.scene = scene;
        req.transaction = key;

        return api.sendReq(req);
    }


    private WXMediaMessage.IMediaObject genTextObject(MethodCall call) {
        WXTextObject textObject = new WXTextObject();
        textObject.text = call.argument("text");
        return textObject;
    }

    private WXMediaMessage.IMediaObject genImageObject(MethodCall call) {
        WXImageObject imageObject = new WXImageObject();
        String path = call.argument("imagePath");
        imageObject.imageData = getImageBytes(path);
        if (imageObject.imageData == null) {
            return null;
        }
        return imageObject;
    }

    private WXMediaMessage.IMediaObject genMusicObject(MethodCall call) {
        WXMusicObject musicObject = new WXMusicObject();
        musicObject.musicDataUrl = call.argument("musicDataUrl");
        musicObject.musicUrl = call.argument("musicUrl"); // optional
        return musicObject;
    }

    private WXMediaMessage.IMediaObject genVideoObject(MethodCall call) {
        WXVideoObject videoObject = new WXVideoObject();
        videoObject.videoUrl = call.argument("videoUrl");
        return videoObject;
    }

    private WXMediaMessage.IMediaObject genUrlObject(MethodCall call) {
        WXWebpageObject webPageObject = new WXWebpageObject();
        webPageObject.webpageUrl = call.argument("webPageUrl");
        return webPageObject;
    }

    private WXMediaMessage.IMediaObject genProgramObject(MethodCall call) {
        WXMiniProgramObject programObject = new WXMiniProgramObject();
        programObject.userName = call.argument("userName");
        programObject.path = call.argument("path");
        programObject.withShareTicket = call.argument("withShareTicket"); // optional
        programObject.webpageUrl = call.argument("webPageUrl"); // optional
        Integer type = call.argument("miniProgramType");
        programObject.miniprogramType = type == 1 ? WXMiniProgramObject.MINIPROGRAM_TYPE_TEST
                : type == 0 ? WXMiniProgramObject.MINIPROGRAM_TYPE_PREVIEW
                : WXMiniProgramObject.MINIPTOGRAM_TYPE_RELEASE;
        return programObject;
    }

    private byte[] getImageBytes(String thumb) {
        if (TextUtils.isEmpty(thumb)) {
            return null;
        }
        if (!(thumb.startsWith("http"))) {
            return Utils.getFileBytes(thumb);
        } else {
            try {
                return Utils.httpGet(thumb);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
