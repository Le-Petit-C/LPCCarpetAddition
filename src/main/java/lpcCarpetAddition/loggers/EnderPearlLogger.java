package lpcCarpetAddition.loggers;

public class EnderPearlLogger extends LPCStandardLogger{
    public static EnderPearlLogger getInstance(){return instance;}
    public static boolean isEnabled;
    private static final EnderPearlLogger instance = new EnderPearlLogger();
    private EnderPearlLogger() {
        super(EnderPearlLogger.class, "ender_pearl", new String[]{"test"}, true);
    }
}
