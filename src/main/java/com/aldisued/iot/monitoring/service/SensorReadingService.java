package com.aldisued.iot.monitoring.service;

import com.aldisued.iot.monitoring.dto.SensorReadingDto;
import com.aldisued.iot.monitoring.entity.Alert;
import com.aldisued.iot.monitoring.entity.Sensor;
import com.aldisued.iot.monitoring.entity.SensorReading;
import com.aldisued.iot.monitoring.entity.SensorType;
import com.aldisued.iot.monitoring.repository.AlertRepository;
import com.aldisued.iot.monitoring.repository.SensorReadingRepository;
import com.aldisued.iot.monitoring.repository.SensorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class SensorReadingService {

    private final SensorReadingRepository sensorReadingRepository;
    private final SensorRepository sensorRepository;
    private final AlertRepository alertRepository;

    public SensorReadingService(SensorReadingRepository sensorReadingRepository,
                                SensorRepository sensorRepository, AlertRepository alertRepository) {
        this.sensorReadingRepository = sensorReadingRepository;
        this.sensorRepository = sensorRepository;
        this.alertRepository = alertRepository;
    }

    @Transactional
    public SensorReading saveSensorReading(SensorReadingDto sensorReadingDto) {

        Sensor sensor = sensorRepository.findById(sensorReadingDto.sensorId())
                .orElseThrow(() -> new IllegalArgumentException("Sensor not found"));
        SensorReading sensorReading = sensorReadingRepository.save(new SensorReading(
                sensorReadingDto.value(), sensorReadingDto.timestamp(), sensor));

        String alertMessage = getAlertMessage(sensor.getType(), sensorReading.getValue());
        if (alertMessage != null) {
            alertRepository.save(new Alert(alertMessage, sensorReading.getTimestamp(), sensor));
        }

        return sensorReading;
    }

    private String getAlertMessage(SensorType type, double value) {
        String messageFormat = switch (type) {
            case TEMPERATURE -> value > 40.0 || value < -20.0
                    ? "Temperature out of range: %s" : null;
            case HUMIDITY -> value > 90.0 ? "Humidity too high: %s" : null;
            case ATMOSPHERIC_PRESSURE -> value < 950.0 || value > 1050.0
                    ? "Pressure out of range: %s" : null;
        };
        if (messageFormat == null) {
            return null;
        }
        return String.format(Locale.ROOT, messageFormat, value);
    }

}
