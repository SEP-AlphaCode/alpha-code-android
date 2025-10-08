package com.ubtrobot.mini.sdkdemo.socket;

import android.util.Log;

import com.ubtrobot.mini.sdkdemo.socket.RobotRequestProto.RobotRequest;

public class ProtobufConverter {
    private static final String TAG = "ProtobufConverter";

    public static byte[] requestToProtoBytes(String type, int[] asrArray, byte[] image,
                                             java.util.Map<String, String> params, String speech) {
        try {
            RobotRequest.Builder builder = RobotRequest.newBuilder()
                    .setType(type);

            // Add ASR data if present
            if (asrArray != null) {
                for (int value : asrArray) {
                    builder.addAsr(value);
                }
            }

            // Add image data if present
            if (image != null) {
                builder.setImage(com.google.protobuf.ByteString.copyFrom(image));
            }

            // Add parameters if present
            if (params != null) {
                builder.putAllParams(params);
            }

            // Add speech if present
            if (speech != null) {
                builder.setSpeech(speech);
            }

            RobotRequest request = builder.build();
            return request.toByteArray();

        } catch (Exception e) {
            Log.e(TAG, "Error converting request to protobuf", e);
            return null;
        }
    }

    public static RobotRequest bytesToRequest(byte[] data) {
        try {
            return RobotRequest.parseFrom(data);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing protobuf data", e);
            return null;
        }
    }

    // Helper method to create a simple request with just type and params
    public static byte[] createSimpleRequest(String type, java.util.Map<String, String> params) {
        return requestToProtoBytes(type, null, null, params, null);
    }
}