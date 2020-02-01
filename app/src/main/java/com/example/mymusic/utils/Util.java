package com.example.mymusic.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    public static String getProperty(String key,Context context) throws IOException {
        Properties properties = new Properties();;
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = assetManager.open("config.properties");
        properties.load(inputStream);
        return properties.getProperty(key);

    }

    public static String buildUrl(String endpoint, Context context) {

        String URL = "http://";

        // get api url path from config file
        try {
            String api_host = getProperty("api_host", context);
            String api_port = Util.getProperty("api_port", context);
            String api_endpoint = Util.getProperty("api_endpoint", context);

            URL += api_host + ":" + api_port + api_endpoint + endpoint;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return URL;
    }

    public static String getAPI_KEY(Context context) {

        String API_KEY = "";

        try {
            API_KEY = Util.getProperty("api_key", context);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return API_KEY;
    }

    public static boolean validateUrl(String url) {
        Pattern p = Pattern.compile("(?:[https\\:\\/\\/])|(?:(?:http\\:\\/\\/))");
        Matcher m = p.matcher(url);
        return m.find();
    }

    public static boolean validateMp3(String filename) {
        Pattern p = Pattern.compile(".*\\.mp3$");
        Matcher m = p.matcher(filename);
        return m.matches();
    }
}