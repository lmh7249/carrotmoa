package carrotmoa.carrotmoa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class RedisSubscriber {
    private final NotificationService notificationService;

    public void onMessage(String message) {
        log.info("Received Redis message!! : {}", message);
        notificationService.handleIncomingNotification(message);
    }

}
