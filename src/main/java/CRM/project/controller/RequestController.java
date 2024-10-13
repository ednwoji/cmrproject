package CRM.project.controller;

import CRM.project.dto.BulkDto;
import CRM.project.dto.CommentData;
import CRM.project.dto.MessagePreference;
import CRM.project.dto.Requestdto;
import CRM.project.entity.*;
import CRM.project.repository.DepartmentRepository;
import CRM.project.repository.RequestRepository;
import CRM.project.repository.UsersRepository;
import CRM.project.response.Responses;
import CRM.project.service.EmailServiceImpl;
import CRM.project.service.RequestService;
import CRM.project.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static CRM.project.service.RequestService.DIRECTORY_PATH;

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
    private EmailServiceImpl emailService;

    @Autowired
    private UsersRepository usersRepository;
    @PostMapping("/createRequest")
    public ResponseEntity<?> uploadImageToFIleSystem(@RequestParam(value = "attachments", required = false) List<MultipartFile> files,
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

        log.info("Description::: {}", description);
        requestEntity.setSubject(subject);
        requestEntity.setUnit(unit.split("~~")[0]);
        requestEntity.setPriority(priority);
        requestEntity.setCategory(category);
        requestEntity.setSubCategory(subCategory);
        requestEntity.setDescription(description);
        requestEntity.setTechnician(technician);
        requestEntity.setEmail(email);
        log.info("Requester name is:: "+requestEntity.getRequester());
        Map<String, String> uploadImage =requestService.uploadImageToFileSystem(files,requestEntity);
        return ResponseEntity.status(HttpStatus.OK)
                .body(uploadImage);
    }


//    @PostMapping("/downloadRequest")
//    public ResponseEntity<?> downloadImageFromFileSystem(@RequestBody Map<String, String> data) throws IOException {
//        RequestEntity requestdto1=requestRepository.findById(Integer.valueOf(data.get("requestId"))).orElse(null);
//        byte[] imageData=requestService.downloadImageFromFileSystem(requestdto1.getFilePath());
//        return ResponseEntity.status(HttpStatus.OK)
//                .body(imageData);
//    }


    @PostMapping("/downloadRequest")
    public ResponseEntity<?> downloadAttachments(@RequestBody Map<String, String> data) {
        String requestId = data.get("requestId");

        Optional<RequestEntity> optionalRequest = requestRepository.findByRequestId(data.get("requestId"));

        if (optionalRequest.isPresent()) {
            RequestEntity request = optionalRequest.get();
            List<FileMetaData> fileMetaDataList = request.getFiles();

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 ZipOutputStream zos = new ZipOutputStream(baos)) {

                for (FileMetaData fileMetaData : fileMetaDataList) {
                    log.info("File meta Data is "+fileMetaData.toString());
                    byte[] fileData = Utils.readFile(fileMetaData.getFilePath());
                    ZipEntry zipEntry = new ZipEntry(fileMetaData.getName());
                    zipEntry.setSize(fileData.length);
                    zos.putNextEntry(zipEntry);
                    zos.write(fileData);
                    zos.closeEntry();
                }

                zos.finish();
                byte[] zipData = baos.toByteArray();

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentDispositionFormData("attachment", requestId + "_attachments.zip");

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(zipData);
            } catch (IOException e) {
                log.error("Error while creating zip file: ", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while downloading files");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Request not found");
        }
    }


    @PostMapping("/findRequestByUnit")
    public ResponseEntity<?> findRequestByUnit(@RequestBody Requestdto requestdto) {

        Optional<Department> department = departmentRepository.findByDepartmentName(requestdto.getDepartmentName());
        Page<RequestEntity> requests;
        if(department.isPresent()) {
            Pageable pageable = PageRequest.of(requestdto.getPage(), requestdto.getSize());

            if(requestdto.getStatus().equalsIgnoreCase("OPEN")) {
                List<Status> excludedStatuses = Arrays.asList(Status.CLOSED, Status.RESOLVED);
                requests = requestRepository.findByStatusNotInAndUnitAndTechnician(excludedStatuses,
                        department.get().getDepartmentName(), requestdto.getTechnician(), pageable);
            }
            else {
                requests = requestRepository.findByStatusAndUnitAndTechnician(Status.valueOf(requestdto.getStatus()), department.get().getDepartmentName(), requestdto.getTechnician(), pageable);
            }
            return new ResponseEntity<>(requests, HttpStatus.OK);
        } else return new ResponseEntity<>(new Responses<>("90","Request could not be found", null),HttpStatus.OK);
    }


    @PostMapping("/findRequestByRequester")
    public ResponseEntity<?> findRequestByRequester(@RequestBody Requestdto requestdto) {

        log.info("Incoming request::: "+requestdto.toString());
//        Users user = usersRepository.findByStaffName(requestdto.getStaffName()).orElse(null);
//        if(user!=null) {
//            List<RequestEntity> requests = null;
            Page<RequestEntity> requests;
        Pageable pageable = PageRequest.of(requestdto.getPage(), requestdto.getSize());
        if(requestdto.getStatus() == null) {
               requests = requestRepository.findByRequester(requestdto.getStaffName(), pageable);
            }
            else {
                if(requestdto.getStatus().equalsIgnoreCase("OPEN")) {
                    List<Status> excludedStatuses = Arrays.asList(Status.CLOSED, Status.RESOLVED);

                    if (requestdto.getStaffName() != null) {
                    requests = requestRepository.findByRequesterAndStatusNotIn(requestdto.getStaffName(), excludedStatuses, pageable);
                    } else {
                    requests = requestRepository.findByRequesterUnitAndStatusNotIn(requestdto.getDepartmentName(), excludedStatuses, pageable);
                    }
            }
                else {
                    if (requestdto.getStaffName() != null) {
                        requests = requestRepository.findByRequesterAndStatus(requestdto.getStaffName(), Status.valueOf(requestdto.getStatus()), pageable);
                    } else {
                        requests = requestRepository.findByRequesterUnitAndStatus(requestdto.getDepartmentName(), Status.valueOf(requestdto.getStatus()), pageable);
                    }
                }
            }
            return new ResponseEntity<>(requests, HttpStatus.OK);
//        } else
//            return new ResponseEntity<>(new Responses("90","Request could not be found"),HttpStatus.OK);
    }


    @PostMapping("/fetchAllRequestsBank")
    public ResponseEntity<?> findAllRequests(@RequestBody Requestdto requestdto) {
        log.info("Incoming requests::: {} ",requestdto.toString());
        Pageable pageable = PageRequest.of(requestdto.getPage(), requestdto.getSize());
        Page<RequestEntity> fetchRequests = requestService.fetchAllRequestsByStatus(Status.valueOf(requestdto.getStatus()), pageable);
        return new ResponseEntity<>(fetchRequests, HttpStatus.OK);
    }

    @PostMapping("/updateRequestTechnician")
    @Transactional
    public ResponseEntity<?> assignRequestToTechnician(@RequestBody Map<String, String> data) {

        Map<String, String> response = new HashMap<>();
        RequestEntity request = requestRepository.findByRequestId(data.get("requestId")).orElse(null);

        if(request != null) {
            AuditTrail newAudit = new AuditTrail(request.getTechnician(), "Assigned request to "+data.get("newTechnician"), LocalDateTime.now());
            request.getAuditTrails().add(newAudit);
            request.setTechnician(data.get("newTechnician"));
            log.info("Request is::: {} ", request.toString());
            requestRepository.save(request);
            response.put("code", "00");
        }

        else {
            response.put("code", "99");
        }

        return new ResponseEntity<>(response, HttpStatus.OK);

    }


    @PostMapping("/findRequest")
    public ResponseEntity<?> getRequestById(@RequestBody Map<String, String> data) {
        if(data.get("requestId") != null) {
            Optional<RequestEntity> request = requestRepository.findByRequestId(data.get("requestId"));
            if(request.isPresent()) {
                return new ResponseEntity<>(new Responses<>("00","Success", request.get()), HttpStatus.OK);
            }
            return new ResponseEntity<>(new Responses<>("42","Failure", null), HttpStatus.OK);
        }
        return new ResponseEntity<>(new Responses<>("90","Invalid request ID", null), HttpStatus.OK);
    }


    @PostMapping("/additionalInfo")
    public ResponseEntity<?> addMoreInfoToRequest(@RequestParam(value = "attachments", required = false)List<MultipartFile> files,
                                                  @RequestParam("requestId") String requestId,
                                                  @RequestParam("staffName") String staffName,
                                                  @RequestParam("comments") String comments) {

        log.info("Comments::: "+comments);
        RequestEntity request = requestRepository.findByRequestId(requestId).orElse(null);
        if(request != null) {
            request.getCommentData().add(new CommentData(staffName,comments,LocalDateTime.now()));
            if (files != null && !files.isEmpty()) {
                for(MultipartFile file: files) {
                    String filePath = requestService.saveFileToStorage(file);
                    FileMetaData fileMetaData = new FileMetaData();
                    fileMetaData.setFilePath(DIRECTORY_PATH + filePath);
                    fileMetaData.setName(file.getOriginalFilename());
                    fileMetaData.setType(file.getContentType());
                    request.getFiles().add(fileMetaData);
                }
            }
            AuditTrail newAudit = new AuditTrail(staffName, "Provided additional information", LocalDateTime.now());
            request.setStatus(Status.OPEN);
            request.getAuditTrails().add(newAudit);

            requestRepository.save(request);
            return new ResponseEntity<>(new Responses<>("00", "Saved successfully", null), HttpStatus.OK);
        }
        return new ResponseEntity<>(new Responses<>("45", "Invalid request", null), HttpStatus.BAD_REQUEST);
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
            request.setRating(Integer.parseInt(data.get("rating")));
            if(Status.valueOf(data.get("status")) == Status.RESOLVED) {
                AuditTrail newAudit = new AuditTrail(request.getTechnician(), "Marked request as resolved", LocalDateTime.now());
                request.getAuditTrails().add(newAudit);
                request.setResolutionTime(LocalDateTime.now());
                request.setResolutionTechnician(data.get("closedBy"));
                request.setResolvedWithinSla(request.getDueDate().isAfter(request.getResolutionTime()));
            }

            else if(Status.valueOf(data.get("status")) == Status.CLOSED) {
                AuditTrail newAudit = new AuditTrail(request.getRequester(), "Marked request as closed", LocalDateTime.now());
                request.getAuditTrails().add(newAudit);
                request.setClosureTime(LocalDateTime.now());
            } else if(Status.valueOf(data.get("status")) == Status.ADDITIONAL_INFORMATION) {
                AuditTrail newAudit = new AuditTrail(request.getTechnician(), "requested for additional information", LocalDateTime.now());
                request.getAuditTrails().add(newAudit);
                request.setResolutionTime(LocalDateTime.now());
                request.setResolutionTechnician(data.get("closedBy"));
                request.setResolvedWithinSla(request.getDueDate().isAfter(request.getResolutionTime()));
            }

            else if(Status.valueOf(data.get("status")) == Status.ON_HOLD) {
                AuditTrail newAudit = new AuditTrail(request.getTechnician(), "Placed request on hold", LocalDateTime.now());
                request.getAuditTrails().add(newAudit);
                request.setResolutionTime(LocalDateTime.now());
                request.setResolutionTechnician(data.get("closedBy"));
                request.setResolvedWithinSla(request.getDueDate().isAfter(request.getResolutionTime()));
            }

            else if(Status.valueOf(data.get("status")) == Status.WORK_IN_PROGRESS) {
                AuditTrail newAudit = new AuditTrail(request.getTechnician(), "request is a work in progress", LocalDateTime.now());
                request.getAuditTrails().add(newAudit);
                request.setResolutionTime(LocalDateTime.now());
                request.setResolutionTechnician(data.get("closedBy"));
                request.setResolvedWithinSla(request.getDueDate().isAfter(request.getResolutionTime()));
            }

            requestRepository.save(request);
            response = new Responses<>("00", "Success",null);
            try{
                emailService.sendEmail(request, MessagePreference.valueOf(String.valueOf(request.getStatus())),null);
            }catch (Exception e) {
                e.printStackTrace();
            }
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
                    try {
                        emailService.sendEmail(findRequest, MessagePreference.valueOf(String.valueOf(findRequest.getStatus())), null);
                    }catch (Exception e) {
                        e.printStackTrace();
                    }

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
                    try {
                        emailService.sendEmail(findRequest, MessagePreference.valueOf(String.valueOf(findRequest.getStatus())), null);
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
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

    @PostMapping("/redirect-request")
    public ResponseEntity<?> RedirectRequests(@RequestBody Map<String, String> data) {

        RequestEntity request = requestRepository.findByRequestId(data.get("requestId")).orElse(null);
        if(request != null) {
            request.setUnit(data.get("newUnit"));
            AuditTrail auditTrail = new AuditTrail(data.get("routedBy"), "Routed request to "+request.getUnit(), LocalDateTime.now());
            request.getAuditTrails().add(auditTrail);
            String technician = requestService.findLeastAssignedTechnician(data.get("newUnit"));
            request.setTechnician(technician == null ? "Unassigned" : technician);
            request.setEmail(departmentRepository.findByDepartmentName(data.get("newUnit")).get().getEmail());
            requestRepository.save(request);
            return new ResponseEntity<>(new Responses<>("00", "Redirected successfully", null), HttpStatus.OK);
        }

        return new ResponseEntity<>(new Responses<>("99", "Invalid request ID", null), HttpStatus.OK);

    }
}
