package org.ley.time;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.ley.time.interfaces.RunTimer;
import org.ley.time.interfaces.TickTimer;
import org.ley.time.interfaces.TimeClock;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * The TimeMatrix class provides a sophisticated time management system for Bukkit plugins.
 * It allows registering various types of timers and clocks that trigger at specific times or intervals.
 */
public final class TimeMatrix {

    private static final int TICKS_PER_SECOND = 20;
    private static final HashMap<LocalTime, List<TimeClock>> clockMap = new HashMap<>();
    private static final HashMap<Integer, List<TickTimer>> tickTimerMap = new HashMap<>();
    private static final HashMap<Integer, List<RunTimer>> runTimerMap = new HashMap<>();

    private int globalTick = 0;
    private int runTick = 0;
    private final Plugin plugin;
    private final File dataFile;
    private final List<Long> timeStamps = new ArrayList<>();
    private BukkitTask task;

    /**
     * Constructs a new TimeMatrix instance for the given plugin.
     * @param plugin The plugin instance that owns this TimeMatrix
     * @throws NullPointerException if plugin is null
     */
    public TimeMatrix(Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "Plugin cannot be null");
        this.dataFile = new File(plugin.getDataFolder(), "time.yml");
        loadData();
        start();
    }

    /**
     * Starts the TimeMatrix scheduler.
     * If already running, logs a warning and does nothing.
     */
    public void start() {
        if (task != null && !task.isCancelled()) {
            plugin.getLogger().warning("TimeMatrix is already running!");
            return;
        }

        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            try {
                LocalDate currentDate = LocalDate.now();
                LocalTime currentTime = LocalTime.now();

                checkTimeClock(currentDate, currentTime);
                checkTickTimer(currentDate, currentTime);
                checkRunTimer(currentDate, currentTime);

                globalTick++;
                runTick++;

                if (globalTick % 6000 == 0) {
                    saveData();
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Error in TimeMatrix task: " + e.getMessage());
                e.printStackTrace();
            }
        }, 0L, 1L);
    }

    /**
     * Stops the TimeMatrix scheduler and saves data.
     */
    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        saveData();
    }

    private void loadData() {
        if (!dataFile.exists()) {
            return;
        }

        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
            this.globalTick = config.getInt("global_tick", 0);
            this.timeStamps.addAll(config.getLongList("time_stamps"));
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load TimeMatrix data: " + e.getMessage());
        }
    }

    private void saveData() {
        try {
            YamlConfiguration config = new YamlConfiguration();
            config.set("global_tick", globalTick);
            timeStamps.add(System.currentTimeMillis());
            config.set("time_stamps", timeStamps);

            if (!dataFile.exists()) {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            }

            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save time data: " + e.getMessage());
        }
    }

    /* TimeClock Methods */

    /**
     * Registers a TimeClock to trigger at a specific time.
     * @param clock The TimeClock implementation to register
     * @param time The time at which the clock should trigger
     * @throws NullPointerException if either parameter is null
     */
    public static void registerTimeClock(TimeClock clock, LocalTime time) {
        registerTimeClock(clock, List.of(time));
    }

    /**
     * Registers a TimeClock to trigger at multiple specific times.
     * @param clock The TimeClock implementation to register
     * @param times List of times at which the clock should trigger
     * @throws NullPointerException if either parameter is null
     */
    public static void registerTimeClock(TimeClock clock, List<LocalTime> times) {
        Objects.requireNonNull(clock, "TimeClock cannot be null");
        Objects.requireNonNull(times, "Times list cannot be null");

        times.forEach(time -> {
            LocalTime roundedTime = time.withNano(0);
            clockMap.computeIfAbsent(roundedTime, k -> new ArrayList<>()).add(clock);
        });
    }

    /**
     * Unregisters a TimeClock from all times.
     * @param clock The TimeClock to unregister
     */
    public static void unregisterTimeClock(TimeClock clock) {
        clockMap.values().forEach(list -> list.remove(clock));
    }

    /**
     * Unregisters a TimeClock from a specific time.
     * @param clock The TimeClock to unregister
     * @param time The time from which to unregister the clock
     */
    public static void unregisterTimeClock(TimeClock clock, LocalTime time) {
        List<TimeClock> clocks = clockMap.get(time.withNano(0));
        if (clocks != null) {
            clocks.remove(clock);
        }
    }

    /* TickTimer Methods */

    /**
     * Registers a TickTimer with a duration in the specified time unit.
     * @param timer The TickTimer implementation to register
     * @param timeType The time unit (SECONDS, MINUTES, etc.)
     * @param duration The duration in the specified time unit
     * @throws IllegalArgumentException if duration is not positive
     * @throws NullPointerException if timer or timeType is null
     */
    public static void registerTickTimer(TickTimer timer, TimeUnit timeType, int duration) {
        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        registerTickTimer(timer, convertToTicks(duration, timeType));
    }

    /**
     * Registers a TickTimer with multiple durations in the specified time unit.
     * @param timer The TickTimer implementation to register
     * @param timeType The time unit (SECONDS, MINUTES, etc.)
     * @param durations List of durations in the specified time unit
     * @throws NullPointerException if any parameter is null
     * @throws IllegalArgumentException if any duration is not positive
     */
    public static void registerTickTimer(TickTimer timer, TimeUnit timeType, List<Integer> durations) {
        Objects.requireNonNull(durations, "Durations list cannot be null");
        durations.forEach(duration -> registerTickTimer(timer, timeType, duration));
    }

    /**
     * Registers a TickTimer with a duration in ticks.
     * @param timer The TickTimer implementation to register
     * @param tickDuration The duration in ticks
     * @throws IllegalArgumentException if tickDuration is not positive
     */
    public static void registerTickTimer(TickTimer timer, int tickDuration) {
        if (tickDuration <= 0) {
            throw new IllegalArgumentException("Tick duration must be positive");
        }
        registerTickTimer(timer, List.of(tickDuration));
    }

    /**
     * Registers a TickTimer with multiple durations in ticks.
     * @param timer The TickTimer implementation to register
     * @param tickDurations List of durations in ticks
     * @throws NullPointerException if either parameter is null
     * @throws IllegalArgumentException if any tick duration is not positive
     */
    public static void registerTickTimer(TickTimer timer, List<Integer> tickDurations) {
        Objects.requireNonNull(timer, "TickTimer cannot be null");
        Objects.requireNonNull(tickDurations, "Tick durations list cannot be null");

        tickDurations.forEach(ticks -> {
            if (ticks <= 0) {
                throw new IllegalArgumentException("All tick durations must be positive");
            }
            tickTimerMap.computeIfAbsent(ticks, k -> new ArrayList<>()).add(timer);
        });
    }

    /**
     * Unregisters a TickTimer from all intervals.
     * @param timer The TickTimer to unregister
     */
    public static void unregisterTickTimer(TickTimer timer) {
        tickTimerMap.values().forEach(list -> list.remove(timer));
    }

    /**
     * Unregisters a TickTimer from a specific interval.
     * @param timer The TickTimer to unregister
     * @param tickDuration The tick interval from which to unregister
     */
    public static void unregisterTickTimer(TickTimer timer, int tickDuration) {
        List<TickTimer> timers = tickTimerMap.get(tickDuration);
        if (timers != null) {
            timers.remove(timer);
        }
    }

    /* RunTimer Methods */

    /**
     * Registers a RunTimer with a duration in the specified time unit.
     * @param timer The RunTimer implementation to register
     * @param timeType The time unit (SECONDS, MINUTES, etc.)
     * @param time The duration in the specified time unit
     * @throws IllegalArgumentException if time is not positive
     * @throws NullPointerException if timer or timeType is null
     */
    public static void registerRunTimer(RunTimer timer, TimeUnit timeType, int time) {
        if (time <= 0) {
            throw new IllegalArgumentException("Time must be positive");
        }
        registerRunTimer(timer, convertToTicks(time, timeType));
    }

    /**
     * Registers a RunTimer with multiple durations in the specified time unit.
     * @param timer The RunTimer implementation to register
     * @param timeType The time unit (SECONDS, MINUTES, etc.)
     * @param times List of durations in the specified time unit
     * @throws NullPointerException if any parameter is null
     * @throws IllegalArgumentException if any duration is not positive
     */
    public static void registerRunTimer(RunTimer timer, TimeUnit timeType, List<Integer> times) {
        Objects.requireNonNull(times, "Times list cannot be null");
        times.forEach(time -> registerRunTimer(timer, timeType, time));
    }

    /**
     * Registers a RunTimer with a duration in ticks.
     * @param timer The RunTimer implementation to register
     * @param ticks The duration in ticks
     * @throws IllegalArgumentException if ticks is not positive
     */
    public static void registerRunTimer(RunTimer timer, int ticks) {
        if (ticks <= 0) {
            throw new IllegalArgumentException("Ticks must be positive");
        }
        registerRunTimer(timer, List.of(ticks));
    }

    /**
     * Registers a RunTimer with multiple durations in ticks.
     * @param timer The RunTimer implementation to register
     * @param ticks List of durations in ticks
     * @throws NullPointerException if either parameter is null
     * @throws IllegalArgumentException if any tick duration is not positive
     */
    public static void registerRunTimer(RunTimer timer, List<Integer> ticks) {
        Objects.requireNonNull(timer, "RunTimer cannot be null");
        Objects.requireNonNull(ticks, "Ticks list cannot be null");

        ticks.forEach(tick -> {
            if (tick <= 0) {
                throw new IllegalArgumentException("All ticks must be positive");
            }
            runTimerMap.computeIfAbsent(tick, k -> new ArrayList<>()).add(timer);
        });
    }

    /**
     * Unregisters a RunTimer from all intervals.
     * @param timer The RunTimer to unregister
     */
    public static void unregisterRunTimer(RunTimer timer) {
        runTimerMap.values().forEach(list -> list.remove(timer));
    }

    /**
     * Unregisters a RunTimer from a specific interval.
     * @param timer The RunTimer to unregister
     * @param ticks The tick interval from which to unregister
     */
    public static void unregisterRunTimer(RunTimer timer, int ticks) {
        List<RunTimer> timers = runTimerMap.get(ticks);
        if (timers != null) {
            timers.remove(timer);
        }
    }

    /* Utility Methods */

    /**
     * Converts a duration in the given time unit to ticks.
     * @param duration The duration to convert
     * @param timeType The time unit of the duration
     * @return The duration in ticks
     * @throws NullPointerException if timeType is null
     */
    private static int convertToTicks(int duration, TimeUnit timeType) {
        Objects.requireNonNull(timeType, "TimeUnit cannot be null");
        return (int) (timeType.toSeconds(duration) * TICKS_PER_SECOND);
    }

    /**
     * Gets the current global tick count since the TimeMatrix started.
     * @return The global tick count
     */
    public int getGlobalTick() {
        return globalTick;
    }

    /**
     * Gets the current run tick count since the last reset.
     * @return The run tick count
     */
    public int getRunTick() {
        return runTick;
    }

    /**
     * Gets an unmodifiable list of all recorded timestamps.
     * @return List of timestamps in milliseconds
     */
    public List<Long> getTimeStamps() {
        return Collections.unmodifiableList(timeStamps);
    }

    /* Internal Methods */

    private void checkTimeClock(LocalDate date, LocalTime time) {
        LocalTime roundedTime = time.withNano(0);
        List<TimeClock> clocks = clockMap.get(roundedTime);
        if (clocks != null) {
            clocks.forEach(clock -> {
                try {
                    clock.playOnTimeClock(date, time, runTick, globalTick);
                } catch (Exception e) {
                    plugin.getLogger().severe("Error in TimeClock: " + e.getMessage());
                }
            });
        }
    }

    private void checkTickTimer(LocalDate date, LocalTime time) {
        tickTimerMap.forEach((tickInterval, timers) -> {
            if (tickInterval > 0 && globalTick % tickInterval == 0) {
                timers.forEach(timer -> {
                    try {
                        timer.playEveryTick(tickInterval, date, time, runTick, globalTick);
                    } catch (Exception e) {
                        plugin.getLogger().severe("Error in TickTimer: " + e.getMessage());
                    }
                });
            }
        });
    }

    private void checkRunTimer(LocalDate date, LocalTime time) {
        runTimerMap.forEach((tickInterval, timers) -> {
            if (tickInterval > 0 && runTick % tickInterval == 0) {
                timers.forEach(timer -> {
                    try {
                        timer.runOnRunTicks(date, time, runTick, globalTick);
                    } catch (Exception e) {
                        plugin.getLogger().severe("Error in RunTimer: " + e.getMessage());
                    }
                });
            }
        });
    }
}