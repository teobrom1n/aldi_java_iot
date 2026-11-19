package com.aldisued.iot.monitoring.controller;

import com.aldisued.iot.monitoring.dto.AlertDto;
import com.aldisued.iot.monitoring.service.AlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/alerts")
public class AlertController {

  private final AlertService alertService;

  public AlertController(AlertService alertService) {
    this.alertService = alertService;
  }


  /**
   * Endpoint to get the latest alert
   * @param sensorId the reading sensor's id
   * @return the latest AlertDto with HTTP 200, or HTTP 404 if no alert is found
   * */
  @GetMapping("/latest")
  public ResponseEntity<AlertDto> getLastAlert(@RequestParam UUID sensorId) {
    AlertDto alertDto = alertService.findLastAlertBySensorId(sensorId);
    if (alertDto == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(alertDto);
  }

}
