package CRM.project.service;

import CRM.project.dto.Availability;
import CRM.project.dto.MessagePreference;
import CRM.project.dto.Requestdto;
import CRM.project.entity.*;
import CRM.project.repository.DepartmentRepository;
import CRM.project.repository.RequestRepository;
import CRM.project.repository.SubCategoryRepository;
import CRM.project.repository.UsersRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

import static CRM.project.utils.ExcelToCategoryutils.getRolesForUser;

@Service
@Slf4j
public class RequestService {

    public static String DIRECTORY_PATH = "/u01/uploads/";
    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private SubCategoryRepository subCategoryRepository;

    @Autowired
    private EmailServiceImpl emailService;


    public Map<String, String> uploadImageToFileSystem(List<MultipartFile> files, RequestEntity requestEntity) throws IOException {
        Map<String, String> responseData = new HashMap<>();
        SubCategory sub = subCategoryRepository.findBySubCategoryName(requestEntity.getSubCategory()).orElse(null);
        Department department = departmentRepository.findByDepartmentName(requestEntity.getUnit()).orElse(null);
        if(department != null && sub != null) {
            requestEntity.setSla(sub.getSla());
            requestEntity.setDueDate(LocalDateTime.now().plusHours((long) sub.getSla()));
            List<FileMetaData> data = new ArrayList<>();
            if (files != null && !files.isEmpty()) {
                for (MultipartFile file : files) {
                    log.info("In here::::");
                    String filePath = saveFileToStorage(file);
                    FileMetaData fileMetaData = new FileMetaData();
                    fileMetaData.setFilePath(DIRECTORY_PATH + filePath);
                    fileMetaData.setType(file.getContentType());
                    fileMetaData.setName(file.getOriginalFilename());
                    log.info(fileMetaData.toString());
                    data.add(fileMetaData);
                }
                requestEntity.setFiles(data);
            }

            if(requestEntity.getTechnician().equalsIgnoreCase("Unassigned") && department.isAutoAssign()) {
                String technician = findLeastAssignedTechnician(requestEntity.getUnit());
                if(technician != null) {
                    requestEntity.setTechnician(technician);
                    List<AuditTrail> auditList = Arrays.asList(new AuditTrail("SYSTEM", "Assigned request to "+technician, LocalDateTime.now()));
                    requestEntity.setAuditTrails(auditList);
                }
                else {
                    requestEntity.setTechnician("Unassigned");
                }
            }
            if(!department.isAutoAssign()) {
                requestEntity.setTechnician("Unassigned");
            }

            requestEntity.setStatus(Status.OPEN);
            requestEntity.setResolvedWithinSla(true);
            requestEntity.setLogTime(LocalDateTime.now());
            log.info("Request Entity:::::: "+requestEntity);
            try {
                RequestEntity storeData = requestRepository.save(requestEntity);
                responseData.put("code", "00");
                responseData.put("message", "Request saved successfully");
                responseData.put("requestId", storeData.getRequestId());

//                try{
//                    emailService.sendEmail(requestEntity, MessagePreference.OPEN,null);
//                }catch (Exception e) {
//                    e.printStackTrace();
//                }
            }catch(Exception e) {
                responseData.put("code", "90");
                responseData.put("message", "Failed to save request");
            }
        }

        else {
            responseData.put("code", "99");
            responseData.put("message", "Invalid Department");
        }
        return responseData;
    }


    public byte[] downloadImageFromFileSystem(String fileName) throws IOException {
        byte[] images = Files.readAllBytes(new File(fileName).toPath());
        return images;
    }

    public String findLeastAssignedTechnician(String unitName) {

        Department department = departmentRepository.findByDepartmentName(unitName).orElse(null);
        List<RequestEntity> allRequestsForTheDay = requestRepository.findByUnitAndCreatedTimeBetween(unitName, LocalDate.now().atStartOfDay(), LocalDate.now().atTime(LocalTime.MAX));

        List<Users> allTeamMembers = usersRepository.findAllByUnitNameAndAvailability(department, Availability.fromCode("1"));
        List<String> unassignedStaff = new ArrayList<>();

        if(!allRequestsForTheDay.isEmpty()) {
            Map<String, Long> technicianRequestCount = allRequestsForTheDay.stream()
                    .collect(Collectors.groupingBy(RequestEntity::getTechnician, Collectors.counting()));

            log.info(technicianRequestCount.toString());

            allTeamMembers.stream().forEach(users -> {
                if (!technicianRequestCount.containsKey(users.getStaffName())) {
                    unassignedStaff.add(users.getStaffName());
                    log.info("Unassigned user is " + users.getStaffName());
                }
            });

            if (!unassignedStaff.isEmpty()) {
                return unassignedStaff.get(0);
            } else {
                long minRequestCount = technicianRequestCount.values().stream().min(Long::compare).orElse(Long.MAX_VALUE);
                log.info("" + minRequestCount);

                List<String> leastBusyTechnicians = technicianRequestCount.entrySet().stream()
                        .filter(entry -> entry.getValue() == minRequestCount)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());

                log.info(leastBusyTechnicians.toString());

                if (!leastBusyTechnicians.isEmpty()) {
                    Random random = new Random();
                    return leastBusyTechnicians.get(random.nextInt(leastBusyTechnicians.size()));
                }
            }
        }

        else {
            if(allTeamMembers.isEmpty()) {
                return null;
            }
            else {
                log.info("First Team member is:: " + allTeamMembers.stream().findAny().get().getStaffName());
                return allTeamMembers.get(0).getStaffName();
            }
        }

        return null;

    }

    public String saveFileToStorage(MultipartFile file) {

        String extensionType = file.getContentType();

        String extension = "";

        if (extensionType != null && !extensionType.isEmpty()) {
            String[] parts = extensionType.split("/");
            if (parts.length > 1) {
                extension = "." + parts[1];
            }
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + extension;

        try {
            File directory = new File(DIRECTORY_PATH);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            File outputFile = new File(DIRECTORY_PATH + fileName);

            FileOutputStream outputStream = new FileOutputStream(outputFile);
            outputStream.write(file.getBytes());
            outputStream.close();

            log.info("File saved successfully to: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            log.info("Error saving file: " + e.getMessage());
        }
        return fileName;
    }

    public List<RequestEntity> findAllRequestsByUnit(String unit) {
        return requestRepository.findByUnit(unit);
    }

    public Map<String, Long> findAllRecordByUser(String userName) {
        log.info("User is "+userName);
        Users user = usersRepository.findByUserEmail(userName).orElse(null);
        Map<String, Long> result = new HashMap<>();
        if(user != null) {
            List<RequestEntity> allRequests = requestRepository.findByTechnician(user.getStaffName());

            int totalRequests = allRequests.size();
            log.info(""+totalRequests);

            Long sumOfClosedRequests = allRequests.stream()
                    .filter(request -> Status.CLOSED.equals(request.getStatus()))
                    .mapToLong(request -> 1)
                    .sum();

            Long sumOfResolvedRequests = allRequests.stream()
                    .filter(request -> Status.RESOLVED.equals(request.getStatus()))
                    .mapToLong(request -> 1)
                    .sum();

            Long sumOfOpenRequests = allRequests.stream()
                    .filter(request -> Status.OPEN.equals(request.getStatus()))
                    .mapToLong(request -> 1)
                    .sum();

            long sumOfRequestsWithinSla = allRequests.stream()
                    .filter(request -> LocalDateTime.now().isAfter(request.getDueDate()))
                    .mapToLong(request -> 1)
                    .sum();

            result.put("openRequest", sumOfOpenRequests);
            result.put("closedRequest", sumOfClosedRequests);
            result.put("totalRequest", (long) totalRequests);
            result.put("resolvedTicket", sumOfResolvedRequests);
            result.put("withinSla", sumOfRequestsWithinSla);
            result.put("breachedSla", totalRequests - sumOfRequestsWithinSla);
        }
        return result;
    }

    public Map<String, Long> getRequestsPerMonth(String username) {

        List<String> roles = getRolesForUser(username);
        List<Object[]> results = new ArrayList<>();

        Map<String, Long> monthlyRequests = new HashMap<>();
        for (Month month : Month.values()) {
            monthlyRequests.put(month.name(), 0L);
        }

        results = !roles.contains("admin") ? requestRepository.findRequestsGroupedByMonth(username)
                : requestRepository.findAdminRequestsGroupedByMonth();

        for (Object[] result : results) {
            int month = (Integer) result[0];
            long count = (Long) result[1];
            monthlyRequests.put(Month.of(month).name(), count);
        }
        return monthlyRequests;
    }

//    public byte[] readFile(String filePath) throws IOException {
//      return Files.readAllBytes(Paths.get(filePath));
//    }

    public List<RequestEntity> fetchAllRequestsByStatus(Status status) {
        List<Status> excludedStatuses = Arrays.asList(Status.CLOSED, Status.RESOLVED);
        if(status == Status.OPEN) {
            return requestRepository.findByStatusNotIn(excludedStatuses);
        }
        return requestRepository.findByStatus(status);
    }
}
