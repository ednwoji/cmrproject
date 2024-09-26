package CRM.project.controller;

import CRM.project.dto.BulkDto;
import CRM.project.dto.CommentData;
import CRM.project.dto.Requestdto;
import CRM.project.entity.*;
import CRM.project.repository.DepartmentRepository;
import CRM.project.repository.RequestRepository;
import CRM.project.repository.UsersRepository;
import CRM.project.response.Responses;
import CRM.project.service.RequestService;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.apache.logging.log4j.util.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/requests")
@CrossOrigin
@Slf4j
public class RequestController {
    @Autowired
    private RequestService requestService;
    @Autowired
    private RequestRepository requestRepository;
    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UsersRepository usersRepository;
    @PostMapping("/createRequest")
    public ResponseEntity<?> uploadImageToFIleSystem(@RequestParam(value = "attachments", required = false) MultipartFile file,
                                                     @RequestParam("requester") String requester,
                                                     RequestEntity requestEntity,
                                                     @RequestParam("title") String subject,
                                                     @RequestParam("unit") String unit,
                                                     @RequestParam("email") String email,
                                                     @RequestParam("priority") String priority,
                                                     @RequestParam("category") String category,
                                                     @RequestParam("subcategory") String subCategory,
                                                     @RequestParam("description") String description,
                                                     @RequestParam("technician") String technician) throws IOException {
        requestEntity.setSubject(subject);
        requestEntity.setUnit(unit.split("~~")[0]);
        requestEntity.setPriority(priority);
        requestEntity.setCategory(category);
        requestEntity.setSubCategory(subCategory);
        requestEntity.setDescription(description);
        requestEntity.setTechnician(technician);
        requestEntity.setEmail(email);
        log.info("Requester name is:: "+requestEntity.getRequester());
        Map<String, String> uploadImage =requestService.uploadImageToFileSystem(file,requestEntity);
        return ResponseEntity.status(HttpStatus.OK)
                .body(uploadImage);
    }
    @PostMapping("/downloadRequest")
    public ResponseEntity<?> downloadImageFromFileSystem(@RequestBody Map<String, String> data) throws IOException {
        RequestEntity requestdto1=requestRepository.findById(Integer.valueOf(data.get("requestId"))).orElse(null);
        byte[] imageData=requestService.downloadImageFromFileSystem(requestdto1.getFilePath());
        return ResponseEntity.status(HttpStatus.OK)
                .body(imageData);

    }
    @PostMapping("/findRequestByUnit")
    public ResponseEntity<?> findRequestByUnit(@RequestBody Requestdto requestdto) {

        Optional<Department> department = departmentRepository.findByDepartmentName(requestdto.getDepartmentName());
        if(department.isPresent()) {
            List<RequestEntity> requests = requestRepository.findByStatusAndUnitAndTechnician(Status.valueOf(requestdto.getStatus()), department.get().getDepartmentName(), requestdto.getTechnician());
            return new ResponseEntity<>(requests, HttpStatus.OK);
        } else return new ResponseEntity<>(new Responses("90","Request could not be found", null),HttpStatus.OK);
    }


    @PostMapping("/findRequestByRequester")
    public ResponseEntity<?> findRequestByRequester(@RequestBody Requestdto requestdto) {

        log.info("Incoming request::: "+requestdto.toString());
//        Users user = usersRepository.findByStaffName(requestdto.getStaffName()).orElse(null);
//        if(user!=null) {
            List<RequestEntity> requests = null;
            if(requestdto.getStatus() == null) {
               requests = requestRepository.findByRequester(requestdto.getStaffName());
            }
            else {
                if(requestdto.getStaffName() != null) {
                    requests = requestRepository.findByRequesterAndStatus(requestdto.getStaffName(), Status.valueOf(requestdto.getStatus()));
                }
                else {
                    requests = requestRepository.findByRequesterUnitAndStatus(requestdto.getDepartmentName(), Status.valueOf(requestdto.getStatus()));
                }
            }
            return new ResponseEntity<>(requests, HttpStatus.OK);
//        } else
//            return new ResponseEntity<>(new Responses("90","Request could not be found"),HttpStatus.OK);
    }


    @PostMapping("/updateRequestTechnician")
    public ResponseEntity<?> assignRequestToTechnician(@RequestBody Map<String, String> data) {

        Map<String, String> response = new HashMap<>();
        RequestEntity request = requestRepository.findById(Integer.parseInt(data.get("requestId"))).orElse(null);

        if(request != null) {
            List<AuditTrail> auditList = Arrays.asList(new AuditTrail(request.getTechnician(), "Assigned request to "+data.get("newTechnician"), LocalDateTime.now()));
            request.setAuditTrails(auditList);
            request.setTechnician(data.get("newTechnician"));

            requestRepository.save(request);
            response.put("code", "00");
        }

        else {
            response.put("code", "99");
        }

        return new ResponseEntity<>(response, HttpStatus.OK);

    }


    @PostMapping("/closeRequest")
    public ResponseEntity<?> closeTicket(@RequestBody Map<String, String> data) {

        Responses response = null;
        log.info("Closing Request::: "+data.toString());
        RequestEntity request = requestRepository.findByRequestId(data.get("requestId")).orElse(null);

        if(request != null) {

            if(request.getStatus().equals(Status.valueOf(data.get("status")))) {
                response = new Responses<>("99", "You can't modify this request",null);
                return new ResponseEntity<>(response, HttpStatus.OK);
            }

            request.setStatus(Status.valueOf(data.get("status")));
            request.setClosureComments(data.get("closureComments"));
            request.getCommentData().add(new CommentData(data.get("closedBy"), data.get("closureComments"), LocalDateTime.now()));
            request.setClosureTime(LocalDateTime.now());
            request.setRating(Integer.parseInt(data.get("rating")));
            requestRepository.save(request);
            response = new Responses<>("00", "Success",null);
        }
        else {
            response = new Responses<>("99", "Invalid Request ID",null);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PostMapping("/updateRequestTechnicianMultiple")
    public ResponseEntity<?> pickBulkRequests(@RequestBody BulkDto bulkDto) {


        log.info("Incoming request for bulk assigning "+bulkDto.toString());
        Map<String, String> response = new HashMap<>();
        for(RequestEntity request : bulkDto.getRequests()) {
            RequestEntity findRequest = requestRepository.findByRequestId((request.getRequestId())).orElse(null);

            if(request != null) {
                request.setTechnician(bulkDto.getTechnician());
                requestRepository.save(request);
                response.put("code", "00");
            }

            else {
                response.put("code", "99");
            }
        }

        return new ResponseEntity<>(response, HttpStatus.OK);

    }



    @PostMapping("/pickupclosemultiple")
    public ResponseEntity<?> pickAndCloseBulkRequests(@RequestBody BulkDto bulkDto) {


        log.info("Incoming request for bulk assigning "+bulkDto.toString());
        if(bulkDto.getTechnician() != null) {
            Map<String, String> response = new HashMap<>();
            for (RequestEntity request : bulkDto.getRequests()) {
                RequestEntity findRequest = requestRepository.findByRequestId((request.getRequestId())).orElse(null);

                if (findRequest != null) {
                    findRequest.setTechnician(bulkDto.getTechnician());
                    findRequest.setStatus(Status.RESOLVED);
                    findRequest.setClosureTime(LocalDateTime.now());
                    findRequest.setClosureComments("Marked as resolved and awaiting confirmation");
                    findRequest.getCommentData().add(new CommentData(bulkDto.getTechnician(), "Marked as resolved and awaiting confirmation", LocalDateTime.now()));
                    requestRepository.save(findRequest);
                    response.put("code", "00");
                } else {
                    response.put("code", "99");
                }
            }
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        else {
            Map<String, String> response = new HashMap<>();
            for (RequestEntity request : bulkDto.getRequests()) {
                RequestEntity findRequest = requestRepository.findByRequestId((request.getRequestId())).orElse(null);

                if (findRequest != null) {
                    findRequest.setStatus(Status.CLOSED);
                    findRequest.setClosureTime(LocalDateTime.now());
                    findRequest.getCommentData().add(new CommentData(bulkDto.getTechnician(), "Marked as Closed", LocalDateTime.now()));
//                    findRequest.getComments().add("Marked as Closed");
                    findRequest.setClosureComments("Marked as Closed");
                    requestRepository.save(findRequest);
                    response.put("code", "00");
                } else {
                    response.put("code", "99");
                }
            }
            return new ResponseEntity<>(response, HttpStatus.OK);

        }

    }


    @PostMapping("/allData")
    public ResponseEntity<?> findAllData(@RequestBody Map<String, String> data) {
        Map<String, Long> allRecordByUser = requestService.findAllRecordByUser(data.get("userName"));
        return new ResponseEntity<>(allRecordByUser, HttpStatus.OK);
    }

    @PostMapping("/monthlyRequest")
    public ResponseEntity<?> findRequestsByMonth(@RequestBody Map<String, String> data) {
        return new ResponseEntity<>(requestService.getRequestsPerMonth(data.get("userName")), HttpStatus.OK);
    }
}
