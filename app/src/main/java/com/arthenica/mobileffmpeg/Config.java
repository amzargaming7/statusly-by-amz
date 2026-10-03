package com.arthenica.mobileffmpeg;

import android.os.Build;

/**
 * Minimal MobileFFmpeg 4.4-compatible Config bridge for the native
 * MobileFFmpeg library shipped with the PurePixel-derived project.
 */
public final class Config {
    public static final int RETURN_CODE_SUCCESS = 0;
    public static final int RETURN_CODE_CANCEL = 255;

    static {
        String lib = "mobileffmpeg";
        try {
            String[] abis = Build.SUPPORTED_ABIS;
            if (abis != null && abis.length > 0 && "armeabi-v7a".equals(abis[0])) {
                lib = "mobileffmpeg_armv7a_neon";
            }
        } catch (Throwable ignored) { }
        System.loadLibrary(lib);
    }

    private Config() { }

    private static native int nativeFFmpegExecute(long executionId, String[] arguments);
    private static native void nativeFFmpegCancel(long executionId);
    private static native String getNativeLastCommandOutput();
    private static native String getNativeFFmpegVersion();
    private static native String getNativeVersion();
    private static native String getNativeBuildDate();
    private static native void enableNativeRedirection();
    private static native void disableNativeRedirection();

    public static int execute(String command) {
        return nativeFFmpegExecute(0L, tokenize(command));
    }

    public static void cancel() {
        nativeFFmpegCancel(0L);
    }

    public static String getLastCommandOutput() {
        try {
            String s = getNativeLastCommandOutput();
            return s == null ? "" : s;
        } catch (Throwable t) {
            return t.toString();
        }
    }

    public static String getFFmpegVersion() {
        return getNativeFFmpegVersion();
    }

    public static String getVersion() {
        return getNativeVersion();
    }

    public static String getBuildDate() {
        return getNativeBuildDate();
    }

    public static void enableRedirection() {
        enableNativeRedirection();
    }

    public static void disableRedirection() {
        disableNativeRedirection();
    }

    /** Simple shell-like tokenizer supporting quoted paths and backslash escapes. */
    private static String[] tokenize(String command) {
        java.util.ArrayList<String> out = new java.util.ArrayList<>();
        StringBuilder cur = new StringBuilder();
        char quote = 0;
        boolean escape = false;
        for (int i = 0; i < command.length(); i++) {
            char c = command.charAt(i);
            if (escape) {
                cur.append(c);
                escape = false;
            } else if (c == '\\') {
                escape = true;
            } else if (quote != 0) {
                if (c == quote) quote = 0;
                else cur.append(c);
            } else if (c == '\'' || c == '"') {
                quote = c;
            } else if (Character.isWhitespace(c)) {
                if (cur.length() > 0) {
                    out.add(cur.toString());
                    cur.setLength(0);
                }
            } else {
                cur.append(c);
            }
        }
        if (escape) cur.append('\\');
        if (cur.length() > 0) out.add(cur.toString());
        return out.toArray(new String[0]);
    }
}
