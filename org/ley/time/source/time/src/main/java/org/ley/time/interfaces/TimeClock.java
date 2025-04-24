package org.ley.time.interfaces;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Interface for clocks that execute at specific times of day.
 */
public interface TimeClock {

    /**
     * Called when the current time matches the registered time.
     * @param date The current date when triggered
     * @param time The current time when triggered
     * @param runTick The current run tick count
     * @param globalTick The global tick count since TimeMatrix started
     */
    void playOnTimeClock(LocalDate date, LocalTime time, int runTick, int globalTick);
}