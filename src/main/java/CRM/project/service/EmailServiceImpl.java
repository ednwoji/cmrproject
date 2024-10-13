package CRM.project.service;

import CRM.project.dto.EmailDto;
import CRM.project.dto.MessagePreference;
import CRM.project.entity.RequestEntity;
import CRM.project.entity.Status;
import CRM.project.entity.Users;
import CRM.project.response.EmailResponse;
import CRM.project.utils.RequiredParams;
import CRM.project.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class EmailServiceImpl {

    @Autowired
    private Utils utils;

//    @Autowired
//    private JavaMailSender mailSender;

    @Autowired
    private UsersService usersService;


    public EmailResponse sendEmail(RequestEntity request, MessagePreference messageType, byte[] attachment) throws Exception {
        EmailDto emailReq = new EmailDto();
        Users technician = usersService.fetchStaffByFullName(request.getTechnician());
        String suffix = "@unionbankng.com";

        String requesterEmail = request.getRequesterUserName()+suffix;
        String technicianEmail = technician!= null ? technician.getUserEmail()+suffix : "";

        switch (request.getStatus()) {
            case RESOLVED:
                emailReq.setSubject("REQUEST RESOLUTION NOTIFICATION - "+request.getRequestId());
                break;
            case CLOSED:
                emailReq.setSubject("REQUEST CLOSURE NOTIFICATION - "+request.getRequestId());
                break;
            default:
                emailReq.setSubject("REQUEST NOTIFICATION - "+request.getRequestId());
                break;
        }
        MessagePreference initiatorMessagePreference = null;
        MessagePreference technicianMessagePreference = null;

        if(messageType == MessagePreference.OPEN) {
            initiatorMessagePreference = MessagePreference.INITIATORCREATION;
        }
        else if(messageType == MessagePreference.RESOLVED) {
            initiatorMessagePreference = MessagePreference.INITIATORRESOLUTION;
        }
        else if(messageType == MessagePreference.CLOSED) {
            initiatorMessagePreference = MessagePreference.INITIATORCLOSURE;
        }else {
            initiatorMessagePreference = MessagePreference.OTHERS;
        }

        EmailResponse responses = null;

        try{

            List<EmailDto.Recipient> recList = new ArrayList<>();
            EmailDto.Recipient re = null;

            re = new EmailDto.Recipient(emailReq.getSubject(), "TO", requesterEmail);
            recList.add(re);

            emailReq.setSender(new EmailDto.Sender(emailReq.getSubject(), "itcare@unionbankng.com"));

            emailReq.setRecipients(recList);
            emailReq.setBody(emailForUsers(initiatorMessagePreference, request.getRequester(), request));

            String token = Utils.getAuthServToken();
            HttpHeaders headers = Utils.createHeaders(token);
            responses = utils.restTemplatePost(RequiredParams.emailUrl, emailReq, headers, EmailResponse.class);
            log.info("Email to requester::: {} ", responses.toString());

            recList.remove(0);
            EmailDto.Recipient rs = null;
            rs = new EmailDto.Recipient(emailReq.getSubject(), "TO", technicianEmail);
            recList.add(rs);
            if(messageType == MessagePreference.OPEN) {
                technicianMessagePreference = MessagePreference.TECHNICIANCREATION;
            }
            else if(messageType == MessagePreference.RESOLVED) {
                technicianMessagePreference = MessagePreference.TECHNICIANRESOLUTION;
            }
            else if(messageType == MessagePreference.CLOSED) {
                technicianMessagePreference = MessagePreference.TECHNICIANCLOSURE;
            }
            emailReq.setBody(emailForUsers(technicianMessagePreference, technician.getStaffName(), request));
            responses = utils.restTemplatePost(RequiredParams.emailUrl, emailReq, headers, EmailResponse.class);
            log.info("Email response to technician::: {} ", responses.toString());
        }
    catch (Exception ex) {
        ex.printStackTrace();
        }
        return responses;
    }


//    @Override
//    public MiservResponse sendEmail(Users users, MESSAGE messageType, TransactionBatchSummary summary) throws Exception {
//
//        log.info("Sending email to users::::::::::");
//        MimeMessage message = mailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(message, true);
//        String htmlContent = emailForUsers(messageType, users.getFirstName(), summary);
//
//        helper.setFrom("support@unit-session.com");
//        helper.setTo(users.getEmail());
//        helper.setText(htmlContent, true);
//        helper.setSubject("TRANSACTION NOTIFICATION");
//
//        mailSender.send(message);
//        log.info("Mail Sent successfully");
//        return null;
//
//    }


    private String emailForUsers(MessagePreference messageType, String recipientName, RequestEntity request) {
        if (messageType == null || recipientName == null) {
            return "<p>Invalid email content</p>";
        }
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<p>Dear ").append(recipientName).append(",</p>");

        switch (messageType) {
            case INITIATORCREATION:
                htmlContent.append("<p>You have successfully logged your request.</p>")
                        .append("<p>See breakdown of request and logon to the Union-CRM Portal to view the request.</p>");
                break;

            case TECHNICIANCREATION:
                htmlContent.append("<p>This is to notify you that a request has just been assigned to you.</p>")
                        .append("<p>See breakdown of request and logon to the Union-CRM Portal to view the request.</p>");
                break;

            case INITIATORRESOLUTION:
                htmlContent.append("<p>Your request has been marked as resolved successfully by the Technician</p>")
                        .append("<p>Please login to the CRM portal to validate and close request if confirmed resolved</p>")
                        .append("<p>Don't forget to rate the technician on the portal</p>");
                break;

            case TECHNICIANRESOLUTION:
                htmlContent.append("<p>Your have successfully marked your ticket as resolved.</p>")
                        .append("<p>Please follow up with the requester to validate and close ticket</p>");

            case INITIATORCLOSURE:
                htmlContent.append("<p>You have successfully closed your request</p>")
                        .append("<p>Thank you for using the Union CRM-Portal</p>");
                break;

            case TECHNICIANCLOSURE:
                htmlContent.append("<p>Request assigned to you have been closed successfully.</p>")
                        .append("<p>Please login to check other requests</p>");

                break;
            case OTHERS:
                htmlContent.append("<p>Request have been updated successfully with a new status.</p>")
                        .append("<p>Please login to check details</p>");
                break;

            default:
                htmlContent.append("<p>Request updated successfully with a new status.</p>")
                        .append("<p>Please login to check details</p>");
                break;
        }

        htmlContent.append("<table border=\"1\">")
                .append("<tbody>")
                .append("<tr><td style=\"background-color: #009FE3\"><b>Initiator Name</b></td><td>").append(
                        request.getRequester() != null ? request.getRequester() : "N/A").append("</td></tr>")
                .append("<tr><td style=\"background-color: #009FE3\"><b>Subject</b></td><td>").append(
                        request.getSubject() != null ? request.getSubject(): "N/A").append("</td></tr>")
                .append("<tr><td style=\"background-color: #009FE3\"><b>Technician</b></td><td>").append(
                        request.getTechnician() != null ? request.getTechnician() : "N/A").append("</td></tr>")
                .append("<tr><td style=\"background-color: #009FE3\"><b>SLA</b></td><td>").append(
                        request.getSla() != 0 ? request.getSla() : "0").append("</td></tr>")
                .append("</tbody></table>");

        return htmlContent.toString();
    }



}
