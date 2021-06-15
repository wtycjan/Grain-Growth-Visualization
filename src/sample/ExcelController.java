//package sample;
//import com.gembox.spreadsheet.*;
//import com.gembox.spreadsheet.charts.ChartType;
//import com.gembox.spreadsheet.charts.ExcelChart;
//
//import java.io.IOException;
//
//public class ExcelController {
//    public void Export() throws IOException {
//        SpreadsheetInfo.setLicense("FREE-LIMITED-KEY");
//
//        ExcelFile workbook = new ExcelFile();
//        ExcelWorksheet worksheet = workbook.addWorksheet("Chart");
//
//        int numberOfEmployees = 4;
//
//        // Create Excel chart and select data for it.
//        ExcelChart chart = worksheet.getCharts().add(ChartType.BAR, "D2", "M25");
//        chart.selectData(worksheet.getCells().getSubrangeAbsolute(0, 0, numberOfEmployees, 1), true);
//
//        // Add data which is used by the Excel chart.
//        String[] names = new String[] { "John Doe", "Fred Nurk", "Hans Meier", "Ivan Horvat" };
//        java.util.Random random = new java.util.Random();
//        for (int i = 0; i < numberOfEmployees; i++) {
//            worksheet.getCell(i + 1, 0).setValue(names[i % names.length] + (i < names.length ? "" : " " + (i / names.length + 1)));
//            worksheet.getCell(i + 1, 1).setValue(random.nextInt(4000) + 1000);
//        }
//
//        // Set header row and formatting.
//        worksheet.getCell(0, 0).setValue("Name");
//        worksheet.getCell(0, 1).setValue("Salary");
//        worksheet.getCell(0, 1).getStyle().getFont().setWeight(ExcelFont.BOLD_WEIGHT);
//        worksheet.getCell(0, 0).getStyle().getFont().setWeight(ExcelFont.BOLD_WEIGHT);
//        worksheet.getColumn(0).setWidth((int) LengthUnitConverter.convert(3, LengthUnit.CENTIMETER, LengthUnit.ZERO_CHARACTER_WIDTH_256_TH_PART));
//        worksheet.getColumn(1).getStyle().setNumberFormat("\"$\"#,##0");
//
//        workbook.save("Chart.xlsx");
//    }
//    }
//
