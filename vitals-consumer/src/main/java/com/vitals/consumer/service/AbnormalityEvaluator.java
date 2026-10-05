package com.vitals.consumer.service;

import com.vitals.consumer.messaging.VitalsAlertMessage;
import com.vitals.consumer.messaging.VitalsReadingMessage;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Evaluates all abnormality rules defined in SPEC.md section 5.
 *
 * Each rule is a pure function of the reading — no I/O, no side effects.
 * This makes the rules trivially unit-testable without Spring context.
 *
 * A reading may produce zero, one, or multiple alert messages
 * (e.g. a reading with both high heart rate and low oxygen produces two alerts).
 */
@Component
public class AbnormalityEvaluator {

    // ── Heart rate thresholds ─────────────────────────────────────────────────
    private static final int HR_WARNING_HIGH  = 100;
    private static final int HR_CRITICAL_HIGH = 150;
    private static final int HR_WARNING_LOW   = 50;
    private static final int HR_CRITICAL_LOW  = 40;

    // ── Systolic BP thresholds ────────────────────────────────────────────────
    private static final int SBP_WARNING_HIGH  = 140;
    private static final int SBP_CRITICAL_HIGH = 180;
    private static final int SBP_WARNING_LOW   = 90;
    private static final int SBP_CRITICAL_LOW  = 70;

    // ── Diastolic BP thresholds ───────────────────────────────────────────────
    private static final int DBP_WARNING_HIGH  = 90;
    private static final int DBP_CRITICAL_HIGH = 120;
    private static final int DBP_WARNING_LOW   = 60;
    private static final int DBP_CRITICAL_LOW  = 40;

    // ── Oxygen saturation thresholds ──────────────────────────────────────────
    private static final BigDecimal SPO2_WARNING_LOW  = new BigDecimal("95.0");
    private static final BigDecimal SPO2_CRITICAL_LOW = new BigDecimal("90.0");

    // ── Temperature thresholds (°C) ───────────────────────────────────────────
    private static final BigDecimal TEMP_WARNING_HIGH  = new BigDecimal("37.5");
    private static final BigDecimal TEMP_CRITICAL_HIGH = new BigDecimal("39.0");
    private static final BigDecimal TEMP_WARNING_LOW   = new BigDecimal("36.0");
    private static final BigDecimal TEMP_CRITICAL_LOW  = new BigDecimal("35.0");

    /**
     * Evaluates a reading against all rules.
     *
     * @param message the incoming reading
     * @return list of alert messages — empty if the reading is entirely normal
     */
    public List<VitalsAlertMessage> evaluate(VitalsReadingMessage message) {
        List<VitalsAlertMessage> alerts = new ArrayList<>();
        Instant now = Instant.now();

        // Heart rate
        if (message.getHeartRate() > HR_CRITICAL_HIGH) {
            alerts.add(alert(message, "HIGH_HEART_RATE", "CRITICAL",
                    bd(message.getHeartRate()), bd(HR_CRITICAL_HIGH), now));
        } else if (message.getHeartRate() > HR_WARNING_HIGH) {
            alerts.add(alert(message, "HIGH_HEART_RATE", "WARNING",
                    bd(message.getHeartRate()), bd(HR_WARNING_HIGH), now));
        } else if (message.getHeartRate() < HR_CRITICAL_LOW) {
            alerts.add(alert(message, "LOW_HEART_RATE", "CRITICAL",
                    bd(message.getHeartRate()), bd(HR_CRITICAL_LOW), now));
        } else if (message.getHeartRate() < HR_WARNING_LOW) {
            alerts.add(alert(message, "LOW_HEART_RATE", "WARNING",
                    bd(message.getHeartRate()), bd(HR_WARNING_LOW), now));
        }

        // Systolic BP
        if (message.getSystolicBp() > SBP_CRITICAL_HIGH) {
            alerts.add(alert(message, "HIGH_SYSTOLIC_BP", "CRITICAL",
                    bd(message.getSystolicBp()), bd(SBP_CRITICAL_HIGH), now));
        } else if (message.getSystolicBp() > SBP_WARNING_HIGH) {
            alerts.add(alert(message, "HIGH_SYSTOLIC_BP", "WARNING",
                    bd(message.getSystolicBp()), bd(SBP_WARNING_HIGH), now));
        } else if (message.getSystolicBp() < SBP_CRITICAL_LOW) {
            alerts.add(alert(message, "LOW_SYSTOLIC_BP", "CRITICAL",
                    bd(message.getSystolicBp()), bd(SBP_CRITICAL_LOW), now));
        } else if (message.getSystolicBp() < SBP_WARNING_LOW) {
            alerts.add(alert(message, "LOW_SYSTOLIC_BP", "WARNING",
                    bd(message.getSystolicBp()), bd(SBP_WARNING_LOW), now));
        }

        // Diastolic BP
        if (message.getDiastolicBp() > DBP_CRITICAL_HIGH) {
            alerts.add(alert(message, "HIGH_DIASTOLIC_BP", "CRITICAL",
                    bd(message.getDiastolicBp()), bd(DBP_CRITICAL_HIGH), now));
        } else if (message.getDiastolicBp() > DBP_WARNING_HIGH) {
            alerts.add(alert(message, "HIGH_DIASTOLIC_BP", "WARNING",
                    bd(message.getDiastolicBp()), bd(DBP_WARNING_HIGH), now));
        } else if (message.getDiastolicBp() < DBP_CRITICAL_LOW) {
            alerts.add(alert(message, "LOW_DIASTOLIC_BP", "CRITICAL",
                    bd(message.getDiastolicBp()), bd(DBP_CRITICAL_LOW), now));
        } else if (message.getDiastolicBp() < DBP_WARNING_LOW) {
            alerts.add(alert(message, "LOW_DIASTOLIC_BP", "WARNING",
                    bd(message.getDiastolicBp()), bd(DBP_WARNING_LOW), now));
        }

        // Oxygen saturation
        if (message.getOxygenSaturation().compareTo(SPO2_CRITICAL_LOW) < 0) {
            alerts.add(alert(message, "LOW_OXYGEN_SATURATION", "CRITICAL",
                    message.getOxygenSaturation(), SPO2_CRITICAL_LOW, now));
        } else if (message.getOxygenSaturation().compareTo(SPO2_WARNING_LOW) < 0) {
            alerts.add(alert(message, "LOW_OXYGEN_SATURATION", "WARNING",
                    message.getOxygenSaturation(), SPO2_WARNING_LOW, now));
        }

        // Temperature
        if (message.getTemperatureCelsius().compareTo(TEMP_CRITICAL_HIGH) > 0) {
            alerts.add(alert(message, "HIGH_TEMPERATURE", "CRITICAL",
                    message.getTemperatureCelsius(), TEMP_CRITICAL_HIGH, now));
        } else if (message.getTemperatureCelsius().compareTo(TEMP_WARNING_HIGH) > 0) {
            alerts.add(alert(message, "HIGH_TEMPERATURE", "WARNING",
                    message.getTemperatureCelsius(), TEMP_WARNING_HIGH, now));
        } else if (message.getTemperatureCelsius().compareTo(TEMP_CRITICAL_LOW) < 0) {
            alerts.add(alert(message, "LOW_TEMPERATURE", "CRITICAL",
                    message.getTemperatureCelsius(), TEMP_CRITICAL_LOW, now));
        } else if (message.getTemperatureCelsius().compareTo(TEMP_WARNING_LOW) < 0) {
            alerts.add(alert(message, "LOW_TEMPERATURE", "WARNING",
                    message.getTemperatureCelsius(), TEMP_WARNING_LOW, now));
        }

        return alerts;
    }

    private VitalsAlertMessage alert(VitalsReadingMessage message,
                                     String alertType, String severity,
                                     BigDecimal value, BigDecimal threshold,
                                     Instant triggeredAt) {
        return VitalsAlertMessage.builder()
                .alertId(UUID.randomUUID())
                .readingId(message.getReadingId())
                .patientId(message.getPatientId())
                .alertType(alertType)
                .severity(severity)
                .value(value)
                .threshold(threshold)
                .triggeredAt(triggeredAt)
                .build();
    }

    private static BigDecimal bd(int value) {
        return BigDecimal.valueOf(value);
    }
}
