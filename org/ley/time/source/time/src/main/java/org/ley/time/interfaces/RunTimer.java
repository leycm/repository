package org.ley.time.interfaces;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Interface for timers that execute based on run ticks.
 * Run ticks are counted separately from global ticks and can be reset.
 */
public interface RunTimer {

    /**
     * Called when the run tick count reaches the registered interval.
     * @param date The current date when triggered
     * @param time The current time when triggered
     * @param runTick The current run tick count
     * @param globalTick The global tick count since TimeMatrix started
     */
    void runOnRunTicks(LocalDate date, LocalTime time, int runTick, int globalTick);
}