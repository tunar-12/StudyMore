package StudyMore.models;

import java.time.LocalDateTime;

public class Multiplier {
    private long multiplierId; 
    private double currentValue; 
    private final double maxValue = 5.01;
    private final double minValue = 0.99;
    private final int incrementInterval = 10 * 60; // 10 minute
    private final int cooldownInterval = 60 * 60; // 1 hour 
    private int incrementIntervalDuration; 
    private int cooldownIntervalDuration; 
    private LocalDateTime lastActiveTime; 
    private boolean isUpdated;

    public Multiplier() {
        multiplierId = SnowflakeIDGenerator.generate();
        currentValue = 1;
        incrementIntervalDuration = incrementInterval; 
        cooldownIntervalDuration = cooldownInterval;
        lastActiveTime = LocalDateTime.now();
        isUpdated = true;
    }

    public Multiplier(double val) {
        this();
        currentValue = val;
    }

    public void increment() {
        if(checkIncrementDuration()) {
            incrementIntervalDuration--;
            return; // returns if the duration is not 0
        }

        incrementIntervalDuration = incrementInterval; 
        if (currentValue + 0.1 < maxValue) {
            currentValue += 0.1;
            isUpdated = true;
        }

        lastActiveTime = LocalDateTime.now();
    }

    public void applyCooldown() {
        if (checkCooldownDuration()) {
            cooldownIntervalDuration--;
            return; // returns if the duration is not 0
        }

        cooldownIntervalDuration = cooldownInterval;
        if (currentValue - 0.1 > minValue) {
            currentValue -= 0.1;
            isUpdated = true;
        }

        lastActiveTime = LocalDateTime.now();
    }

    // when accesed this variable we update the ui so change the isUpdated variable to false before returning it.
    public double getValue() {
        isUpdated = false;
        return currentValue;
    }

    public boolean isUpdated() {
        return isUpdated;
    }

    private boolean checkIncrementDuration() {
        return incrementIntervalDuration > 0;
    }

    private boolean checkCooldownDuration() {
        return cooldownIntervalDuration > 0;
    }

}
