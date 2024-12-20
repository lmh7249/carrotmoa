package carrotmoa.carrotmoa.service;

import carrotmoa.carrotmoa.entity.Notification;
import carrotmoa.carrotmoa.entity.UserProfile;
import carrotmoa.carrotmoa.enums.NotificationType;
import carrotmoa.carrotmoa.model.request.NotificationUpdateRequest;
import carrotmoa.carrotmoa.model.request.SaveNotificationRequest;
import carrotmoa.carrotmoa.model.response.NotificationResponse;
import carrotmoa.carrotmoa.model.response.SseNotificationResponse;
import carrotmoa.carrotmoa.repository.EmitterRepository;
import carrotmoa.carrotmoa.repository.NotificationRepository;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import carrotmoa.carrotmoa.repository.UserProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private static final Long DEFAULT_TIMEOUT = 600L * 1000 * 60;
    private static final String NOTIFICATION_CHANNEL = "notification";

    private final EmitterRepository emitterRepository;
    private final NotificationRepository notificationRepository;
    private final RedisPublisher redisPublisher;
    private final ObjectMapper objectMapper;
    private final UserProfileRepository userProfileRepository;

    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitter.onCompletion(() -> emitterRepository.deleteById(userId));
        emitter.onTimeout(() -> emitterRepository.deleteById(userId));

        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("connected!"));
            emitterRepository.save(userId, emitter);
            log.info("SSE connection established for user: {}", userId);
        } catch (IOException e) {
            log.error("Error during SSE connection: ", e);
            throw new RuntimeException("SSE 연결 중 오류가 발생했습니다", e);
        }
        return emitter;
    }

    @Transactional
    public void sendNotification(Long receiverId, SaveNotificationRequest saveNotificationRequest, String userName, String picUrl) {
        // 1. DB에 알림 저장
        Notification notification = saveNotification(saveNotificationRequest);

        // 2. SSE 응답 객체 생성
        SseNotificationResponse sseNotificationResponse = new SseNotificationResponse(
                notification.getId(),
                NotificationType.COMMENT.getTitle(),
                userName,
                picUrl,
                saveNotificationRequest.getMessage(),
                saveNotificationRequest.getUrl(),
                notification.isRead(),
                notification.isDeleted(),
                notification.getCreatedAt()
        );

        // 3. Redis를 통해 알림 발행
        try {
            String notificationMessage = objectMapper.writeValueAsString(new NotificationMessage(receiverId, sseNotificationResponse));
            redisPublisher.publish(NOTIFICATION_CHANNEL, notificationMessage);
            log.info("Notification published to Redis for user: {}", receiverId);
        } catch (Exception e) {
            log.error("Error publishing notification to Redis: ", e);
            throw new RuntimeException("알림 발행 중 오류가 발생했습니다", e);
        }
    }

    // Redis Subscriber로부터 수신한 메시지 처리
    public void handleIncomingNotification(String message) {
        try {
            NotificationMessage notificationMessage = objectMapper.readValue(message, NotificationMessage.class);
            sendSseNotification(notificationMessage.getReceiverId(), notificationMessage.getNotification());
        } catch (Exception e) {
            log.error("Error handling incoming notification: ", e);
        }
    }

    private void sendSseNotification(Long receiverId, SseNotificationResponse sseNotificationResponse) {
        SseEmitter emitter = emitterRepository.get(receiverId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().data(sseNotificationResponse));
                log.info("SSE notification sent to user: {}", receiverId);
            } catch (Exception e) {
                log.error("Error sending SSE notification: ", e);
                emitterRepository.deleteById(receiverId);
            }
        }
    }

    private Notification saveNotification(SaveNotificationRequest saveNotificationRequest) {
        return notificationRepository.save(saveNotificationRequest.toNotificationEntity());
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> findNotificationsByReceiverId(Long receiverId) {
        return notificationRepository.findNotificationsByReceiverId(receiverId);
    }

    @Transactional
    public List<Long> updateNotifications(List<NotificationUpdateRequest> updateRequests) {
        List<Long> updatedNotificationIds = updateRequests.stream()
                .map(request -> {
                    Notification notification = notificationRepository.findById(request.getId())
                            .orElseThrow(() -> new RuntimeException("해당 알림의 아이디를 찾을 수 없습니다: " + request.getId()));
                    notification.updateStatus(request.isRead(), request.isDeleted());
                    return notification.getId();
                })
                .collect(Collectors.toList());

        notificationRepository.saveAll(updatedNotificationIds.stream()
                .map(id -> notificationRepository.getById(id))
                .collect(Collectors.toList()));

        return updatedNotificationIds;
    }

    // 내부 클래스: Redis 메시지 포맷
    private static class NotificationMessage {
        private Long receiverId;
        private SseNotificationResponse notification;

        public NotificationMessage() {}

        public NotificationMessage(Long receiverId, SseNotificationResponse notification) {
            this.receiverId = receiverId;
            this.notification = notification;
        }

        public Long getReceiverId() {
            return receiverId;
        }

        public SseNotificationResponse getNotification() {
            return notification;
        }
    }

    public void sendReservationNotification(NotificationType notificationType, Long senderId, Long receiverId, String notificationUrl, String message) {
        SaveNotificationRequest saveNotificationRequest = new SaveNotificationRequest(
                notificationType,
                receiverId,
                senderId,
                message,
                notificationUrl
        );
        UserProfile senderUser = userProfileRepository.findNicknameByUserId(senderId);
        sendNotification(receiverId,saveNotificationRequest, senderUser.getNickname(), senderUser.getPicUrl());
    }
}


