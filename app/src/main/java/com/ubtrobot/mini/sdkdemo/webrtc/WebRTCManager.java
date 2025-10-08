package com.ubtrobot.mini.sdkdemo.webrtc;

import android.content.Context;
import android.util.Log;

import org.webrtc.*;

import java.util.ArrayList;

public class WebRTCManager {
    private static final String TAG = "WebRTCManager";

    private PeerConnectionFactory factory;
    private PeerConnection peerConnection;
    private EglBase rootEglBase;

    private VideoTrack localVideoTrack;
    private AudioTrack localAudioTrack;
    private SurfaceTextureHelper surfaceTextureHelper;
    private Camera2Capturer cameraCapturer;
    private VideoSource videoSource;
    private AudioSource audioSource;

    private SurfaceViewRenderer localView;

    public interface Callback {
        void onIceCandidate(IceCandidate candidate);
        void onLocalDescription(SessionDescription sdp);
    }

    private Callback callback;

    public WebRTCManager(Context context, SurfaceViewRenderer localView, Callback callback) {
        this.localView = localView;
        this.callback = callback;
        initPeerFactory(context);
        initSurface();
    }

    private void initPeerFactory(Context context) {
        PeerConnectionFactory.InitializationOptions options =
                PeerConnectionFactory.InitializationOptions.builder(context)
                        .createInitializationOptions();
        PeerConnectionFactory.initialize(options);

        rootEglBase = EglBase.create();

        DefaultVideoEncoderFactory encoderFactory =
                new DefaultVideoEncoderFactory(rootEglBase.getEglBaseContext(), true, true);
        DefaultVideoDecoderFactory decoderFactory =
                new DefaultVideoDecoderFactory(rootEglBase.getEglBaseContext());

        factory = PeerConnectionFactory.builder()
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .createPeerConnectionFactory();
    }

    private void initSurface() {
        localView.init(rootEglBase.getEglBaseContext(), null);
        localView.setZOrderMediaOverlay(false);
    }

    public void startLocalMedia(Context ctx) {
        Camera2Enumerator enumerator = new Camera2Enumerator(ctx);
        String[] devices = enumerator.getDeviceNames();
        if (devices.length == 0) {
            Log.e(TAG, "No camera found");
            return;
        }

        cameraCapturer = new Camera2Capturer(ctx, devices[0], null);
        surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", rootEglBase.getEglBaseContext());
        videoSource = factory.createVideoSource(false);
        cameraCapturer.initialize(surfaceTextureHelper, ctx, videoSource.getCapturerObserver());

        try { cameraCapturer.startCapture(640, 480, 30); } catch (Exception e) { e.printStackTrace(); }

        localVideoTrack = factory.createVideoTrack("ARDAMSv0", videoSource);
        localVideoTrack.addSink(localView);

        audioSource = factory.createAudioSource(new MediaConstraints());
        localAudioTrack = factory.createAudioTrack("ARDAMSa0", audioSource);
    }

    public void createPeerConnection() {
        PeerConnection.RTCConfiguration config = new PeerConnection.RTCConfiguration(new ArrayList<>());
        config.iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));

        peerConnection = factory.createPeerConnection(config, new SimplePeerObserver() {
            @Override
            public void onIceCandidate(IceCandidate candidate) {
                if (callback != null) callback.onIceCandidate(candidate);
            }
        });

        MediaStream stream = factory.createLocalMediaStream("ARDAMS");
        stream.addTrack(localVideoTrack);
        stream.addTrack(localAudioTrack);
        peerConnection.addStream(stream);
    }

    public void createOffer() {
        MediaConstraints constraints = new MediaConstraints();
        peerConnection.createOffer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sdp) {
                peerConnection.setLocalDescription(new SimpleSdpObserver(), sdp);
                if (callback != null) callback.onLocalDescription(sdp);
            }
        }, constraints);
    }

    public void setRemoteDescription(SessionDescription sdp) {
        peerConnection.setRemoteDescription(new SimpleSdpObserver(), sdp);
    }

    public void addIceCandidate(IceCandidate candidate) {
        peerConnection.addIceCandidate(candidate);
    }

    public void release() {
        try {
            if (cameraCapturer != null) {
                cameraCapturer.stopCapture();
                cameraCapturer.dispose();
            }
            if (surfaceTextureHelper != null) surfaceTextureHelper.dispose();
            if (videoSource != null) videoSource.dispose();
            if (audioSource != null) audioSource.dispose();
            if (peerConnection != null) {
                peerConnection.close();
                peerConnection.dispose();
            }
            if (factory != null) factory.dispose();
            if (localView != null) localView.release();
            if (rootEglBase != null) rootEglBase.release();
        } catch (Exception e) { e.printStackTrace(); }
    }
}
