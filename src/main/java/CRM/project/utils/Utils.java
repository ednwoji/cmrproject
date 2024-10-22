package CRM.project.utils;

import CRM.project.dto.AuthToken;
import CRM.project.dto.RequestResponse;
import CRM.project.encryptor.PropertyDecryptor;
import CRM.project.entity.Department;
import CRM.project.entity.RequestEntity;
import CRM.project.entity.Users;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;


@Service
@Slf4j
public class Utils {

    public static String getAuthServToken() throws Exception {
        log.info("Getting AuthServ Token::::::::::::::::::");

        MultiValueMap<String, String> requestBody = setCredentialsForAuthServToken();
        HttpHeaders headers = createHeaders(null);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        log.info("Sending token request to URL " + RequiredParams.authservauthurl);
        AuthToken response = Constants.createRestTemplate().postForObject(RequiredParams.authservauthurl, requestEntity, AuthToken.class);
        return response.getAccess_token();
    }

    public static MultiValueMap<String, String> setCredentialsForAuthServToken() throws Exception {

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("client_secret", PropertyDecryptor.decrypt(RequiredParams.authServClientSecret, RequiredParams.secretKey));
        requestBody.add("client_id", RequiredParams.authServClientId);
        requestBody.add("grant_type", RequiredParams.authServGrantType);
        requestBody.add("username", RequiredParams.authServUsername);
        requestBody.add("password", PropertyDecryptor.decrypt(RequiredParams.authServPassword, RequiredParams.secretKey));

        return requestBody;
    }

    public static HttpHeaders createHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        if (token == null) {
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        } else {
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + token);
        }
        return headers;
    }

    public <T> T restTemplatePost(String endpoint, Object jsonPayload, HttpHeaders headers, Class<T> responseClass) {

        log.info("Making post call::::::::");
        HttpEntity<Object> requestEntity = new HttpEntity<>(jsonPayload, headers);
        ResponseEntity<T> responseEntity = Constants.createRestTemplate().exchange(endpoint, HttpMethod.POST, requestEntity, responseClass);
        return responseEntity.getBody();
    }


    public static String saveFiles(byte[] fileBytes, String path, String fileName) {

        try {
            File directory = new File(path);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            File outputFile = new File(directory, fileName);
            try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                outputStream.write(fileBytes);
                log.info("File saved successfully to: " + outputFile.getAbsolutePath());
                return "success";
            }
        } catch (IOException e) {
            log.error("Error saving file: " + e.getMessage(), e);
            return "error";
        }
    }

    public static byte[] readFile(String filePath) throws IOException {
        return Files.readAllBytes(Paths.get(filePath));
    }

    public static Users getUserProfile(String username) {

//        return Users.builder()
//                .staffName("Emeka Nwoji")
//                .unitName(new Department(0L,"ATM Support","atmsupport@unionbankng.com", false, ""))
//                .userEmail("ednwoji@unionbankng.com")
//                .userEmail(username)
//                .build();
        try {
            com.unionbankng.applications.ws.UBNSMSService_Service service1 = new com.unionbankng.applications.ws.UBNSMSService_Service();
            com.unionbankng.applications.ws.UBNSMSService port1 = service1.getBasicHttpBindingUBNSMSService();
            org.datacontract.schemas._2004._07.ubn_security.UserProfile result = port1.getUserProfile(username, "fcubs");
            if(result.getFirstName().getValue() != null) {
                return Users.builder()
                        .staffName(result.getLastName().getValue() + " " + result.getFirstName().getValue())
                        .unitName(new Department(0L, result.getJobTitle().getValue().split(", ")[1], "", false, ""))
                        .userEmail(result.getEmail().getValue())
                        .userEmail(username)
                        .build();
            }
            else {
                return null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

    }

    public static List<RequestResponse> mapRequestsToDto(List<RequestEntity> requests) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String jsonRequests = mapper.writeValueAsString(requests);
        return mapper.readValue(jsonRequests, new TypeReference<List<RequestResponse>>() {});
    }

}
