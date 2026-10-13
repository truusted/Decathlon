package com.example.decathlon.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelReader {

	public List<Object[]> readSheet(File file, String sheetName) throws IOException {
		try (FileInputStream fis = new FileInputStream(file);
			 XSSFWorkbook wb = new XSSFWorkbook(fis)) {

			Sheet sheet = wb.getSheet(sheetName);
			if (sheet == null) {
				return new ArrayList<>();
			}
			return readRows(sheet);
		}
	}

	public List<Object[]> readSheet(File file, int sheetNumber) throws IOException {
		try (FileInputStream fis = new FileInputStream(file);
			 XSSFWorkbook wb = new XSSFWorkbook(fis)) {

			return readRows(wb.getSheetAt(sheetNumber));
		}
	}

	private List<Object[]> readRows(Sheet sheet) {
		List<Object[]> rows = new ArrayList<>();
		DataFormatter dataFormatter = new DataFormatter();

		for (Row row : sheet) {
			int lastCol = row.getLastCellNum();
			if (lastCol < 0) {
				continue;
			}
			Object[] values = new String[lastCol];
			for (int c = 0; c < lastCol; c++) {
				Cell cell = row.getCell(c);
				values[c] = cell == null ? "" : dataFormatter.formatCellValue(cell);
			}
			rows.add(values);
		}
		return rows;
	}

}