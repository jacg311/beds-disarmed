package net.jacg.beds_disarmed;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BedsDisarmed implements ModInitializer {
	public static final String MOD_ID = "beds_disarmed";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static Map<String, Boolean> CONFIG = new HashMap<>();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().setLenient().create();
	@Override
	public void onInitialize() {
		File configFile = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".json").toFile();
		if (!(configFile.exists() && configFile.isFile())) {
			configFile.getParentFile().mkdirs();
		}
		try (FileReader reader = new FileReader(configFile)) {
			CONFIG = GSON.fromJson(reader, CONFIG.getClass());
			if (CONFIG == null) {
				CONFIG = new HashMap<>();
				CONFIG.put("minecraft:the_end", false);
				CONFIG.put("minecraft:the_nether", false);
			}
		} catch (IOException e) {
			CONFIG = new HashMap<>();
		}

		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			LiteralArgumentBuilder<ServerCommandSource> command = CommandManager.literal("disableBedExplosion").requires(
					serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
					.then(CommandManager.argument("dimension", DimensionArgumentType.dimension())
							.then(CommandManager.argument("enable", BoolArgumentType.bool())
									.executes(context -> {
										String dim = DimensionArgumentType.getDimensionArgument(context, "dimension").getRegistryKey().getValue().toString();
										boolean enable = BoolArgumentType.getBool(context, "enable");
										CONFIG.put(dim, enable);
										context.getSource().sendFeedback(new LiteralText(
												enable ? "Beds in " + dim +" no longer explode" : "Beds in " + dim +" now explode"), true);
										return 1;
									})));

			dispatcher.register(command);
		});

		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			try (FileWriter writer = new FileWriter(configFile)) {
				writer.write(GSON.toJson(CONFIG));
			} catch (IOException e) {
				LOGGER.error("Could not save config. {}", e.getMessage());
			}
		});
	}
}
