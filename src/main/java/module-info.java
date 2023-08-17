module cn.sheyifan.fxpdfs {
    requires java.instrument;

    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires log4j;
    requires org.apache.pdfbox;
    requires poi;
    requires poi.ooxml;
    requires org.apache.tika.core;
    requires junit;
    requires com.jfoenix;
    requires java.desktop;
    requires kotlin.stdlib;

    exports cn.sheyifan;
}