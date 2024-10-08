package CRM.project.service;

//import CRM.project.dto.NotificationMessage;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Service;
//
//@Service
//public class NotificationService {
//
//    private final SimpMessagingTemplate messagingTemplate;
//
//    @Autowired
//    public NotificationService(SimpMessagingTemplate messagingTemplate) {
//        this.messagingTemplate = messagingTemplate;
//    }
//
//    public void sendNotification(String message) {
//        NotificationMessage notificationMessage = new NotificationMessage();
//        notificationMessage.setContent(message);
//        messagingTemplate.convertAndSend("/topic/1", message);
//    }
//}
