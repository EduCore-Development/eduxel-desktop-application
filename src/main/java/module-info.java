module dev.educore.eduxel {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires java.prefs;
    requires java.desktop;

    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.materialdesign2;

    opens dev.educore.eduxel.app to javafx.graphics;
    opens dev.educore.eduxel.ui.main to javafx.fxml, javafx.base;
    opens dev.educore.eduxel.ui.school to javafx.fxml, javafx.base;
    opens dev.educore.eduxel.ui.inventory to javafx.fxml;
    opens dev.educore.eduxel.ui.settings to javafx.fxml;
    opens dev.educore.eduxel.ui.web to javafx.fxml;

    exports dev.educore.eduxel.app;
    exports dev.educore.eduxel.meta;
    exports dev.educore.eduxel.navigation;
    exports dev.educore.eduxel.domain.school;
    exports dev.educore.eduxel.domain.inventory;
}
