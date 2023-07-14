package by.parser.parserostrovshop;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;


public class MainController {

    @FXML
    private TextField tfPath;

    @FXML
    void initialize() {

    }

    @FXML
    void btnChose(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Книга Excel", "*.xlsx"));
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            tfPath.setText(file.getAbsoluteFile().toString());
        } else {
            if (tfPath.getText().equals("")) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Не указан путь к файлу и его наименование.");
                alert.setTitle("Создание файла");
                alert.setHeaderText(null);
                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/image/ico.png"))));
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) event.consume();
            }
        }
    }

    @FXML
    void btnStart(ActionEvent event) {
        if (tfPath.getText().trim().equals("")) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Укажите путь к файлу и его имя!");
            alert.setTitle("Создание файла");
            alert.setHeaderText(null);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/image/ico.png"))));
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) event.consume();
        }else {
            try {
                Document document = Jsoup.connect("https://ostrov-shop.by/catalog/tovary-dlya-detey/gigiena-i-ukhod-za-detmi/podguzniki/")
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                        .timeout(5000)
                        .ignoreContentType(true)
                        .ignoreHttpErrors(true)
                        .referrer("https://www.google.com/").get();

                Elements elements = document.getElementsByClass("dinamic_info_wrapper");

            } catch (IOException e) {
               e.printStackTrace();
            }
        }
    }
}