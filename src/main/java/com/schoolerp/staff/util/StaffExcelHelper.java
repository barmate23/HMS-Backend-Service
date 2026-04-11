package com.schoolerp.staff.util;

import com.schoolerp.staff.constants.StaffStatus;
import com.schoolerp.staff.dto.StaffExcelDto;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StaffExcelHelper {

    public static String[] HEADERS = {
            "First Name", "Last Name", "Email", "Phone", "Date of Birth (YYYY-MM-DD)",
            "Father Name", "License Number", "Department", "Designation", "Status",
            "Bank Name", "Account Holder Name", "Account Number", "IFSC Code", "Branch Name", "UPI ID",
            "Address Line 1", "Address Line 2", "City", "State", "Country", "Postal Code"
    };

    public static byte[] generateStaffExcelTemplate(List<String> departments, List<String> designations) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Staff Details");

            // Header Row
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < HEADERS.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(HEADERS[col]);
                sheet.setColumnWidth(col, 6000); // 6000 units roughly ~ 22 chars width
            }

            // Status Dropdown (Col 9)
            List<String> statuses = new ArrayList<>();
            for (StaffStatus status : StaffStatus.values()) {
                statuses.add(status.name());
            }
            if (!statuses.isEmpty()) {
                DataValidationHelper dvHelper = sheet.getDataValidationHelper();
                DataValidationConstraint dvConstraint = dvHelper.createExplicitListConstraint(statuses.toArray(new String[0]));
                CellRangeAddressList addressList = new CellRangeAddressList(1, 1000, 9, 9);
                DataValidation validation = dvHelper.createValidation(dvConstraint, addressList);
                validation.setShowErrorBox(true);
                sheet.addValidationData(validation);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Excel template: " + e.getMessage());
        }
    }

    public static List<StaffExcelDto> parseExcelFile(InputStream is) {
        try (Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheet("Staff Details");
            if (sheet == null) sheet = workbook.getSheetAt(0); // fallback

            Iterator<Row> rows = sheet.iterator();
            List<StaffExcelDto> staffList = new ArrayList<>();

            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();
                if (rowNumber == 0) {
                    rowNumber++;
                    continue; // Skip header
                }

                StaffExcelDto dto = new StaffExcelDto();
                boolean hasData = false;

                for (int cellIdx = 0; cellIdx < HEADERS.length; cellIdx++) {
                    Cell currentCell = currentRow.getCell(cellIdx);
                    String cellValue = currentCell != null ? getCellValueAsString(currentCell) : "";
                    
                    if (cellValue != null && !cellValue.trim().isEmpty()) {
                        hasData = true;
                    }

                    switch (cellIdx) {
                        case 0 -> dto.setFirstName(cellValue);
                        case 1 -> dto.setLastName(cellValue);
                        case 2 -> dto.setEmail(cellValue);
                        case 3 -> dto.setPhone(cellValue);
                        case 4 -> dto.setDob(cellValue);
                        case 5 -> dto.setFatherName(cellValue);
                        case 6 -> dto.setLicenseNumber(cellValue);
                        case 7 -> dto.setDepartmentName(cellValue);
                        case 8 -> dto.setDesignationName(cellValue);
                        case 9 -> dto.setStatus(cellValue);
                        case 10 -> dto.setBankName(cellValue);
                        case 11 -> dto.setAccountHolderName(cellValue);
                        case 12 -> dto.setAccountNumber(cellValue);
                        case 13 -> dto.setIfscCode(cellValue);
                        case 14 -> dto.setBranchName(cellValue);
                        case 15 -> dto.setUpiId(cellValue);
                        case 16 -> dto.setAddressLine1(cellValue);
                        case 17 -> dto.setAddressLine2(cellValue);
                        case 18 -> dto.setCity(cellValue);
                        case 19 -> dto.setState(cellValue);
                        case 20 -> dto.setCountry(cellValue);
                        case 21 -> dto.setPostalCode(cellValue);
                    }
                }

                if (hasData && dto.getEmail() != null && !dto.getEmail().isBlank()) {
                    staffList.add(dto);
                }
            }
            return staffList;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Excel file: " + e.getMessage());
        }
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                }
                // Handle scientific notation or decimal if needed, but phone might be numeric
                long val = (long) cell.getNumericCellValue();
                return String.valueOf(val);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
}
