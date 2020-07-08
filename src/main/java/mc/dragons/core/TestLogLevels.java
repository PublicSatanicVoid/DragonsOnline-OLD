package mc.dragons.core;


import java.util.logging.Level;
import java.util.logging.Logger;
 
import org.apache.logging.log4j.LogManager;
import org.bukkit.plugin.Plugin;
 
public class TestLogLevels {
 
    private static final Level[] LEVEL_VALUES = new Level[] {
            Level.OFF, Level.SEVERE, Level.WARNING, Level.INFO,
            Level.CONFIG, Level.FINE, Level.FINER, Level.FINEST, Level.ALL
    };
 
    public static void onEnable(Plugin plugin) {
        Logger pluginLogger = plugin.getLogger();
        Logger rootLogger = Logger.getLogger("");
        org.apache.logging.log4j.core.Logger log4jRootLogger = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();
        org.apache.logging.log4j.core.Logger pluginLog4jLogger = (org.apache.logging.log4j.core.Logger) LogManager.getLogger(pluginLogger.getName());
 
        // Default
        printAllLoggers(pluginLogger);
        printAllLog4jLoggers(pluginLog4jLogger);
        logOnAllLevels(pluginLogger);
 
        // Plugin logger
        System.out.println("Setting plugin logger level to: " + Level.ALL);
        pluginLogger.setLevel(Level.ALL);
        printAllLoggers(pluginLogger);
        printAllLog4jLoggers(pluginLog4jLogger);
        logOnAllLevels(pluginLogger);
 
        // Plugin log4j logger
        System.out.println("Setting plugin log4j logger level to: " + Level.ALL);
        pluginLog4jLogger.setLevel(org.apache.logging.log4j.Level.ALL);
        printAllLoggers(pluginLogger);
        printAllLog4jLoggers(pluginLog4jLogger);
        logOnAllLevels(pluginLogger);
 
        // Root logger
        System.out.println("Setting root logger level to: " + Level.ALL);
        rootLogger.setLevel(Level.ALL);
        printAllLoggers(pluginLogger);
        printAllLog4jLoggers(pluginLog4jLogger);
        logOnAllLevels(pluginLogger);
 
        // Log4j root logger
        System.out.println("Setting log4j root logger level to: " + Level.ALL);
        log4jRootLogger.setLevel(org.apache.logging.log4j.Level.ALL);
        printAllLoggers(pluginLogger);
        printAllLog4jLoggers(pluginLog4jLogger);
        logOnAllLevels(pluginLogger);
 
        // All loggers
        setAllLogLevels(pluginLogger, Level.ALL);
        printAllLoggers(pluginLogger);
        printAllLog4jLoggers(pluginLog4jLogger);
        logOnAllLevels(pluginLogger);
 
        // All Log4j loggers
        setAllLog4JLogLevels(pluginLog4jLogger, org.apache.logging.log4j.Level.ALL);
        printAllLoggers(pluginLogger);
        printAllLog4jLoggers(pluginLog4jLogger);
        logOnAllLevels(pluginLogger);
    }
 
    private static void printAllLoggers(Logger logger) {
        System.out.println("------ Loggers overview");
        Logger current = logger;
        while (current != null) {
            System.out.println("Logger '" + current.getName() + "' has level " + current.getLevel());
            current = current.getParent();
        }
        System.out.println("------");
    }
 
    private static void printAllLog4jLoggers(org.apache.logging.log4j.core.Logger logger) {
        System.out.println("------ Log4j loggers overview");
        org.apache.logging.log4j.core.Logger current = logger;
        while (current != null) {
            System.out.println("Log4j Logger '" + current.getName() + "' has level " + current.getLevel());
            current = current.getParent();
        }
        System.out.println("------");
    }
 
    private static void setAllLogLevels(Logger logger, Level level) {
        System.out.println("------ Setting all logger levels");
        Logger current = logger;
        while (current != null) {
            System.out.println("Setting level of Logger '" + current.getName() + "' to level " + level);
            current.setLevel(level);
            current = current.getParent();
        }
        System.out.println("------");
    }
 
    private static void setAllLog4JLogLevels(org.apache.logging.log4j.core.Logger logger, org.apache.logging.log4j.Level level) {
        System.out.println("------ Setting all log4j logger levels");
        org.apache.logging.log4j.core.Logger current = logger;
        while (current != null) {
            System.out.println("Setting level of log4j Logger '" + current.getName() + "' to level " + level);
            current.setLevel(level);
            current = current.getParent();
        }
        System.out.println("------");
    }
 
    private static void logOnAllLevels(Logger logger) {
        System.out.println("------ Logging on all levels");
        for (Level level : LEVEL_VALUES) {
            logger.log(level, "A message with level " + level.getName());
        }
        System.out.println("------");
    }
}