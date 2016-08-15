package net.tedwong.ping;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Takes a single photo on service start.
 */
public class PhotoTakingService {
    private static PhotoTakingService inst;
    static int frontCamId;
    Context context;

    public static PhotoTakingService instance(Context context) {
        if (inst == null) {
            inst = new PhotoTakingService(context.getApplicationContext());
            frontCamId = getFrontCameraId(context);
        }
        return inst;
    }

    private PhotoTakingService(Context context) {
        this.context = context;
    }

    @SuppressWarnings("Depreciation")
    static int getFrontCameraId(Context context) {
        if (Build.VERSION.SDK_INT < 22) {
            Camera.CameraInfo ci = new Camera.CameraInfo();
            for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                Camera.getCameraInfo(i, ci);
                if (ci.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) return i;
            }
        } else {
            try {
                CameraManager cManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
                for (int j = 0; j < cManager.getCameraIdList().length; j++) {
                    String[] cameraId = cManager.getCameraIdList();
                    Log.i("Camera", "Camera List " + Arrays.toString(cameraId));
                    CameraCharacteristics characteristics = cManager.getCameraCharacteristics(cameraId[j]);
                    int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                    if (cOrientation == CameraCharacteristics.LENS_FACING_FRONT)
                        return j;
                }
            } catch (CameraAccessException e) {     // If else prevents this error form happening
                e.printStackTrace();
            }
        }

        return -1; // No front-facing camera found
    }

    @SuppressWarnings("deprecation")
    public void takeBackPhoto(final String address) {
        final SurfaceView preview = new SurfaceView(context);
        SurfaceHolder holder = preview.getHolder();
        // deprecated setting, but required on Android versions prior to 3.0
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            //The preview must happen at or after this point or takePicture fails
            public void surfaceCreated(SurfaceHolder holder) {
                Camera camera = null;

                try {
                    camera = Camera.open();

                    try {
                        camera.setPreviewDisplay(holder);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    camera.startPreview();
                    camera.takePicture(null, null, new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] data, Camera camera) {
                            if (data != null) {
                                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                                if (bitmap != null) {
                                    File file = new File(Environment.getExternalStorageDirectory() + "/dirr");
                                    if (!file.isDirectory()) {
                                        file.mkdir();
                                    }
                                    file = new File(Environment.getExternalStorageDirectory() + "/dirr", System.currentTimeMillis() + ".jpg");
                                    try {
                                        FileOutputStream fileOutputStream = new FileOutputStream(file);
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                                        fileOutputStream.flush();
                                        fileOutputStream.close();
                                        sendMMS(file, address);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } catch (Exception exception) {
                                        exception.printStackTrace();
                                    }

                                }
                            }
                            camera.release();
                        }
                    });
                } catch (Exception e) {
                    if (camera != null) {
                        camera.release();
                    }
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }
        });

        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                1, 1, //Must be at least 1x1
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                0,
                //Don't know if this is a safe default
                PixelFormat.UNKNOWN);

        //Don't set the preview visibility to GONE or INVISIBLE
        wm.addView(preview, params);
    }


    @SuppressWarnings("deprecation")
    public void takeFrontPhoto(final String address) {
        final SurfaceView preview = new SurfaceView(context);
        SurfaceHolder holder = preview.getHolder();
        // deprecated setting, but required on Android versions prior to 3.0
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            //The preview must happen at or after this point or takePicture fails
            public void surfaceCreated(SurfaceHolder holder) {
                Camera camera = null;

                try {
                    camera = Camera.open(frontCamId);

                    try {
                        camera.setPreviewDisplay(holder);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    camera.startPreview();
                    camera.takePicture(null, null, new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] data, Camera camera) {
                            if (data != null) {
                                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                                if (bitmap != null) {
                                    File file = new File(Environment.getExternalStorageDirectory() + "/dirr");
                                    if (!file.isDirectory()) {
                                        file.mkdir();
                                    }
                                    file = new File(Environment.getExternalStorageDirectory() + "/dirr", System.currentTimeMillis() + ".jpg");
                                    try {
                                        FileOutputStream fileOutputStream = new FileOutputStream(file);
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                                        fileOutputStream.flush();
                                        fileOutputStream.close();
                                        sendMMS(file, address);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } catch (Exception exception) {
                                        exception.printStackTrace();
                                    }

                                }
                            }
                            camera.release();
                        }
                    });
                } catch (Exception e) {
                    if (camera != null) {
                        camera.release();
                    }
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }
        });

        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                1, 1, //Must be at least 1x1
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                0,
                //Don't know if this is a safe default
                PixelFormat.UNKNOWN);

        //Don't set the preview visibility to GONE or INVISIBLE
        wm.addView(preview, params);
    }

    private void sendMMS(File f, String address) {
        Uri uri = Uri.fromFile(f);
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setPackage("com.android.mms");
        sendIntent.setType("image/jpeg");
        sendIntent.putExtra("address", address);
        sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(sendIntent);
    }
}