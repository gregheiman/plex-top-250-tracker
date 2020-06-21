package org.gregh.PlexTop250Tracker;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class WriteMovieTitlesToExcel {
    private XSSFWorkbook workbook;

    public WriteMovieTitlesToExcel() {
        workbook = new XSSFWorkbook();
    }

    public XSSFWorkbook getWorkbook() {
        return workbook;
    }

    public void setWorkbook(XSSFWorkbook workbook) {
        this.workbook = workbook;
    }

    private CellStyle setSpreadsheetCellStyle(XSSFWorkbook workbook) {
        // Create a font for the rest of the spreadsheet
        Font spreadsheetFont = workbook.createFont();
        spreadsheetFont.setFontHeightInPoints((short) 16);

        // Use the font we just created to add to a cell style that will be applied to the movie titles
        CellStyle spreadsheetCellStyle = workbook.createCellStyle();
        spreadsheetCellStyle.setFont(spreadsheetFont);

        return spreadsheetCellStyle;
    }

    private static void setHeaderRow(XSSFWorkbook workbook, Sheet sheet) {
        // Set the font for the header row
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 24);
        headerFont.setColor(IndexedColors.RED.getIndex());

        // Create a cell style based on the font we just created
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        // Set the value of the header cell with the cell style we just created
        Row headerRow = sheet.createRow(0);
        Cell headerCell = headerRow.createCell(0);
        headerCell.setCellValue("Name");
        headerCell.setCellStyle(headerCellStyle);

        // Create another cell in the next column that houses when the sheet was created
        Cell dateCell = headerRow.createCell(1);
        dateCell.setCellValue("This spreadsheet was created on: " + LocalDateTime.now());
        dateCell.setCellStyle(headerCellStyle);
    }

    public void writeMissingMoviesToSpreadsheet(ArrayList<String> missingMovieNames) {
        FileOutputStream fileOut = null;
        try {
            fileOut = new FileOutputStream(LocalDateTime.now() + "-Missing Movies.xlsx");
        } catch (FileNotFoundException e) {
            System.out.println("There was an issue finding or creating the needed spreadsheet file.");
            e.printStackTrace();
        }

        // Create a spreadsheet inside of workbook and set the header row information
        Sheet spreadsheet = workbook.createSheet("Missing Movies");
        setHeaderRow(workbook, spreadsheet);

        // Go through the entire missingMovieNames list and add the names to the first column
        for (int i = 0; i < missingMovieNames.size(); i++) {
            // Create a new row for each movie
            // Needs to be + 1 in order to allow for the date at the top
            Row row = spreadsheet.createRow(i + 1);

            // For each row create a cell in the A column
            Cell cell = row.createCell(0);
            // Set the value of that A column to be the name of the movie that is missing
            cell.setCellValue(missingMovieNames.get(i));
            // Set the cell style for the movie titles
            cell.setCellStyle(setSpreadsheetCellStyle(workbook));
        }

        // Autosize the name column after all the movies have been added
        spreadsheet.autoSizeColumn(0);

        try {
            // Write the FileOutputStream to the workbook
            workbook.write(fileOut);
        } catch (IOException e) {
            System.out.println("There was a problem writing to the workbook.");
            e.printStackTrace();
        }

        try {
            // Close FileOutputStream and workbook when finished
            fileOut.close();
            workbook.close();
        } catch (IOException e) {
            System.out.println("There was a problem closing the workbook or the FileOutputStream");
            e.printStackTrace();
        }
    }

}
