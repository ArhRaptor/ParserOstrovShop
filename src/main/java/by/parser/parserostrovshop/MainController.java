package by.parser.parserostrovshop;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Objects;
import java.util.Optional;


public class MainController {

    @FXML
    private TextField tfPath;

    @FXML
    private Label labelProgress;

    @FXML
    private ProgressBar progressBar;

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
            progressBar.setVisible(true);
            labelProgress.setVisible(true);

            ParseTask myTask = new ParseTask(tfPath.getText().trim());
            progressBar.progressProperty().bind(myTask.progressProperty());
            labelProgress.textProperty().bind(myTask.messageProperty());

            myTask.setOnSucceeded(myEvent ->{
                progressBar.setVisible(false);
                labelProgress.setVisible(false);

                Alert alert = new Alert(Alert.AlertType.WARNING, "Файл успешно сохранен по указанному пути: " + tfPath.getText().trim());
                alert.setTitle("Создание файла");
                alert.setHeaderText(null);
                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/image/ico.png"))));
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) myEvent.consume();
            });

            Thread thread = new Thread(myTask);
            thread.start();
        }
    }
}