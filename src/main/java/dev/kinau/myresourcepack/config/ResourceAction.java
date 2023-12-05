package dev.kinau.myresourcepack.config;

public enum ResourceAction {
    BLOCK,
    MERGE,
    PASS;

    public static String UNKNOWN_TRANSLATION_KEY = "resourceSelectionScreen.action.unknown";

    public String getTranslationKey(String tab) {
        return "resourceSelectionScreen." + tab + ".action." + name().toLowerCase();
    }
}
