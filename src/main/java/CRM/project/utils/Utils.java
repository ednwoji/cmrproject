package CRM.project.utils;

import CRM.project.dto.AuthToken;
import CRM.project.encryptor.PropertyDecryptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;


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

}
