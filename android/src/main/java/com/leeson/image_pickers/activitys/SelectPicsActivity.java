package com.leeson.image_pickers.activitys;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import com.leeson.image_pickers.AppPath;
import com.leeson.image_pickers.R;
import com.leeson.image_pickers.utils.CommonUtils;
import com.leeson.image_pickers.utils.GlideEngine;
import com.leeson.image_pickers.utils.ImageCompressEngine;
import com.leeson.image_pickers.utils.ImageCropEngine;
import com.leeson.image_pickers.utils.MeSandboxFileEngine;
import com.leeson.image_pickers.utils.PictureStyleUtil;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.config.SelectModeConfig;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;
import com.luck.picture.lib.style.PictureSelectorStyle;
import com.luck.picture.lib.style.SelectMainStyle;
import com.luck.picture.lib.style.TitleBarStyle;
import com.luck.picture.lib.utils.StyleUtils;
import com.yalantis.ucrop.UCrop;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by lisen on 2018-09-11.
 * 只选择多张图片，
 *
 * @author lisen < 453354858@qq.com >
 */
@SuppressWarnings("all")
public class SelectPicsActivity extends BaseActivity {

    private static final int WRITE_SDCARD = 101;

    public static final String GALLERY_MODE = "GALLERY_MODE";
    public static final String UI_COLOR = "UI_COLOR";
    public static final String SHOW_GIF = "SHOW_GIF";
    public static final String SHOW_CAMERA = "SHOW_CAMERA";
    public static final String ENABLE_CROP = "ENABLE_CROP";
    public static final String WIDTH = "WIDTH";
    public static final String HEIGHT = "HEIGHT";
    public static final String COMPRESS_SIZE = "COMPRESS_SIZE";

    public static final String SELECT_COUNT = "SELECT_COUNT";//可选择的数量

    public static final String COMPRESS_PATHS = "COMPRESS_PATHS";//压缩的画
    public static final String CAMERA_MIME_TYPE = "CAMERA_MIME_TYPE";//直接调用拍照或拍视频时有效
    public static final String VIDEO_RECORD_MAX_SECOND = "VIDEO_RECORD_MAX_SECOND";//录制视频最大时间（秒）
    public static final String VIDEO_RECORD_MIN_SECOND = "VIDEO_RECORD_MIN_SECOND";//录制视频最最小时间（秒）
    public static final String VIDEO_SELECT_MAX_SECOND = "VIDEO_SELECT_MAX_SECOND";//选择视频时视频最大时间（秒）
    public static final String VIDEO_SELECT_MIN_SECOND = "VIDEO_SELECT_MIN_SECOND";//选择视频时视频最小时间（秒）
    private Number compressSize;
    private String mode;
    private Map<String, Number> uiColor;
    private Number selectCount;
    private boolean showGif;
    private boolean showCamera;
    private boolean enableCrop;
    private Number width;
    private Number height;
    private String mimeType;

    private Number videoRecordMaxSecond;
    private Number videoRecordMinSecond;
    private Number videoSelectMaxSecond;
    private Number videoSelectMinSecond;

    @Override
    public void onCreate(@androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_pics);
        mode = getIntent().getStringExtra(GALLERY_MODE);
        uiColor = (Map<String, Number>) getIntent().getSerializableExtra(UI_COLOR);

        selectCount = getIntent().getIntExtra(SELECT_COUNT, 9);
        showGif = getIntent().getBooleanExtra(SHOW_GIF, true);
        showCamera = getIntent().getBooleanExtra(SHOW_CAMERA, false);
        enableCrop = getIntent().getBooleanExtra(ENABLE_CROP, false);
        width = getIntent().getIntExtra(WIDTH, 1);
        height = getIntent().getIntExtra(HEIGHT, 1);
        compressSize = getIntent().getIntExtra(COMPRESS_SIZE, 500);
        mimeType = getIntent().getStringExtra(CAMERA_MIME_TYPE);

        videoRecordMaxSecond = getIntent().getIntExtra(VIDEO_RECORD_MAX_SECOND, 120);
        videoRecordMinSecond = getIntent().getIntExtra(VIDEO_RECORD_MIN_SECOND, 1);
        videoSelectMaxSecond = getIntent().getIntExtra(VIDEO_SELECT_MAX_SECOND, 120);
        videoSelectMinSecond = getIntent().getIntExtra(VIDEO_SELECT_MIN_SECOND, 1);
        Log.e("TAGTAG", "videoRecordMaxSecond  "+videoRecordMaxSecond);
        Log.e("TAGTAG", "videoRecordMinSecond  "+videoRecordMinSecond);
        Log.e("TAGTAG", "videoSelectMaxSecond  "+videoSelectMaxSecond);
        Log.e("TAGTAG", "videoSelectMinSecond  "+videoSelectMinSecond);
        startSel();
    }

    private UCrop.Options buildOptions(PictureSelectorStyle selectorStyle) {
        UCrop.Options options = new UCrop.Options();
        if (selectorStyle != null && selectorStyle.getSelectMainStyle().getStatusBarColor() != 0) {
            SelectMainStyle mainStyle = selectorStyle.getSelectMainStyle();
            boolean isDarkStatusBarBlack = mainStyle.isDarkStatusBarBlack();
            int statusBarColor = mainStyle.getStatusBarColor();
            options.isDarkStatusBarBlack(isDarkStatusBarBlack);
            options.setSkipCropMimeType(new String[]{PictureMimeType.ofGIF(), PictureMimeType.ofWEBP()});
            if (StyleUtils.checkStyleValidity(statusBarColor)) {
                options.setStatusBarColor(statusBarColor);
                options.setToolbarColor(statusBarColor);
            }
            TitleBarStyle titleBarStyle = selectorStyle.getTitleBarStyle();
            if (StyleUtils.checkStyleValidity(titleBarStyle.getTitleTextColor())) {
                options.setToolbarWidgetColor(titleBarStyle.getTitleTextColor());
            }
        }
        return options;
    }

    private void startSel() {
        PictureStyleUtil pictureStyleUtil = new PictureStyleUtil(this);
        pictureStyleUtil.setStyle(uiColor);
        PictureSelectorStyle selectorStyle = pictureStyleUtil.getSelectorStyle();
        //添加图片
        PictureSelector pictureSelector = PictureSelector.create(this);
        if (mimeType != null) {
            //直接调用拍照或拍视频时
            PictureSelector.create(this).openCamera("photo".equals(mimeType) ? SelectMimeType.ofImage() : SelectMimeType.ofVideo())
                    .setRecordVideoMaxSecond(videoRecordMaxSecond.intValue())
                    .setRecordVideoMinSecond(videoRecordMinSecond.intValue())
                    .setSelectMaxDurationSecond(videoRecordMaxSecond.intValue())
                    .setSelectMinDurationSecond(videoRecordMinSecond.intValue())
                    .setOutputCameraDir(new AppPath(this).getAppVideoDirPath())
                    .setCropEngine((selectCount.intValue() == 1 && enableCrop) ?
                            new ImageCropEngine(this, buildOptions(selectorStyle), width.intValue(), height.intValue()) : null)
                    .setCompressEngine(new ImageCompressEngine(compressSize.intValue()))
                    ./*setCameraInterceptListener(new OnCameraInterceptListener() {
                @Override
                public void openCamera(Fragment fragment, int cameraMode, int requestCode) {
                    //自定义相机
                    Log.e("TAG", "openCamera: 自定义相机" );
                }
            }).*/setSandboxFileEngine(new MeSandboxFileEngine()).forResult(new OnResultCallbackListener<LocalMedia>() {
                @Override
                public void onResult(ArrayList<LocalMedia> result) {
                    Log.e("TAG", "onResult: " );
                    handlerResult(result);
                }

                @Override
                public void onCancel() {
                    Log.e("TAG", "onCancelonCancelonCancel: " );
                    Intent intent = new Intent();
                    intent.putExtra(COMPRESS_PATHS, new ArrayList<>());
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
        } else {

            int selectMimeType = SelectMimeType.ofImage();
            if("image".equals(mode)){
                selectMimeType = SelectMimeType.ofImage();
            }else if ("video".equals(mode)){
                selectMimeType = SelectMimeType.ofVideo();
            }else{
                selectMimeType = SelectMimeType.ofAll();
            }
            Log.e("TAG", "startSel: "+selectMimeType+" == "+mode );
            PictureSelector.create(this).openGallery(selectMimeType)
                    .setImageEngine(GlideEngine.createGlideEngine())
                    .setSelectorUIStyle(pictureStyleUtil.getSelectorStyle())
                    .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                    .setRecordVideoMaxSecond(videoRecordMaxSecond.intValue())
                    .setRecordVideoMinSecond(videoRecordMinSecond.intValue())
                    .setOutputCameraDir(new AppPath(this).getAppVideoDirPath())
                    .setCropEngine((selectCount.intValue() == 1 && enableCrop) ?
                            new ImageCropEngine(this, buildOptions(selectorStyle), width.intValue(), height.intValue()) : null)
                    .setCompressEngine(new ImageCompressEngine(compressSize.intValue()))
                    .setSandboxFileEngine(new MeSandboxFileEngine())
                    .isDisplayCamera(showCamera)
                    .isGif(showGif)
                    .setSelectMaxDurationSecond(videoSelectMaxSecond.intValue())
                    .setSelectMinDurationSecond(videoSelectMinSecond.intValue())
                    .setFilterVideoMaxSecond(videoSelectMaxSecond.intValue())
                    .setFilterVideoMinSecond(videoSelectMinSecond.intValue())
                    .setMaxSelectNum(selectCount.intValue())
                    .setMaxVideoSelectNum(selectCount.intValue())
                    .isWithSelectVideoImage(true)
                    .setImageSpanCount(4)// 每行显示个数 int
                    .setSelectionMode(selectCount.intValue() == 1 ? SelectModeConfig.SINGLE : SelectModeConfig.MULTIPLE)// 多选 or 单选 PictureConfig.MULTIPLE or PictureConfig.SINGLE
                    .isDirectReturnSingle(true)
                    .setSkipCropMimeType(new String[]{PictureMimeType.ofGIF(), PictureMimeType.ofWEBP()})
                    .isPreviewImage(true)
                    .isPreviewVideo(true)
                    .forResult(new OnResultCallbackListener<LocalMedia>() {
                        @Override
                        public void onResult(ArrayList<LocalMedia> result) {
                            handlerResult(result);
                        }

                        @Override
                        public void onCancel() {
                            Intent intent = new Intent();
                            intent.putExtra(COMPRESS_PATHS, new ArrayList<>());
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    });
        }

    }


    private void handlerResult(ArrayList<LocalMedia> selectList) {
        List<Map<String, String>> paths = new ArrayList<>();
        for (int i = 0; i < selectList.size(); i++) {
            LocalMedia localMedia = selectList.get(i);

            if (localMedia.getMimeType().contains("image")){
                String path = localMedia.getAvailablePath();
                if (localMedia.isCut()) {
                    path = localMedia.getCutPath();
                }
                Map<String, String> map = new HashMap<>();
                map.put("thumbPath", path);
                map.put("path", path);
                paths.add(map);

            }else{
                if (localMedia.getAvailablePath() == null) {
                    break;
                }
                Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(localMedia.getAvailablePath(), MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
                String thumbPath = CommonUtils.saveBitmap(this, new AppPath(this).getAppImgDirPath(), bitmap);
                Map<String, String> map = new HashMap<>();
                map.put("thumbPath", thumbPath);
                map.put("path", localMedia.getAvailablePath());
                paths.add(map);
            }

        }
        Intent intent = new Intent();
        intent.putExtra(COMPRESS_PATHS, (Serializable) paths);
        setResult(RESULT_OK, intent);
        finish();
    }

}
