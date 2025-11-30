package dev.educore.eduxel.meta;

public final class AppInfo {

    public static final String NAME = "Eduxel";
    public static final Version VERSION = Version.of(1, 0, 0);
    public static final BuildInfo BUILD = BuildInfo.of("2025-01-01", "001");
    public static final Environment ENV = Environment.DEV;

    public static String getWindowTitle() {
        return NAME + " " + VERSION + " • " + ENV.name();
    }
}
