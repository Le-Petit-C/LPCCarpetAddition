package lpcCarpetAddition.loggers;

import carpet.logging.Logger;
import carpet.logging.LoggerRegistry;

import java.lang.reflect.Field;

public abstract class LPCStandardLogger extends Logger {
    public static void registerLogger(Logger logger){
        LoggerRegistry.registerLogger(logger.getLogName(), logger);
    }
    protected LPCStandardLogger(Class<? extends LPCStandardLogger> clazz, String logName, String[] options, boolean strictOptions) {
        super(getIsEnabledField(clazz), logName, options[0], options, strictOptions);
    }
    private static Field getIsEnabledField(Class<?> clazz){
        try {
            return clazz.getDeclaredField("isEnabled");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
