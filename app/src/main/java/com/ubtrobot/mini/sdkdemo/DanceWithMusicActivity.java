package com.ubtrobot.mini.sdkdemo;

import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.Voice;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ubtrobot.master.Master;
import com.ubtrobot.master.context.GlobalContext;
import com.ubtrobot.master.context.MasterContext;
import com.ubtrobot.mini.voice.MiniMediaPlayer;
import com.ubtrobot.mini.voice.VoicePool;
import com.ubtrobot.mini.voice.protos.VoiceProto;

public class DanceWithMusicActivity extends Activity {

    private MiniMediaPlayer miniPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dance_with_music);

        // Giả sử bạn có hàm hoặc cách lấy MasterContext và VoiceProto.Source
        MasterContext context = getMasterContext();
        VoiceProto.Source source = getVoiceProtoSource();

        // Đường dẫn file âm thanh bạn muốn phát
        String audioPath = "https://storage.googleapis.com/alphamini-music-configs/music/starboy_dance.wav?X-Goog-Algorithm=GOOG4-RSA-SHA256&X-Goog-Credential=alpha-mini-stt-service%40alphamini-465103.iam.gserviceaccount.com%2F20250809%2Fauto%2Fstorage%2Fgoog4_request&X-Goog-Date=20250809T082627Z&X-Goog-Expires=604800&X-Goog-SignedHeaders=host&X-Goog-Signature=3665cfb6e9aec45b71b0561ec34474824b28d379a1c2e5d2f4d9f86452cd5dcf5332453b62f382fcf4dc1a74c528c808e43b90de88c8f6f75efc97ee38c0f24bd0c29c00f80f15a059e9910bd132e3ae3c969a7bfb1e7337c490c334aa7f59fb2a8d631117cc3ac51481f6e5cb441fe71e2b2ed158e66ceca75fc63408332daa8cd735653ba5974b8ee1aa8c74da7d744f4647321c43542db8ccb0834fbb760b7727e358c34cbcc1e3224a09091e62e0742dd124ce33426dc9e52381b4ef7af94aef71bbcd7ec4db6acfadcbcf026c1b3234f63922a13a321f5f069dd4332e93958a222dab780be216d6dce78f73dd7444368a25773b2746e12fb37765f2742e";

        // Gọi initPlayer với tham số đúng
        initPlayer(context, source, audioPath);
    }

    // Hàm lấy MasterContext - bạn cần thay bằng cách lấy thật trong SDK của bạn
    private MasterContext getMasterContext() {
        Master master = Master.get();
        return master.getGlobalContext();
    }

    // Hàm lấy VoiceProto.Source - bạn cần thay bằng cách lấy thật trong SDK của bạn
    private VoiceProto.Source getVoiceProtoSource() {
        VoiceProto.Source voiceProto = VoiceProto.Source.MUSIC;
        return voiceProto;
    }

    public void initPlayer(MasterContext context, VoiceProto.Source source, String audioPath) {
        try {
            miniPlayer = MiniMediaPlayer.create(context, source);

            miniPlayer.setDataSource(audioPath);

            miniPlayer.setOnPreparedListener(mp -> {
                Log.i("DanceWithMusic", "Media ready, start playing");
                mp.start();
            });

            miniPlayer.setOnCompletionListener(mp -> Log.i("DanceWithMusic", "Playback completed"));

            miniPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e("DanceWithMusic", "Error playing media: " + what + ", " + extra);
                return true; // lỗi đã xử lý
            });

            miniPlayer.prepareAsync();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Hàm tạm dừng phát
    public void pausePlayer() {
        if (miniPlayer != null && miniPlayer.isPlaying()) {
            miniPlayer.pause();
        }
    }

    // Hàm dừng phát
    public void stopPlayer() {
        if (miniPlayer != null) {
            miniPlayer.stop();
            miniPlayer.reset();
        }
    }

    // Giải phóng tài nguyên khi không dùng nữa
    public void releasePlayer() {
        if (miniPlayer != null) {
            miniPlayer.release();
            miniPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }
}
