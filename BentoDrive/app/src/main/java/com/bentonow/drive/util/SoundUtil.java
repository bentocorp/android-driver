package com.bentonow.drive.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import com.bentonow.drive.Application;


/**
 * Created by APPSORAMA on 07/12/15.
 */
public class SoundUtil {

	/*public static void playSendEffect(){
        try{
			AssetFileDescriptor afd = Application.getInstance().getResources().openRawResourceFd(R.raw.swipe_send);
			if (afd != null) {
				MediaPlayer mediaPlayer = new MediaPlayer();
				mediaPlayer.reset();
				mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
				mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
				mediaPlayer.prepare();
				mediaPlayer.start();
				afd.close();
			}
		}catch(Exception ex){
			DebugUtils.logError("PlaySoundEffect", ex);
		}
	}*/

    public static void playNotificationSound(Uri uriNotification) {
        try {
            if (!isMusicPlaying()) {
                Ringtone r = RingtoneManager.getRingtone(Application.getInstance(), uriNotification);
                r.play();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isMusicPlaying() {
        AudioManager manager = (AudioManager) Application.getInstance().getSystemService(Context.AUDIO_SERVICE);
        return manager.isMusicActive();
    }
}
