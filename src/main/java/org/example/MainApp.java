package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class MainApp extends Application {

    private VBox fieldsContainer;
    private TextArea logArea;

    private final List<TextField> fields = new ArrayList<>();
    private final List<CheckBox> checkBoxes = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        // Контейнер для полей
        fieldsContainer = new VBox(5);

        // Создаём 6 полей
        for (int i = 1; i <= 6; i++) {
            addField();
        }

        // Кнопки
        Button scaleBtn = new Button("Масштаб");
        Button checkBoxBtn = new Button("Флажки");
        Button manageBtn = new Button("Управление полями");

        scaleBtn.setOnAction(e -> openScaleWindow(primaryStage));
        checkBoxBtn.setOnAction(e -> openCheckBoxWindow());
        manageBtn.setOnAction(e -> openManageWindow());

        HBox buttons = new HBox(10, scaleBtn, checkBoxBtn, manageBtn);

        // Лог
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(150);

        root.getChildren().addAll(buttons, fieldsContainer, logArea);

        primaryStage.setScene(new Scene(root, 400, 500));
        primaryStage.setTitle("Главное окно");
        primaryStage.show();
    }

    // =========================
    // СОЗДАНИЕ ПОЛЯ
    // =========================
    private int addField() {
        HBox box = new HBox(10);

        int number = 1;
        while (true) {
            final int current = number;

            boolean exists = fields.stream().anyMatch(x -> {
                Label label = (Label) x.getParent().getChildrenUnmodifiable()
                        .stream()
                        .filter(s -> s instanceof Label)
                        .findFirst()
                        .orElse(null);

                if (label == null) return false;

                return Integer.parseInt(label.getText()) == current;
            });

            if (!exists) break;

            number++;
        }

        Label label = new Label(String.valueOf(number));
        TextField field = new TextField(String.valueOf(number));

        int index = fields.size();

        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (index < checkBoxes.size()) {
                checkBoxes.get(index).setText(newVal);
            }
            log("Изменено поле " + (index + 1) + ": " + newVal);
        });

        fields.add(field);

        box.getChildren().addAll(label, field);
        fieldsContainer.getChildren().add(box);

        return number;
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
                if (scale <= 0)
                {
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
        root.getChildren().clear();

        for (int i = 0; i < fields.size(); i++) {
            int index = i;

            CheckBox cb = new CheckBox(fields.get(i).getText());

            cb.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    fields.get(index).setFont(Font.font("System", javafx.scene.text.FontWeight.BOLD, 12));
                } else {
                    fields.get(index).setFont(Font.getDefault());
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

        addBtn.setOnAction(e -> log("Добавлено поле " + addField()));

        removeBtn.setOnAction(e -> {
            try {
                final int number = Integer.parseInt(input.getText());

                int index = -1;
                for (int i = 0; i < fields.size(); ++i)
                {
                    Label label = (Label) (fields.get(i).getParent().getChildrenUnmodifiable()
                            .stream()
                            .filter(s -> s instanceof Label)
                            .findFirst()
                            .orElse(null));

                    if (label != null && Integer.parseInt(label.getText()) == number)
                    {
                        index = i;
                        break;
                    }
                }

                if (index == -1) {
                    throw new Exception();
                }

                fields.remove(index);
                fieldsContainer.getChildren().remove(index);

                log("Удалено поле " + (number));

            } catch (Exception ex) {
                showError("Некорректный номер");
            }
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