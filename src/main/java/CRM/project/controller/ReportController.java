package CRM.project.controller;

import CRM.project.dto.ReportData;
import CRM.project.entity.Reports;
import CRM.project.repository.ReportRepository;
import CRM.project.response.Responses;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/reports")
@CrossOrigin
@Slf4j
public class ReportController {


    @Autowired
    ReportRepository reportRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/create-report")
    public ResponseEntity<?> createReport(@RequestBody Map<String, Object> request) {

        Reports reports = new Reports();
        reports.setStartDate(String.valueOf(request.get("startDate")));
        reports.setEndDate(String.valueOf(request.get("endDate")));
        reports.setReportName(String.valueOf(request.get("reportName")));
        List<String> fields = (List<String>) request.get("selectedField");
        Map<String, String> fieldFilters = (Map<String, String>) request.get("fieldFilters");
        reports.setReportQuery(applyFilters(fieldFilters, fields, reports.getStartDate(), reports.getEndDate()));
        reports.setRequiredData(fields);
        reports.setReportData(mapReportContents(fields));
        reports.setCreatedBy(String.valueOf(request.get("createdBy")));
        reports.setCreatedTime(LocalDateTime.now());
        log.info("Generated query is::: "+reports.getReportQuery());

        try {
            reportRepository.save(reports);
            return new ResponseEntity<>(new Responses("00", "Report Saved Successfully", null), HttpStatus.OK);
        } catch (DataIntegrityViolationException e) {
            if (e.getCause() instanceof ConstraintViolationException) {
                ConstraintViolationException constraintException = (ConstraintViolationException) e.getCause();
                if (constraintException.getSQLException().getSQLState().startsWith("23")) {
                    log.error("Duplicate name error " +e.getMessage());
                    return new ResponseEntity<>(new Responses("90", "Name of report should be unique", null), HttpStatus.OK);
                }
            }
            log.error("Data integrity violation", e);
            return new ResponseEntity<>(new Responses("90", "Name of report should be unique", null), HttpStatus.BAD_REQUEST);
        }
        catch (Exception e) {
            log.info("Message is "+e.getMessage());
            return new ResponseEntity<>(new Responses("90", "Report Could not be saved", null), HttpStatus.OK);

        }
    }


    @PostMapping("/getReports")
    public ResponseEntity<?> fetchReports(@RequestBody Map<String, String> data) {

        log.info("Incoming request is::: " + data.toString());
        List<Reports> allReports = reportRepository.findByCreatedBy(data.get("createdBy"));
        if (!allReports.isEmpty()) {
            return new ResponseEntity<>(allReports, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new Responses("90", "No Request Yet", null), HttpStatus.OK);
        }
    }

    public ReportData mapReportContents(List<String> content) {

        ReportData data = new ReportData();
        for (String item : content) {
            if (item.equalsIgnoreCase("Requester"))
                data.setRequester(true);
            if (item.equalsIgnoreCase("Created Date"))
                data.setLogTime(true);
            if (item.equalsIgnoreCase("Closure Time"))
                data.setClosureTime(true);
            if (item.equalsIgnoreCase("Priority"))
                data.setPriority(true);
            if (item.equalsIgnoreCase("Closure Comments"))
                data.setPriority(true);
            if (item.equalsIgnoreCase("Technician"))
                data.setTechnician(true);
            if (item.equalsIgnoreCase("Description"))
                data.setDescription(true);
            if (item.equalsIgnoreCase("Subject"))
                data.setSubject(true);
            if (item.equalsIgnoreCase("Unit"))
                data.setUnit(true);
            if (item.equalsIgnoreCase("Category"))
                data.setCategory(true);
            if (item.equalsIgnoreCase("SLA"))
                data.setSla(true);
            if (item.equalsIgnoreCase("Sub-Category"))
                data.setSubCategory(true);

        }
        return data;
    }


    private String applyFilters(Map<String, String> filters, List<String> selectedFields, String startDate, String endDate) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT ");

        for (int i = 0; i < selectedFields.size(); i++) {
            query.append(renameSelectedFields(selectedFields.get(i)));
            if (i < selectedFields.size() - 1) {
                query.append(", ");
            }
        }
//        query.append(" FROM helpdesk_requests WHERE created_date between '"+startDate+"' and '"+endDate+"'");

                query
                .append(" FROM helpdesk_requests ")
                .append("WHERE created_date BETWEEN TO_TIMESTAMP('")
                .append(startDate)
                .append(" 00:00:00', 'YYYY-MM-DD HH24:MI:SS') ")
                .append("AND TO_TIMESTAMP('")
                .append(endDate)
                .append(" 23:59:59', 'YYYY-MM-DD HH24:MI:SS')");

        for (Map.Entry<String, String> entry : filters.entrySet()) {
            String field = entry.getKey();
            String value = entry.getValue();

            if (value != null && !value.isEmpty()) {
                query.append(" AND ").append(renameSelectedFields(field)).append(" = '").append(value).append("'");
            }
        }
        log.info("Constructed Query: " + query.toString());
        return query.toString();
    }

    private String renameSelectedFields(String s) {
        switch (s.toLowerCase()) {
            case "requester":
                return "requester";
            case "created date":
                return "created_date";
            case "closure time":
                return "closure_time";
            case "priority":
                return "priority";
            case "closure comments":
                return "closure_comments";
            case "technician":
                return "technician";
            case "description":
                return "description";
            case "subject":
                return "subject";
            case "unit":
                return "unit";
            case "category":
                return "category";
            case "sla":
                return "sla";
            case "sub-category":
                return "sub_category";
            default:
                return "";
        }
    }


    @PostMapping("export")
    public ResponseEntity<?> exportReportToExcel(@RequestBody Map<String, String> data, HttpServletResponse response) throws IOException {
        if(data.get("reportId") != null) {
            Reports reports = reportRepository.findByReportId(Long.valueOf(data.get("reportId"))).orElse(null);
            if(reports != null) {
                List<Map<String, Object>> resultList = jdbcTemplate.queryForList(reports.getReportQuery());

                try (Workbook workbook = new XSSFWorkbook()) {
                    Sheet sheet = workbook.createSheet("Report");

                    if (!resultList.isEmpty()) {
                        Row headerRow = sheet.createRow(0);
                        Map<String, Object> firstRow = resultList.get(0);
                        int colIndex = 0;
                        for (String key : firstRow.keySet()) {
                            Cell cell = headerRow.createCell(colIndex++);
                            cell.setCellValue(key);
                        }

                        // Create data rows
                        int rowIndex = 1;
                        for (Map<String, Object> rowMap : resultList) {
                            Row row = sheet.createRow(rowIndex++);
                            colIndex = 0;
                            for (Object value : rowMap.values()) {
                                Cell cell = row.createCell(colIndex++);
                                if (value instanceof String) {
                                    cell.setCellValue((String) value);
                                } else if (value instanceof Number) {
                                    cell.setCellValue(((Number) value).doubleValue());
                                } else if (value instanceof Boolean) {
                                    cell.setCellValue((Boolean) value);
                                } else {
                                    cell.setCellValue(value != null ? value.toString() : "");
                                }
                            }
                        }
                    }

//                    ByteArrayOutputStream out = new ByteArrayOutputStream();
//                    workbook.write(out);
//                    out.close();

                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    workbook.write(out);
                    workbook.close(); // Ensure the workbook is closed properly
                    byte[] bytes = out.toByteArray();

//                    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//                    response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.xlsx");
//                    response.getOutputStream().write(out.toByteArray());
//                    response.getOutputStream().flush();

                    HttpHeaders headers = new HttpHeaders();
                    headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.xlsx");
                    headers.setContentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);
                    headers.setContentLength(bytes.length);

                    return new ResponseEntity<>(bytes, headers, HttpStatus.OK);

                } catch (IOException e) {
                    e.printStackTrace();
                    return new ResponseEntity<>(new Responses("90", "Error generating Excel report", null), HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else {
                return new ResponseEntity<>(new Responses("90", "Report not found", null), HttpStatus.NOT_FOUND);
            }
        } else {
            return new ResponseEntity<>(new Responses("90", "Report ID is required", null), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/deleteReport")
    @Transactional
    public ResponseEntity<?> deleteReports(@RequestBody Map<String, String> data) {
        log.info("Incoming request is::: "+data.toString());
        Reports reports = reportRepository.findByReportId(Long.valueOf(data.get("reportId"))).orElse(null);
        if(reports == null) {
            return new ResponseEntity<>(new Responses("90", "Invalid Report ID", null), HttpStatus.OK);
        }else {
            reportRepository.deleteByReportId(reports.getReportId());
            return new ResponseEntity<>(new Responses("00", "Report deleted successfully", null), HttpStatus.OK);
        }
    }
}
