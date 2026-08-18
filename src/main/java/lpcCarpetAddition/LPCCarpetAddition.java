package lpcCarpetAddition;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lpcCarpetAddition.commands.EnchantmentCommand;
import lpcCarpetAddition.commands.HeadCommand;
import lpcCarpetAddition.commands.InteractAllowCommand;
import lpcCarpetAddition.commands.QSetBlockCommand;
import lpcCarpetAddition.loggers.EnderPearlLogger;
import lpcCarpetAddition.loggers.LPCStandardLogger;
import lpcCarpetAddition.validators.LoveGhastlingValidator;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

public class LPCCarpetAddition implements ModInitializer, CarpetExtension {
	public static final String MOD_ID = "lpcCarpetAddition";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public interface LoggerSupplier extends Supplier<LPCStandardLogger>{}
	public static final LoggerSupplier[] loggers = new LoggerSupplier[]{
		EnderPearlLogger::getInstance
	};

	@Override public void onInitialize() {
		LOGGER.info("Start initializing...");
		CarpetServer.manageExtension(this);
		CommandRegistrationCallback.EVENT.register(EnchantmentCommand.getInstance());
		CommandRegistrationCallback.EVENT.register(HeadCommand.getInstance());
		CommandRegistrationCallback.EVENT.register(InteractAllowCommand.getInstance());
		CommandRegistrationCallback.EVENT.register(QSetBlockCommand.getInstance());

		ServerLifecycleEvents.SERVER_STARTED.register(EnchantmentCommand.getInstance());
		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(EnchantmentCommand.getInstance());
		ServerLifecycleEvents.SERVER_STARTED.register(InteractAllowCommand.getInstance());
		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(InteractAllowCommand.getInstance());

		LoveGhastlingValidator.init();
		LOGGER.info("Initialized.");
	}

	@Override public void registerLoggers() {
		for(LoggerSupplier logger : loggers)
			LPCStandardLogger.registerLogger(logger.get());
	}

	@Override public void onGameStarted() {
		CarpetServer.settingsManager.parseSettingsClass(LPCCarpetSettings.class);
	}

	@Override
	public Map<String, String> canHasTranslations(String lang){
		InputStream langFile = LPCCarpetAddition.class.getClassLoader().getResourceAsStream("assets/lpccarpetaddition/lang/%s.json".formatted(lang));
		if (langFile == null) return Collections.emptyMap();
		String jsonData;
		try {
			jsonData = IOUtils.toString(langFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			return Collections.emptyMap();
		}
		return new Gson().fromJson(jsonData, new TypeToken<Map<String, String>>() {}.getType());
	}
}