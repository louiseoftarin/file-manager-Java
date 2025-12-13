package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main extends Application {
    private static final Logger logger = LogManager.getLogger(Main.class);
    private FileService fileService = new FileService();
    private Path selectedSourceDir;
    private String selectedFileName;

    @Override
    public void start(Stage primaryStage) {
        logger.info("Запуск графического интерфейса");

        Button chooseDirBtn = new Button("Выбрать исходную папку");
        ListView<String> fileList = new ListView<>();
        fileList.setPrefHeight(150);
        TextField targetPathField = new TextField();
        targetPathField.setPromptText("Путь к целевой папке (например: /Users/имя/Desktop/dest)");
        Button copyBtn = new Button("Скопировать файл");

        chooseDirBtn.setOnAction(event -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Выберите папку с файлами");
            File selectedDir = chooser.showDialog(primaryStage);
            if (selectedDir != null) {
                selectedSourceDir = selectedDir.toPath();
                try {
                    List<String> fileItems = Files.list(selectedSourceDir)
                            .filter(Files::isRegularFile)
                            .map(file -> {
                                try {
                                    long size = Files.size(file);
                                    String sizeStr = FileService.formatBytes(size);
                                    return file.getFileName() + " (" + sizeStr + ")";
                                } catch (IOException ioEx) {
                                    return file.getFileName() + " (ошибка)";
                                }
                            })
                            .toList();
                    fileList.getItems().setAll(fileItems);
                    logger.info("Загружено файлов: {}", fileItems.size());
                } catch (IOException ex) {
                    logger.error("Ошибка загрузки файлов: {}", ex.getMessage());
                    new Alert(Alert.AlertType.ERROR, "Не удалось прочитать папку: " + ex.getMessage()).show();
                }
            }
        });

        fileList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                String selectedItem = fileList.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    // Извлекаем имя файла без размера: "file.txt (1.2 KB)" → "file.txt"
                    int firstSpace = selectedItem.indexOf(' ');
                    selectedFileName = (firstSpace > 0) ? selectedItem.substring(0, firstSpace) : selectedItem;
                    logger.info("Выбран файл: {}", selectedFileName);
                }
            }
        });

        copyBtn.setOnAction(event -> {
            if (selectedSourceDir == null || selectedFileName == null) {
                new Alert(Alert.AlertType.WARNING, "Сначала выберите файл!").show();
                return;
            }
            String targetPathStr = targetPathField.getText().trim();
            if (targetPathStr.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Укажите путь назначения!").show();
                return;
            }
            try {
                Path targetDir = Path.of(targetPathStr);
                fileService.ensureDirectoryExists(targetDir);
                fileService.copyFile(
                        selectedSourceDir.resolve(selectedFileName),
                        targetDir.resolve(selectedFileName)
                );
                new Alert(Alert.AlertType.INFORMATION, "Файл успешно скопирован!").show();
            } catch (Exception ex) {
                logger.error("Ошибка копирования: {}", ex.getMessage());
                new Alert(Alert.AlertType.ERROR, "Ошибка: " + ex.getMessage()).show();
            }
        });

        VBox root = new VBox(10,
                new Label("Файловый менеджер"),
                chooseDirBtn,
                new Label("Файлы в папке:"),
                fileList,
                targetPathField,
                copyBtn
        );
        root.setPadding(new javafx.geometry.Insets(15));

        Scene scene = new Scene(root, 500, 350);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Файловый менеджер (Курсовая)");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}