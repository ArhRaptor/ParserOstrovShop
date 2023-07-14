module by.parser.parserostrovshop {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.jsoup;


    opens by.parser.parserostrovshop to javafx.fxml;
    exports by.parser.parserostrovshop;
}