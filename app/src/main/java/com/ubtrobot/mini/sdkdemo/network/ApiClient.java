package com.ubtrobot.mini.sdkdemo.network;

import com.ubtrobot.mini.sdkdemo.BuildConfig;

import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit springRetrofit;
    private static Retrofit pythonRetrofit;

    private static OkHttpClient getUnsafeOkHttpClient() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {}
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {}
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() { return new java.security.cert.X509Certificate[]{}; }
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier((hostname, session) -> true);

            // Add logging if needed
            HttpLoggingInterceptor log = new HttpLoggingInterceptor();
            log.level(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(log);

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Cho Spring API
    public static Retrofit getSpringInstance() {
        if (springRetrofit == null) {
            springRetrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.API_SPRING_PATH)
                    .client(getUnsafeOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return springRetrofit;
    }

    // Cho Python API
    public static Retrofit getPythonInstance() {
        if (pythonRetrofit == null) {
            pythonRetrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.API_PYTHON_PATH)
                    .client(getUnsafeOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return pythonRetrofit;
    }
}
