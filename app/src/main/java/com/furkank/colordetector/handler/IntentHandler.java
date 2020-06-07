package com.furkank.colordetector.handler;

import android.content.Context;
import android.content.Intent;

public class IntentHandler {

    /**
     * Opens a new intent and removes history
     *
     * @param context
     * @param targetClass
     */
    public static void open(Context context, Class targetClass) {
        openIntent(context, targetClass, true);
    }

    /**
     * Opens a new intent and removes or don't removes history according to your choice
     *
     * @param context
     * @param targetClass
     * @param clearHistory
     */
    public static void open(Context context, Class targetClass, boolean clearHistory) {
        openIntent(context, targetClass, clearHistory);
    }

    private static void openIntent(Context context, Class targetClass, boolean clearHistory) {
        Intent intent = new Intent(context, targetClass);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if (clearHistory) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }

        context.startActivity(intent);
    }
}
