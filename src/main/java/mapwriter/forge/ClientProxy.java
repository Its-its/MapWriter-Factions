package mapwriter.forge;

import java.io.File;

import mapwriter.Mw;
import mapwriter.api.MwAPI;
import mapwriter.config.ConfigurationHandler;
import mapwriter.overlay.OverlayFaction;
import mapwriter.overlay.OverlayFactionGrid;
import mapwriter.overlay.OverlayGrid;
import mapwriter.overlay.OverlaySlime;
import mapwriter.region.MwChunk;
import mapwriter.util.Reference;
import mapwriter.util.VersionCheck;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInterModComms;

public class ClientProxy extends CommonProxy {

	@Override
	public void preInit(File configFile) {
		ConfigurationHandler.init(configFile);
		MinecraftForge.EVENT_BUS.register(new ConfigurationHandler());
	}

	@Override
	public void load() {
		EventHandler eventHandler = new EventHandler(Mw.getInstance());
		MinecraftForge.EVENT_BUS.register(eventHandler);

		MwKeyHandler keyEventHandler = new MwKeyHandler();
		MinecraftForge.EVENT_BUS.register(keyEventHandler);
	}

	@Override
	public void postInit() {
		if (Loader.isModLoaded("VersionChecker")) {
			FMLInterModComms.sendRuntimeMessage(Reference.MOD_ID, "VersionChecker", "addVersionCheck", Reference.VersionURL);
		} else {
			new Thread(new VersionCheck(), "Version Check").start();
		}
		
		if (Loader.isModLoaded("CarpentersBlocks")) {
			MwChunk.carpenterdata();
		}
		
		if (Loader.isModLoaded("ForgeMultipart")) {
			MwChunk.FMPdata();
		}
		
		MwAPI.registerDataProvider("Slime", new OverlaySlime());
		MwAPI.registerDataProvider("Grid", new OverlayGrid());
		MwAPI.registerDataProvider("Faction", new OverlayFaction());
		MwAPI.registerDataProvider("Faction/Grid", new OverlayFactionGrid());
	}
}
