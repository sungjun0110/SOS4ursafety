package sungjun.sos4ursafety.ui.home;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.util.concurrent.TimeUnit;

public class SosService extends Service {
    private Thread sosThread;
    int num = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        sosThread = new Thread() {
            @Override
            public void run() {
                try {
                    while (num > 0) {
                        sos();
                    }
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        num = 99999;
        sosThread.start();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        num = 0;
    }

    private void sos() throws CameraAccessException {
        sosHelper(100); // Represents s
        sosHelper(300); // Represents o
        sosHelper(100); // Represents s
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sosHelper (int time) throws CameraAccessException {
        CameraManager flash = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        String cameraID = flash.getCameraIdList()[0];
        for (int i = 0; i < 3; i++) {
            flash.setTorchMode(cameraID, true);
            if (num <= 0) { flash.setTorchMode(cameraID, false); return;} // To turn off the flash immediately
            try {
                TimeUnit.MILLISECONDS.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (num <= 0) { flash.setTorchMode(cameraID, false); return;} // To turn off the flash immediately
            flash.setTorchMode(cameraID, false);
            if (num <= 0) { flash.setTorchMode(cameraID, false); return;} // To turn off the flash immediately
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (num <= 0) { flash.setTorchMode(cameraID, false); return;} // To turn off the flash immediately
        }
    }
}
