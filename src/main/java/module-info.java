module by.parser.parserostrovshop {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.jsoup;
    requires org.apache.poi.ooxml;


    opens by.parser.parserostrovshop to javafx.fxml;
    exports by.parser.parserostrovshop;
}