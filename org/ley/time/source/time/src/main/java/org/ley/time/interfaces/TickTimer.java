package org.ley.time.interfaces;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Interface for timers that execute at regular tick intervals.
 */
public interface TickTimer {

    /**
     * Called at the registered tick interval.
     * @param tickFrequency The interval in ticks at which this timer was registered
     * @param date The current date when triggered
     * @param time The current time when triggered
     * @param runTick The current run tick count
     * @param globalTick The global tick count since TimeMatrix started
     */
    void playEveryTick(int tickFrequency, LocalDate date, LocalTime time, int runTick, int globalTick);
}