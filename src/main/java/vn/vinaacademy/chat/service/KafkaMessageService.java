package vn.vinaacademy.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import vn.vinaacademy.chat.dto.MessageDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaMessageService {

  private final KafkaTemplate<String, MessageDto> kafkaTemplate;
  private final SimpMessagingTemplate messagingTemplate;

  private static final String PRIVATE_MESSAGE_TOPIC = "private-messages";
  private static final String GROUP_MESSAGE_TOPIC = "group-messages";

  /** Send private message to Kafka topic */
  public void sendPrivateMessage(String userId, MessageDto message) {
    kafkaTemplate
        .send(PRIVATE_MESSAGE_TOPIC, userId, message)
        .whenComplete(
            (res, ex) -> {
              if (ex != null) {
                log.error("Kafka send failed for userId={}", userId, ex);
              } else {
                log.debug(
                    "Kafka send ok topic={} partition={} offset={} key={}",
                    res.getRecordMetadata().topic(),
                    res.getRecordMetadata().partition(),
                    res.getRecordMetadata().offset(),
                    userId);
              }
            });
  }

  /** Send group message to Kafka topic */
  public void sendGroupMessage(String groupId, MessageDto message) {
    try {
      kafkaTemplate.send(GROUP_MESSAGE_TOPIC, groupId, message);
      log.info("Sent group message to Kafka topic: {} for group: {}", GROUP_MESSAGE_TOPIC, groupId);
    } catch (Exception e) {
      log.error("Failed to send group message to Kafka", e);
    }
  }

  /** Listen for private messages from Kafka and forward to WebSocket */
  @KafkaListener(topics = PRIVATE_MESSAGE_TOPIC)
  public void handlePrivateMessage(ConsumerRecord<String, MessageDto> record) {
    try {
      log.info("Received private message from Kafka: {}", record);

      MessageDto payload = record.value();

      messagingTemplate.convertAndSendToUser(record.key(), "/queue/pm", payload);
      log.info("Forwarded private message to WebSocket topic");
    } catch (Exception e) {
      log.error("Failed to handle private message from Kafka", e);
    }
  }

  /** Listen for group messages from Kafka and forward to WebSocket */
  @KafkaListener(topics = GROUP_MESSAGE_TOPIC)
  public void handleGroupMessage(ConsumerRecord<String, MessageDto> record) {
    try {
      log.info("Received group message from Kafka: {}", record);

      MessageDto payload = record.value();

      // Broadcast to topic (all subscribers to the conversation will receive)
      messagingTemplate.convertAndSend("/topic/group/" + record.key(), payload);
      log.info("Forwarded group message to WebSocket topic");
    } catch (Exception e) {
      log.error("Failed to handle group message from Kafka", e);
    }
  }
}
