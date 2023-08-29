package by.parser.parserostrovshop;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

import static by.parser.parserostrovshop.Category.*;


public class MainController {

    @FXML
    private TextField tfPath;

    @FXML
    private Label labelProgress;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private CheckBox cbPampers;

    @FXML
    private CheckBox cbNappies;

    @FXML
    private CheckBox cbWipes;

    @FXML
    private Button btnStart;

    @FXML
    private Button btnChose;


    private final ArrayList<String> categories = new ArrayList<>();

    @FXML
    void initialize() {
        cbPampers.setOnAction(actionEvent -> {
            if (cbPampers.isSelected()) {
                categories.add(PAMPERS.code);
                categories.add(ADULT_PAMPERS.code);
                categories.add(PANTIES.code);
            } else {
                categories.remove(PAMPERS.code);
                categories.remove(ADULT_PAMPERS.code);
                categories.remove(PANTIES.code);
            }
        });

        cbNappies.setOnAction(actionEvent -> {
            if (cbNappies.isSelected()) {
                categories.add(NAPPIES.code);
            } else {
                categories.remove(NAPPIES.code);
            }
        });

        cbWipes.setOnAction(actionEvent -> {
            if (cbWipes.isSelected()) {
                categories.add(WIPES.code);
                categories.add(CHILDREN_WIPES.code);
                categories.add(TOILET_PAPER.code);
            } else {
                categories.remove(WIPES.code);
                categories.remove(CHILDREN_WIPES.code);
                categories.remove(TOILET_PAPER.code);
            }
        });
    }

    @FXML
    void btnChose(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Книга Excel", "*.xlsx"));
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            tfPath.setText(file.getAbsoluteFile().toString());
        } else {
            if (tfPath.getText().isEmpty()) {
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
        if (tfPath.getText().trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Укажите путь к файлу и его имя!");
            alert.setTitle("Создание файла");
            alert.setHeaderText(null);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/image/ico.png"))));
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) event.consume();
        } else {
            progressBar.setVisible(true);
            labelProgress.setVisible(true);

            if (!cbPampers.isSelected() && !cbNappies.isSelected() && !cbWipes.isSelected()){
                categories.clear();
                categories.add(PAMPERS.code);
                categories.add(PANTIES.code);
                categories.add(ADULT_PAMPERS.code);
                categories.add(WIPES.code);
                categories.add(CHILDREN_WIPES.code);
                categories.add(TOILET_PAPER.code);
                categories.add(NAPPIES.code);
            }

            ParseTask myTask = new ParseTask(tfPath.getText().trim(), categories);
            progressBar.progressProperty().bind(myTask.progressProperty());
            labelProgress.textProperty().bind(myTask.messageProperty());
            setDisable(true);

            myTask.setOnSucceeded(myEvent -> {
                progressBar.setVisible(false);
                labelProgress.setVisible(false);
                setDisable(false);

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

    private void setDisable(Boolean value){
        cbWipes.setDisable(value);
        cbNappies.setDisable(value);
        cbPampers.setDisable(value);
        btnChose.setDisable(value);
        btnStart.setDisable(value);
    }
}