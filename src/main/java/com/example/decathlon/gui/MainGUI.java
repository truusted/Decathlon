package com.example.decathlon.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.example.decathlon.deca.*;
import com.example.decathlon.excel.ExcelPrinter;
import com.example.decathlon.excel.ExcelReader;

public class MainGUI {

    private static final int MAX_COMPETITORS = 40;

    private final String[] disciplines = {
            "100m", "400m", "1500m", "110m Hurdles",
            "Long Jump", "High Jump", "Pole Vault",
            "Discus Throw", "Javelin Throw", "Shot Put"
    };

    private final Map<String, Map<String, Integer>> competitors = new LinkedHashMap<>();

    private JTextField nameField;
    private JTextField resultField;
    private JComboBox<String> disciplineBox;
    private DefaultTableModel standingsModel;
    private JTable standingsTable;

    public static void main(String[] args) {
        new MainGUI().createAndShowGUI();
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Track and Field Calculator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5));

        nameField = new JTextField(20);
        inputPanel.add(new JLabel("Enter Competitor's Name:"));
        inputPanel.add(nameField);

        disciplineBox = new JComboBox<>(disciplines);
        inputPanel.add(new JLabel("Select Discipline:"));
        inputPanel.add(disciplineBox);

        resultField = new JTextField(10);
        inputPanel.add(new JLabel("Enter Result:"));
        inputPanel.add(resultField);

        JButton calculateButton = new JButton("Calculate Score");
        CalculateButtonListener calculateListener = new CalculateButtonListener();
        calculateButton.addActionListener(calculateListener);
        resultField.addActionListener(calculateListener);
        inputPanel.add(new JLabel(""));
        inputPanel.add(calculateButton);

        String[] columns = buildColumnNames();
        standingsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        standingsTable = new JTable(standingsModel);
        JScrollPane scrollPane = new JScrollPane(standingsTable);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton exportButton = new JButton("Export to Excel");
        exportButton.addActionListener(new ExportButtonListener());
        JButton importButton = new JButton("Import from Excel");
        importButton.addActionListener(new ImportButtonListener());
        bottomPanel.add(exportButton);
        bottomPanel.add(importButton);

        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private String[] buildColumnNames() {
        String[] columns = new String[disciplines.length + 3];
        columns[0] = "Rank";
        columns[1] = "Name";
        for (int i = 0; i < disciplines.length; i++) {
            columns[i + 2] = disciplines[i];
        }
        columns[columns.length - 1] = "Total";
        return columns;
    }

    private int totalFor(Map<String, Integer> scores) {
        int total = 0;
        for (Integer value : scores.values()) {
            total += value;
        }
        return total;
    }

    private void refreshStandings() {
        standingsModel.setRowCount(0);

        List<Map.Entry<String, Map<String, Integer>>> entries = new java.util.ArrayList<>(competitors.entrySet());
        entries.sort((a, b) -> totalFor(b.getValue()) - totalFor(a.getValue()));

        int rank = 1;
        for (Map.Entry<String, Map<String, Integer>> entry : entries) {
            Map<String, Integer> scores = entry.getValue();
            Object[] row = new Object[disciplines.length + 3];
            row[0] = rank;
            row[1] = entry.getKey();
            for (int i = 0; i < disciplines.length; i++) {
                Integer score = scores.get(disciplines[i]);
                row[i + 2] = score == null ? "" : score;
            }
            row[row.length - 1] = totalFor(scores);
            standingsModel.addRow(row);
            rank++;
        }
    }

    private int calculateScore(String discipline, double result) {
        switch (discipline) {
            case "100m":
                return new Deca100M().calculateResult(result);
            case "400m":
                return new Deca400M().calculateResult(result);
            case "1500m":
                return new Deca1500M().calculateResult(result);
            case "110m Hurdles":
                return new Deca110MHurdles().calculateResult(result);
            case "Long Jump":
                return new DecaLongJump().calculateResult(result);
            case "High Jump":
                return new DecaHighJump().calculateResult(result);
            case "Pole Vault":
                return new DecaPoleVault().calculateResult(result);
            case "Discus Throw":
                return new DecaDiscusThrow().calculateResult(result);
            case "Javelin Throw":
                return new DecaJavelinThrow().calculateResult(result);
            case "Shot Put":
                return new DecaShotPut().calculateResult(result);
            default:
                throw new IllegalArgumentException("Unknown discipline: " + discipline);
        }
    }

    private class CalculateButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String name = nameField.getText().trim();
            String discipline = (String) disciplineBox.getSelectedItem();
            String resultText = resultField.getText().trim();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please enter a competitor name.", "Missing Name", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double result;
            try {
                result = Double.parseDouble(resultText);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Please enter a valid number for the result.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!competitors.containsKey(name) && competitors.size() >= MAX_COMPETITORS) {
                JOptionPane.showMessageDialog(null, "The maximum number of competitors (" + MAX_COMPETITORS + ") has been reached.", "Competitor Limit Reached", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int score;
            try {
                score = calculateScore(discipline, result);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Invalid Result", JOptionPane.ERROR_MESSAGE);
                return;
            }

            competitors.computeIfAbsent(name, k -> new LinkedHashMap<>()).put(discipline, score);
            refreshStandings();
            resultField.setText("");
            resultField.requestFocusInWindow();
        }
    }

    private class ExportButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (competitors.isEmpty()) {
                JOptionPane.showMessageDialog(null, "There are no results to export yet.", "Nothing to Export", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JFileChooser chooser = new JFileChooser();
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            chooser.setSelectedFile(new File("results_" + timestamp + ".xlsx"));
            int choice = chooser.showSaveDialog(null);
            if (choice != JFileChooser.APPROVE_OPTION) {
                return;
            }

            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".xlsx")) {
                file = new File(file.getParentFile(), file.getName() + ".xlsx");
            }

            Object[][] data = new Object[competitors.size() + 1][disciplines.length + 2];
            String[] header = new String[disciplines.length + 2];
            header[0] = "Name";
            for (int i = 0; i < disciplines.length; i++) {
                header[i + 1] = disciplines[i];
            }
            header[header.length - 1] = "Total";
            data[0] = header;

            int rowIndex = 1;
            for (Map.Entry<String, Map<String, Integer>> entry : competitors.entrySet()) {
                Object[] row = new Object[disciplines.length + 2];
                row[0] = entry.getKey();
                Map<String, Integer> scores = entry.getValue();
                for (int i = 0; i < disciplines.length; i++) {
                    Integer score = scores.get(disciplines[i]);
                    row[i + 1] = score == null ? "" : score;
                }
                row[row.length - 1] = totalFor(scores);
                data[rowIndex] = row;
                rowIndex++;
            }

            try {
                ExcelPrinter printer = new ExcelPrinter();
                printer.add(data, "Results");
                printer.write(file);
                JOptionPane.showMessageDialog(null, "Results exported to " + file.getAbsolutePath(), "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Could not export the file: " + ex.getMessage(), "Export Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class ImportButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            int choice = chooser.showOpenDialog(null);
            if (choice != JFileChooser.APPROVE_OPTION) {
                return;
            }

            File file = chooser.getSelectedFile();
            try {
                ExcelReader reader = new ExcelReader();
                List<Object[]> rows = reader.readSheet(file, 0);
                if (rows.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "The selected file is empty.", "Import Failed", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Object[] header = rows.get(0);
                Map<Integer, String> columnDiscipline = new LinkedHashMap<>();
                for (int c = 1; c < header.length; c++) {
                    String columnName = String.valueOf(header[c]).trim();
                    for (String discipline : disciplines) {
                        if (discipline.equals(columnName)) {
                            columnDiscipline.put(c, discipline);
                            break;
                        }
                    }
                }

                Map<String, Map<String, Integer>> imported = new LinkedHashMap<>();
                for (int r = 1; r < rows.size(); r++) {
                    Object[] row = rows.get(r);
                    if (row.length == 0) {
                        continue;
                    }
                    String name = String.valueOf(row[0]).trim();
                    if (name.isEmpty()) {
                        continue;
                    }
                    Map<String, Integer> scores = new LinkedHashMap<>();
                    for (Map.Entry<Integer, String> col : columnDiscipline.entrySet()) {
                        int index = col.getKey();
                        if (index >= row.length) {
                            continue;
                        }
                        String value = String.valueOf(row[index]).trim();
                        if (value.isEmpty()) {
                            continue;
                        }
                        try {
                            scores.put(col.getValue(), (int) Math.round(Double.parseDouble(value)));
                        } catch (NumberFormatException ignored) {
                            continue;
                        }
                    }
                    imported.put(name, scores);
                }

                if (imported.size() > MAX_COMPETITORS) {
                    JOptionPane.showMessageDialog(null, "The file contains more than " + MAX_COMPETITORS + " competitors and cannot be imported.", "Import Failed", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                competitors.clear();
                competitors.putAll(imported);
                refreshStandings();
                JOptionPane.showMessageDialog(null, "Imported " + imported.size() + " competitor(s) from " + file.getName(), "Import Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Could not import the file: " + ex.getMessage(), "Import Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
