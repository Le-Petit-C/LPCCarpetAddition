package lpcCarpetAddition;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import lpcCarpetAddition.loggers.EnderPearlLogger;
import lpcCarpetAddition.loggers.LPCStandardLogger;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class LPCCarpetAddition implements ModInitializer, CarpetExtension {
	public static final String MOD_ID = "lpccarpetaddition";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public interface LoggerSupplier extends Supplier<LPCStandardLogger>{}
	public static final LoggerSupplier[] loggers = new LoggerSupplier[]{
		EnderPearlLogger::getInstance
	};

	@Override public void onInitialize() {
		LOGGER.info("Start initializing...");
		CarpetServer.manageExtension(this);
		LOGGER.info("Initialized.");
	}

	@Override public void registerLoggers() {
		for(LoggerSupplier logger : loggers)
			LPCStandardLogger.registerLogger(logger.get());
	}
}