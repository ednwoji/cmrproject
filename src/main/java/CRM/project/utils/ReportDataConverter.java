package CRM.project.utils;

import CRM.project.dto.ReportData;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.AttributeConverter;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

public class ReportDataConverter implements AttributeConverter<ReportData, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(ReportData reportData) {
        try {
            return objectMapper.writeValueAsString(reportData);
        } catch (Exception e) {
            throw new RuntimeException("Could not convert report data to JSON", e);
        }
    }

    @Override
    public ReportData convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, ReportData.class);
        } catch (Exception e) {
            throw new RuntimeException("Could not convert JSON to report data", e);
        }
    }

    public static String formatDate() {
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
        LocalDateTime dateTime = LocalDateTime.now();
        Date date = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
        return sf.format(date);
    }

    public static String generateRequestId() {
        return "UBNHLPDSK"+formatDate()+ UUID.randomUUID().toString().substring(0,3);
    }
}
