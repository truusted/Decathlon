package com.example.decathlon.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.example.decathlon.deca.*;
import com.example.decathlon.excel.ExcelPrinter;
import com.example.decathlon.excel.ExcelReader;
import com.example.decathlon.heptathlon.*;

public class MainGUI {

    private static final int MAX_COMPETITORS = 40;

    private record EventResult(double raw, int points) {}

    private enum Competition {
        DECATHLON("Decathlon", new String[] {
                "100m", "400m", "1500m", "110m Hurdles",
                "Long Jump", "High Jump", "Pole Vault",
                "Discus Throw", "Javelin Throw", "Shot Put"
        }),
        HEPTATHLON("Heptathlon", new String[] {
                "100m Hurdles", "High Jump", "Shot Put", "200m",
                "Long Jump", "Javelin Throw", "800m"
        });

        private final String label;
        private final String[] disciplines;

        Competition(String label, String[] disciplines) {
            this.label = label;
            this.disciplines = disciplines;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private final Map<Competition, Map<String, Map<String, EventResult>>> results = new EnumMap<>(Competition.class);

    private JComboBox<Competition> competitionBox;
    private JTextField nameField;
    private JTextField resultField;
    private JComboBox<String> disciplineBox;
    private DefaultTableModel standingsModel;
    private JTable standingsTable;

    public MainGUI() {
        for (Competition competition : Competition.values()) {
            results.put(competition, new LinkedHashMap<>());
        }
    }

    public static void main(String[] args) {
        new MainGUI().createAndShowGUI();
    }

    private Competition currentCompetition() {
        return (Competition) competitionBox.getSelectedItem();
    }

    private Map<String, Map<String, EventResult>> currentCompetitors() {
        return results.get(currentCompetition());
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Track and Field Calculator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 5, 5));

        competitionBox = new JComboBox<>(Competition.values());
        inputPanel.add(new JLabel("Select Competition:"));
        inputPanel.add(competitionBox);

        nameField = new JTextField(20);
        inputPanel.add(new JLabel("Enter Competitor's Name:"));
        inputPanel.add(nameField);

        disciplineBox = new JComboBox<>(Competition.DECATHLON.disciplines);
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

        standingsModel = new DefaultTableModel(buildColumnNames(Competition.DECATHLON), 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        standingsTable = new JTable(standingsModel);
        JScrollPane scrollPane = new JScrollPane(standingsTable);

        competitionBox.addActionListener(e -> {
            Competition competition = currentCompetition();
            disciplineBox.setModel(new DefaultComboBoxModel<>(competition.disciplines));
            standingsModel.setRowCount(0);
            standingsModel.setColumnIdentifiers(buildColumnNames(competition));
            refreshStandings();
        });

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
        SwingUtilities.invokeLater(() -> nameField.requestFocusInWindow());
    }

    private String[] buildColumnNames(Competition competition) {
        String[] disciplines = competition.disciplines;
        String[] columns = new String[disciplines.length + 3];
        columns[0] = "Rank";
        columns[1] = "Name";
        for (int i = 0; i < disciplines.length; i++) {
            columns[i + 2] = disciplines[i];
        }
        columns[columns.length - 1] = "Total";
        return columns;
    }

    private int totalFor(Map<String, EventResult> scores) {
        int total = 0;
        for (EventResult value : scores.values()) {
            total += value.points();
        }
        return total;
    }

    private void refreshStandings() {
        standingsModel.setRowCount(0);

        String[] disciplines = currentCompetition().disciplines;
        List<Map.Entry<String, Map<String, EventResult>>> entries = new ArrayList<>(currentCompetitors().entrySet());
        entries.sort((a, b) -> totalFor(b.getValue()) - totalFor(a.getValue()));

        int rank = 1;
        for (Map.Entry<String, Map<String, EventResult>> entry : entries) {
            Map<String, EventResult> scores = entry.getValue();
            Object[] row = new Object[disciplines.length + 3];
            row[0] = rank;
            row[1] = entry.getKey();
            for (int i = 0; i < disciplines.length; i++) {
                EventResult result = scores.get(disciplines[i]);
                row[i + 2] = result == null ? "" : result.points();
            }
            row[row.length - 1] = totalFor(scores);
            standingsModel.addRow(row);
            rank++;
        }
    }

    private int calculateScore(Competition competition, String discipline, double result) {
        if (competition == Competition.HEPTATHLON) {
            switch (discipline) {
                case "100m Hurdles":
                    return new Hep100MHurdles().calculateResult(result);
                case "High Jump":
                    return new HeptHightJump().calculateResult(result);
                case "Shot Put":
                    return new HeptShotPut().calculateResult(result);
                case "200m":
                    return new Hep200M().calculateResult(result);
                case "Long Jump":
                    return new HeptLongJump().calculateResult(result);
                case "Javelin Throw":
                    return new HeptJavelinThrow().calculateResult(result);
                case "800m":
                    return new Hep800M().calculateResult(result);
                default:
                    throw new IllegalArgumentException("Unknown discipline: " + discipline);
            }
        }
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

    private Object[][] buildSheetData(Competition competition) {
        String[] disciplines = competition.disciplines;
        Map<String, Map<String, EventResult>> competitors = results.get(competition);

        int columnCount = disciplines.length * 2 + 2;
        Object[][] data = new Object[competitors.size() + 1][columnCount];
        String[] header = new String[columnCount];
        header[0] = "Name";
        int headerIndex = 1;
        for (String discipline : disciplines) {
            header[headerIndex++] = discipline + " Raw";
            header[headerIndex++] = discipline + " Points";
        }
        header[header.length - 1] = "Total";
        data[0] = header;

        int rowIndex = 1;
        for (Map.Entry<String, Map<String, EventResult>> entry : competitors.entrySet()) {
            Object[] row = new Object[columnCount];
            row[0] = entry.getKey();
            Map<String, EventResult> scores = entry.getValue();
            int col = 1;
            for (String discipline : disciplines) {
                EventResult result = scores.get(discipline);
                row[col++] = result == null ? "" : result.raw();
                row[col++] = result == null ? "" : result.points();
            }
            row[row.length - 1] = totalFor(scores);
            data[rowIndex] = row;
            rowIndex++;
        }
        return data;
    }

    private Map<String, Map<String, EventResult>> parseSheet(Competition competition, List<Object[]> rows) {
        Map<String, Map<String, EventResult>> parsed = new LinkedHashMap<>();
        if (rows.isEmpty()) {
            return parsed;
        }

        String[] disciplines = competition.disciplines;
        Object[] header = rows.get(0);
        Map<String, Integer> rawColumns = new LinkedHashMap<>();
        Map<String, Integer> pointsColumns = new LinkedHashMap<>();
        for (int c = 1; c < header.length; c++) {
            String columnName = String.valueOf(header[c]).trim();
            for (String discipline : disciplines) {
                if (columnName.equals(discipline + " Raw")) {
                    rawColumns.put(discipline, c);
                } else if (columnName.equals(discipline + " Points")) {
                    pointsColumns.put(discipline, c);
                }
            }
        }

        for (int r = 1; r < rows.size(); r++) {
            Object[] row = rows.get(r);
            if (row.length == 0) {
                continue;
            }
            String name = String.valueOf(row[0]).trim();
            if (name.isEmpty()) {
                continue;
            }
            Map<String, EventResult> scores = new LinkedHashMap<>();
            for (String discipline : disciplines) {
                Integer rawCol = rawColumns.get(discipline);
                Integer pointsCol = pointsColumns.get(discipline);
                if (rawCol == null || pointsCol == null || rawCol >= row.length || pointsCol >= row.length) {
                    continue;
                }
                String rawValue = String.valueOf(row[rawCol]).trim();
                String pointsValue = String.valueOf(row[pointsCol]).trim();
                if (rawValue.isEmpty() || pointsValue.isEmpty()) {
                    continue;
                }
                try {
                    double raw = Double.parseDouble(rawValue);
                    int points = (int) Math.round(Double.parseDouble(pointsValue));
                    scores.put(discipline, new EventResult(raw, points));
                } catch (NumberFormatException ignored) {
                    continue;
                }
            }
            parsed.put(name, scores);
        }
        return parsed;
    }

    private class CalculateButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Competition competition = currentCompetition();
            Map<String, Map<String, EventResult>> competitors = currentCompetitors();
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
                score = calculateScore(competition, discipline, result);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Invalid Result", JOptionPane.ERROR_MESSAGE);
                return;
            }

            competitors.computeIfAbsent(name, k -> new LinkedHashMap<>()).put(discipline, new EventResult(result, score));
            refreshStandings();
            resultField.setText("");
            resultField.requestFocusInWindow();
        }
    }

    private class ExportButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean hasResults = false;
            for (Competition competition : Competition.values()) {
                if (!results.get(competition).isEmpty()) {
                    hasResults = true;
                }
            }
            if (!hasResults) {
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

            try {
                ExcelPrinter printer = new ExcelPrinter();
                for (Competition competition : Competition.values()) {
                    if (!results.get(competition).isEmpty()) {
                        printer.add(buildSheetData(competition), competition.label);
                    }
                }
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
                Map<Competition, Map<String, Map<String, EventResult>>> imported = new EnumMap<>(Competition.class);
                int importedCount = 0;

                for (Competition competition : Competition.values()) {
                    List<Object[]> rows = reader.readSheet(file, competition.label);
                    if (rows.isEmpty() && competition == Competition.DECATHLON) {
                        rows = reader.readSheet(file, "Results");
                    }
                    Map<String, Map<String, EventResult>> parsed = parseSheet(competition, rows);

                    if (parsed.size() > MAX_COMPETITORS) {
                        JOptionPane.showMessageDialog(null, "The " + competition.label + " sheet contains more than " + MAX_COMPETITORS + " competitors and cannot be imported.", "Import Failed", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    imported.put(competition, parsed);
                    importedCount += parsed.size();
                }

                if (importedCount == 0) {
                    JOptionPane.showMessageDialog(null, "The selected file does not contain any results.", "Import Failed", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                for (Competition competition : Competition.values()) {
                    results.get(competition).clear();
                    results.get(competition).putAll(imported.get(competition));
                }
                refreshStandings();
                JOptionPane.showMessageDialog(null, "Imported " + importedCount + " competitor(s) from " + file.getName(), "Import Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Could not import the file: " + ex.getMessage(), "Import Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}