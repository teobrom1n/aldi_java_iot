package com.aldisued.iot.monitoring.service;

import com.aldisued.iot.monitoring.dto.AlertDto;
import com.aldisued.iot.monitoring.entity.Alert;
import com.aldisued.iot.monitoring.entity.Sensor;
import com.aldisued.iot.monitoring.repository.AlertRepository;
import com.aldisued.iot.monitoring.repository.SensorRepository;
import java.util.UUID;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AlertService {

  private final AlertRepository alertRepository;
  private final SensorRepository sensorRepository;
  private final KafkaTemplate<String, AlertDto> kafkaTemplate;

  public AlertService(AlertRepository alertRepository, SensorRepository sensorRepository,
      KafkaTemplate<String, AlertDto> kafkaTemplate) {
    this.alertRepository = alertRepository;
    this.sensorRepository = sensorRepository;
    this.kafkaTemplate = kafkaTemplate;
  }

  /**
   * Saves the alert and sends it to a kafka topic
   * @param alertDto contains the data to save an alert
   * @return Alert object
   * @throws IllegalArgumentException when there is no sensor found
   * */
  public Alert saveAlert(AlertDto alertDto) {
    Sensor sensor = sensorRepository.findById(alertDto.sensorId())
            .orElseThrow(() -> new IllegalArgumentException("Sensor not found"));

    Alert alert = alertRepository.save(new Alert(
            alertDto.message(), alertDto.timestamp(), sensor));

    kafkaTemplate.send("alerts", alertDto);
    return alert;
  }

  /**
   * Reads the database for the latest alert event
   * @param sensorId the Sensor's id
   * @return AlertDto of the latest event according to the sensor or null if there was no event or sensor
   *
   * */
  public AlertDto findLastAlertBySensorId(UUID sensorId) {
    Alert alert = alertRepository.findFirstBySensorIdOrderByTimestampDescIdDesc(sensorId);
    if (alert == null) {
      return null;
    }
    return new AlertDto(alert.getSensor().getId(), alert.getMessage(), alert.getTimestamp());
  }
}
