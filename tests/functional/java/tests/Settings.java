package tests;
//
//Copyright (c) Microsoft. All rights reserved.
//Licensed under the MIT license. See LICENSE.md file in the project root for full license information.
//

import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import com.microsoft.cognitiveservices.speech.SpeechFactory;

public class Settings {

    // Subscription
    public static String SpeechSubscriptionKey = "<<YOUR_SUBSCRIPTION_KEY>>";
    public static String SpeechRegion = "<<YOUR_REGION>>";

    public static String SpeechAuthorizationToken = "<<YOUR_AUTH_TOKEN>>";

    public static String LuisSubscriptionKey = "<<YOUR_LUIS_SUBSCRIPTION_KEY>>";
    public static String LuisRegion = "<<YOUR_LUIS_REGION>>";
    public static String LuisAppId = "<<YOUR_LUIS_APP_KEY>>";

    public static String WavFile = "whatstheweatherlike.wav";

    public static String Keyword = "Computer";
    public static String KeywordModel = "/data/keyword/kws.table";

    private static SpeechFactory factory;

    private static Boolean isSettingsInitialized = false;
    public static Object s_settingsClassLock;
    
    static {
        try {
            Class.forName("com.microsoft.cognitiveservices.speech.SpeechFactory");
        } catch (ClassNotFoundException e) {
            throw new UnsatisfiedLinkError(e.toString());
        }

        // prevent classgc from reclaiming the settings class, thus
        // throwing away any custom setting value..
        s_settingsClassLock = new Settings();
        
        LoadSettings();
    };

    public static void LoadSettings() {
        if(isSettingsInitialized)
            return;
        
        SpeechAuthorizationToken = System.getProperty("SpeechAuthorizationToken", SpeechAuthorizationToken);
        SpeechSubscriptionKey = System.getProperty("SpeechSubscriptionKey", SpeechSubscriptionKey);
        SpeechRegion = System.getProperty("SpeechRegion", SpeechRegion);

        LuisSubscriptionKey = System.getProperty("LuisSubscriptionKey", LuisSubscriptionKey);
        LuisRegion = System.getProperty("LuisRegion", LuisRegion);
        LuisAppId = System.getProperty("LuisAppId", LuisAppId);

        WavFile = System.getProperty("WaveFile", WavFile);

        Keyword = System.getProperty("Keyword", Keyword);
        KeywordModel = System.getProperty("KeywordModel", KeywordModel);

        isSettingsInitialized = true;
    }

    
    public static SpeechFactory getFactory() {
        if (factory == null) {
            try {
                factory = SpeechFactory.fromSubscription(SpeechSubscriptionKey, SpeechRegion);

                // PMA parameters
                factory.getParameters().set("DeviceGeometry", "Circular6+1");
                factory.getParameters().set("SelectedGeometry", "Circular3+1");
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                displayException(ex);
                return null;
            }
        }

        return factory;
    }

    public static void displayException(Exception ex) {
        System.out.println(ex.getMessage() + "\n");
        for (StackTraceElement item : ex.getStackTrace()) {
            System.out.println(item.toString());
        }
    }

    public static <T> void setOnTaskCompletedListener(final Future<T> task, final OnTaskCompletedListener<T> listener) {
        new FutureTask<Object>(() -> {
            listener.onCompleted(task.get());
            return true;
        }).run();
    }

    public interface OnTaskCompletedListener<T> {
        public void onCompleted(T taskResult);
    }
}
