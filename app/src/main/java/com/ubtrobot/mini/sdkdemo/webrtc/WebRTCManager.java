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
    private CameraVideoCapturer videoCapturer;
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
        localView.setMirror(false);
        localView.setEnableHardwareScaler(true);
    }

    public void startLocalMedia(Context ctx) {
        // Use CameraEnumerator instead of Camera2Enumerator
        CameraEnumerator enumerator = new Camera1Enumerator(true); // true for captureToTexture

        String[] deviceNames = enumerator.getDeviceNames();
        if (deviceNames.length == 0) {
            Log.e(TAG, "No camera found");
            return;
        }

        // Find front camera
        String cameraDeviceName = null;
        boolean isFrontFacing = false;

        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                cameraDeviceName = deviceName;
                isFrontFacing = true;
                break;
            }
        }

        // If no front camera found, use first available camera
        if (cameraDeviceName == null && deviceNames.length > 0) {
            cameraDeviceName = deviceNames[0];
            isFrontFacing = enumerator.isFrontFacing(cameraDeviceName);
        }

        if (cameraDeviceName == null) {
            Log.e(TAG, "No usable camera found");
            return;
        }

        // Set mirroring based on camera type
        if (localView != null) {
            localView.setMirror(isFrontFacing);
        }

        // Create camera capturer using Camera1 API
        videoCapturer = enumerator.createCapturer(cameraDeviceName, null);

        if (videoCapturer == null) {
            Log.e(TAG, "Failed to create camera capturer");
            return;
        }

        surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", rootEglBase.getEglBaseContext());
        videoSource = factory.createVideoSource(false);

        // Initialize the capturer
        videoCapturer.initialize(surfaceTextureHelper, ctx, videoSource.getCapturerObserver());

        // Start capture
        videoCapturer.startCapture(640, 480, 30);

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

        if (peerConnection != null) {
            MediaStream stream = factory.createLocalMediaStream("ARDAMS");
            if (localVideoTrack != null) {
                stream.addTrack(localVideoTrack);
            }
            if (localAudioTrack != null) {
                stream.addTrack(localAudioTrack);
            }
            peerConnection.addStream(stream);
        }
    }

    public void createOffer() {
        if (peerConnection == null) {
            Log.e(TAG, "PeerConnection is null, cannot create offer");
            return;
        }

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
        if (peerConnection != null) {
            peerConnection.setRemoteDescription(new SimpleSdpObserver(), sdp);
        }
    }

    public void addIceCandidate(IceCandidate candidate) {
        if (peerConnection != null) {
            peerConnection.addIceCandidate(candidate);
        }
    }

    public void release() {
        try {
            if (videoCapturer != null) {
                videoCapturer.stopCapture();
                videoCapturer.dispose();
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
        } catch (Exception e) {
            Log.e(TAG, "Error during release", e);
        }
    }
}