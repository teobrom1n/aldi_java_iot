package com.aldisued.iot.monitoring.service;


import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class MeasurementCalculatorService {

    /**
     * Filters the incoming list according to the allowed deviation from the average
     * Keeps values where abs(value - average) is at most abs(average) * deviation
     * @param values the List which contains the reading values
     * @param deviation the allowed deviation between 0.0 and 1.0, both included
     * @return filtered values in their original order, or an empty list if none remain
     * @throws IllegalArgumentException when deviation is null, NaN or outside the limits
     * */
    public List<Double> filterByAverageDeviation(List<Double> values, Double deviation) {
        if (deviation == null || !(deviation >= 0.0 && deviation <= 1.0)) {
            throw new IllegalArgumentException("Deviation must be between 0.0 and 1.0");
        }
        if (values.isEmpty()) {
            return List.of();
        }
        double sum = 0.0;

        for (Double value : values) {
            sum += value;
        }

        double average = sum / values.size();
        double allowedDeviation = Math.abs(average) * deviation;

        List<Double> filteredValues = new ArrayList<>();

        for (Double value : values) {
            if (Math.abs(value - average) <= allowedDeviation) {
                filteredValues.add(value);
            }
        }

        return filteredValues;
    }

}
