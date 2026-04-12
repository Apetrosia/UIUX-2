package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.*;

public class MainApp extends Application {

    private VBox fieldsContainer;
    private TextArea logArea;

    private final List<TextField> fields = new ArrayList<>();
    private final List<CheckBox> checkBoxes = new ArrayList<>();
    private final Map<Integer, HBox> fieldMap = new HashMap<>();

    @Override
    public void start(Stage primaryStage) {

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        // ✅ СНАЧАЛА создаём лог
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(150);

        // Контейнер для полей
        fieldsContainer = new VBox(5);

        // Теперь можно безопасно вызывать addFieldWithNumber (там есть log)
        for (int i = 1; i <= 6; i++) {
            addFieldWithNumber(i);
        }

        Button scaleBtn = new Button("Масштаб");
        Button checkBoxBtn = new Button("Флажки");
        Button manageBtn = new Button("Управление полями");

        scaleBtn.setOnAction(e -> openScaleWindow(primaryStage));
        checkBoxBtn.setOnAction(e -> openCheckBoxWindow());
        manageBtn.setOnAction(e -> openManageWindow());

        HBox buttons = new HBox(10, scaleBtn, checkBoxBtn, manageBtn);

        root.getChildren().addAll(buttons, fieldsContainer, logArea);

        primaryStage.setScene(new Scene(root, 400, 500));
        primaryStage.setTitle("Главное окно");
        primaryStage.show();
    }

    // =========================
    // ДОБАВЛЕНИЕ ПОЛЯ ПО НОМЕРУ
    // =========================
    private void addFieldWithNumber(int number) {

        if (fieldMap.containsKey(number)) {
            showError("Поле с таким номером уже существует");
            return;
        }

        HBox box = new HBox(10);
        Label label = new Label(String.valueOf(number));
        TextField field = new TextField(String.valueOf(number));

        field.textProperty().addListener((obs, oldVal, newVal) -> {
            int index = fields.indexOf(field);
            if (index >= 0 && index < checkBoxes.size()) {
                checkBoxes.get(index).setText(newVal);
            }
            log("Изменено поле " + number + ": " + newVal);
        });

        box.getChildren().addAll(label, field);

        fields.add(field);
        fieldMap.put(number, box);
        fieldsContainer.getChildren().add(box);

        log("Добавлено поле " + number);
    }

    // =========================
    // УДАЛЕНИЕ ПОЛЯ
    // =========================
    private void removeField(int number) {
        if (!fieldMap.containsKey(number)) {
            showError("Поля с таким номером не существует");
            return;
        }

        HBox box = fieldMap.get(number);
        TextField field = (TextField) box.getChildren().get(1);

        fields.remove(field);
        fieldsContainer.getChildren().remove(box);
        fieldMap.remove(number);

        log("Удалено поле " + number);
    }

    // =========================
    // ВАЛИДАЦИЯ ЧИСЛА
    // =========================
    private Integer parsePositiveInt(String text) {
        try {
            int val = Integer.parseInt(text);
            if (val <= 0) return null;
            return val;
        } catch (Exception e) {
            return null;
        }
    }

    // =========================
    // ОКНО МАСШТАБА
    // =========================
    private void openScaleWindow(Stage mainStage) {
        Stage stage = new Stage();

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        TextField input = new TextField();
        Button apply = new Button("Применить");

        apply.setOnAction(e -> {
            try {
                double scale = Double.parseDouble(input.getText());
                if (scale <= 0) {
                    log("Некорректное число: " + scale);
                    return;
                }
                mainStage.setWidth(mainStage.getWidth() * scale);
                mainStage.setHeight(mainStage.getHeight() * scale);
                log("Масштаб изменён: " + scale);
            } catch (Exception ex) {
                showError("Некорректное число");
            }
        });

        root.getChildren().addAll(new Label("Множитель:"), input, apply);

        stage.setScene(new Scene(root, 200, 150));
        stage.setTitle("Масштаб");
        stage.show();
    }

    // =========================
    // ОКНО ФЛАЖКОВ
    // =========================
    private void openCheckBoxWindow() {
        Stage stage = new Stage();

        VBox root = new VBox(5);
        root.setPadding(new Insets(10));

        checkBoxes.clear();

        for (TextField field : fields) {
            int index = fields.indexOf(field);

            CheckBox cb = new CheckBox(field.getText());

            cb.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    field.setFont(Font.font("System", javafx.scene.text.FontWeight.BOLD, 12));
                } else {
                    field.setFont(Font.getDefault());
                }
                log("Флажок " + (index + 1) + ": " + newVal);
            });

            checkBoxes.add(cb);
            root.getChildren().add(cb);
        }

        stage.setScene(new Scene(root, 200, 250));
        stage.setTitle("Флажки");
        stage.show();
    }

    // =========================
    // ОКНО УПРАВЛЕНИЯ
    // =========================
    private void openManageWindow() {
        Stage stage = new Stage();

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        TextField input = new TextField();
        input.setPromptText("Номер");

        Button addBtn = new Button("Добавить");
        Button removeBtn = new Button("Удалить");

        addBtn.setOnAction(e -> {
            Integer number = parsePositiveInt(input.getText());
            if (number == null) {
                showError("Введите положительное целое число");
                return;
            }
            addFieldWithNumber(number);
        });

        removeBtn.setOnAction(e -> {
            Integer number = parsePositiveInt(input.getText());
            if (number == null) {
                showError("Введите положительное целое число");
                return;
            }
            removeField(number);
        });

        root.getChildren().addAll(input, addBtn, removeBtn);

        stage.setScene(new Scene(root, 200, 200));
        stage.setTitle("Управление");
        stage.show();
    }

    private void log(String text) {
        logArea.appendText(text + "\n");
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch();
    }
}