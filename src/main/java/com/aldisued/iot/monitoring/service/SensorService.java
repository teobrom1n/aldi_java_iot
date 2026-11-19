package com.aldisued.iot.monitoring.service;

import com.aldisued.iot.monitoring.dto.SensorDto;
import com.aldisued.iot.monitoring.entity.Sensor;
import com.aldisued.iot.monitoring.repository.SensorRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SensorService {

  private final SensorRepository sensorRepository;

  public SensorService(SensorRepository sensorRepository) {
    this.sensorRepository = sensorRepository;
  }

  /**
   * Saves the sensor with a unique name
   * @param sensor contains the data to save a sensor
   * @return the saved Sensor
   * @throws DuplicateKeyException when the sensor name already exists
   * */
  @Transactional
  public Sensor saveSensor(SensorDto sensor) {
    if (sensorRepository.existsByName(sensor.name())) {
      throw new DuplicateKeyException("Sensor name already exists");
    }

    return sensorRepository.save(new Sensor(
            sensor.name(),
            sensor.type()
    ));
  }
}
