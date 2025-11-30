package dev.educore.eduxel.meta;

public final class BuildInfo {

    private final String date;
    private final String build;

    private BuildInfo(String date, String build) {
        this.date = date;
        this.build = build;
    }

    public static BuildInfo of(String date, String build) {
        return new BuildInfo(date, build);
    }

    @Override
    public String toString() {
        return "Build " + build + " (" + date + ")";
    }
}
