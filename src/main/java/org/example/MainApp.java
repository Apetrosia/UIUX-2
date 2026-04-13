package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.*;

public class MainApp extends Application {
    private Stage primaryStage;

    private VBox fieldsContainer;
    private TextArea logArea;
    private VBox checkBoxContainer = null;

    private final Map<Integer, TextField> fields = new HashMap<>();
    private final Map<Integer, CheckBox> checkBoxes = new HashMap<>();

    double minWidth = 600 * 0.7;
    double minHeight = 500 * 0.7;
    double maxWidth = 600 * 1.2;
    double maxHeight = 500 * 1.2;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(150);

        fieldsContainer = new VBox(5);
        fieldsContainer.setPadding(new Insets(5, 10, 5, 10));

        ScrollPane scrollPane = new ScrollPane(fieldsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(250);

        for (int i = 1; i <= 6; i++) {
            createField(i, false);
        }

        Button scaleBtn = new Button("Масштаб");
        Button checkBoxBtn = new Button("Флажки");
        Button manageBtn = new Button("Управление полями");

        scaleBtn.setOnAction(e -> openScaleWindow(primaryStage));
        checkBoxBtn.setOnAction(e -> openCheckBoxWindow());
        manageBtn.setOnAction(e -> openManageWindow());

        HBox buttons = new HBox(10, scaleBtn, checkBoxBtn, manageBtn);

        root.getChildren().addAll(buttons, scrollPane, logArea);

        primaryStage.setScene(new Scene(root, 600, 500));
        primaryStage.setTitle("Синхронизация");
        primaryStage.show();

        primaryStage.setOnCloseRequest(e -> Platform.exit());
    }

    // СОЗДАНИЕ ПОЛЯ
    private void createField(int number, boolean withLog) {

        if (fields.containsKey(number)) {
            showError("Поле с таким номером уже существует");
            return;
        }

        Label label = new Label(String.valueOf(number));
        TextField field = new TextField(String.valueOf(number));

        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (checkBoxes.containsKey(number)) {
                checkBoxes.get(number).setText(newVal);
            }
            log("Изменено поле " + number + ": " + newVal);
        });

        HBox row = new HBox(10, label, field);
        VBox.setMargin(row, new Insets(5));

        fields.put(number, field);

        int insertIndex = 0;
        List<Integer> sortedKeys = new ArrayList<>(fields.keySet());
        Collections.sort(sortedKeys);

        for (Integer key : sortedKeys) {
            if (key == number) break;
            insertIndex++;
        }

        fieldsContainer.getChildren().add(insertIndex, row);

        if (withLog) {
            log("Добавлено поле " + number);
        }

        addCheckBox(number);
    }

    // УДАЛЕНИЕ ПОЛЯ
    private void deleteField(int number) {

        if (!fields.containsKey(number)) {
            showError("Поля с таким номером не существует");
            return;
        }

        TextField field = fields.get(number);
        HBox row = (HBox) field.getParent();

        fieldsContainer.getChildren().remove(row);
        fields.remove(number);

        if (checkBoxes.containsKey(number)) {
            CheckBox cb = checkBoxes.get(number);
            if (checkBoxContainer != null) {
                checkBoxContainer.getChildren().remove(cb);
            }
            checkBoxes.remove(number);
        }

        log("Удалено поле " + number);
    }

    // ДОБАВЛЕНИЕ ЧЕКБОКСА
    private void addCheckBox(int number) {
        if (checkBoxContainer == null) return;

        TextField field = fields.get(number);

        CheckBox cb = new CheckBox(field.getText());

        cb.setSelected(field.getFont() != null && field.getFont().getStyle().contains("Bold"));

        cb.selectedProperty().addListener((obs, oldVal, newVal) -> {
            field.setFont(newVal
                    ? Font.font("System", javafx.scene.text.FontWeight.BOLD, 12)
                    : Font.getDefault());

            log("Флажок " + number + ": " + newVal);
        });

        VBox.setMargin(cb, new Insets(5));

        checkBoxes.put(number, cb);

        int insertIndex = 0;
        List<Integer> sortedKeys = new ArrayList<>(checkBoxes.keySet());
        Collections.sort(sortedKeys);

        for (Integer key : sortedKeys) {
            if (key == number) break;
            insertIndex++;
        }

        checkBoxContainer.getChildren().add(insertIndex, cb);
    }

    private Integer parsePositiveInt(String text) {
        try {
            int value = Integer.parseInt(text);
            return value > 0 ? value : null;
        } catch (Exception e) {
            return null;
        }
    }

    // ОКНО МАСШТАБИРОВАНИЯ
    private void openScaleWindow(Stage mainStage) {
        Stage stage = new Stage();
        stage.initOwner(primaryStage);

        TextField input = new TextField();
        Button apply = new Button("Применить");

        apply.setOnAction(e -> {
            try {
                double scale = Double.parseDouble(input.getText());
                if (scale <= 0) {
                    log("Некорректное число: " + scale);
                    return;
                }

                double oldWidth = mainStage.getWidth();
                double oldHeight = mainStage.getHeight();

                double newWidth = oldWidth * scale;
                double newHeight = oldHeight * scale;

                mainStage.setWidth(Math.max(minWidth, Math.min(newWidth, maxWidth)));
                mainStage.setHeight(Math.max(minHeight, Math.min(newHeight, maxHeight)));

                if (newWidth < minWidth || newHeight < minHeight) {
                    log("Масштаб изменён на минимальный: " + (int)mainStage.getWidth() + " x " + (int)mainStage.getHeight());
                }
                else if (newWidth > maxWidth || newHeight > maxHeight) {
                    log("Масштаб изменён на максимальный: " + (int) mainStage.getWidth() + " x " + (int) mainStage.getHeight());
                } else {
                    log("Масштаб изменён: " + scale);
                }
            } catch (Exception ex) {
                showError("Некорректное число");
            }
        });

        VBox root = new VBox(10, new Label("Множитель:"), input, apply);
        root.setPadding(new Insets(10));

        stage.setScene(new Scene(root, 300, 150));
        stage.setTitle("Масштаб");
        stage.show();
    }

    // ОКНО ФЛАЖКОВ
    private void openCheckBoxWindow() {
        Stage stage = new Stage();
        stage.initOwner(primaryStage);

        checkBoxContainer = new VBox(5);
        checkBoxContainer.setPadding(new Insets(5, 10, 5, 10));

        checkBoxes.clear();

        List<Integer> sortedKeys = new ArrayList<>(fields.keySet());
        Collections.sort(sortedKeys);

        for (Integer number : sortedKeys) {
            addCheckBox(number);
        }

        ScrollPane scrollPane = new ScrollPane(checkBoxContainer);
        scrollPane.setFitToWidth(true);

        stage.setScene(new Scene(scrollPane, 300, 300));
        stage.setTitle("Флажки");

        stage.setOnCloseRequest(e -> checkBoxContainer = null);
        stage.show();
    }

    // ОКНО УПРАВЛЕНИЯ
    private void openManageWindow() {
        Stage stage = new Stage();
        stage.initOwner(primaryStage);

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
            createField(number, true);
        });

        removeBtn.setOnAction(e -> {
            Integer number = parsePositiveInt(input.getText());
            if (number == null) {
                showError("Введите положительное целое число");
                return;
            }
            deleteField(number);
        });

        VBox root = new VBox(10, new Label("Номер поля:"), input, addBtn, removeBtn);
        root.setPadding(new Insets(10));

        stage.setScene(new Scene(root, 300, 200));
        stage.setTitle("Управление");
        stage.show();
    }

    private void log(String text) {
        logArea.appendText(text + "\n");
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }

    public static void main(String[] args) {
        launch();
    }
}