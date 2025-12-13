package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);
    private FileService fileService = new FileService();
    private Path selectedSourceDir;
    private String selectedFileName;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().createAndShowGUI());
    }

    private void createAndShowGUI() {
        logger.info("Запуск графического интерфейса (Swing)");

        JFrame frame = new JFrame("Файловый менеджер (Курсовая)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout());
        JButton chooseDirBtn = new JButton("Выбрать исходную папку");
        topPanel.add(chooseDirBtn);

        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> fileList = new JList<>(listModel);
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fileList.setVisibleRowCount(10);
        JScrollPane listScrollPane = new JScrollPane(fileList);

        JTextField targetPathField = new JTextField(30);
        targetPathField.setText("/Users/" + System.getProperty("user.name") + "/Desktop");

        JButton copyBtn = new JButton("Скопировать файл");

        chooseDirBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setDialogTitle("Выберите папку с файлами");
                int result = chooser.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedDir = chooser.getSelectedFile();
                    selectedSourceDir = selectedDir.toPath();
                    try {
                        listModel.clear();
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
                        for (String item : fileItems) {
                            listModel.addElement(item);
                        }
                        logger.info("Загружено файлов: {}", fileItems.size());
                    } catch (IOException ex) {
                        logger.error("Ошибка загрузки файлов: {}", ex.getMessage());
                        JOptionPane.showMessageDialog(frame, "Ошибка: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        fileList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    String selectedItem = fileList.getSelectedValue();
                    if (selectedItem != null) {
                        int firstSpace = selectedItem.indexOf(' ');
                        selectedFileName = (firstSpace > 0) ? selectedItem.substring(0, firstSpace) : selectedItem;
                        logger.info("Выбран файл: {}", selectedFileName);
                    }
                }
            }
        });

        copyBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedSourceDir == null || selectedFileName == null) {
                    JOptionPane.showMessageDialog(frame, "Сначала выберите файл!", "Предупреждение", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String targetPathStr = targetPathField.getText().trim();
                if (targetPathStr.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Укажите путь назначения!", "Предупреждение", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                try {
                    Path targetDir = Path.of(targetPathStr);
                    fileService.ensureDirectoryExists(targetDir);
                    fileService.copyFile(
                            selectedSourceDir.resolve(selectedFileName),
                            targetDir.resolve(selectedFileName)
                    );
                    JOptionPane.showMessageDialog(frame, "Файл успешно скопирован!", "Успех", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    logger.error("Ошибка копирования: {}", ex.getMessage());
                    JOptionPane.showMessageDialog(frame, "Ошибка: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(new JLabel("Путь назначения:"));
        bottomPanel.add(targetPathField);
        bottomPanel.add(copyBtn);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(listScrollPane, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}