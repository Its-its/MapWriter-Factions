package mapwriter.config;

import mapwriter.Mw;
import mapwriter.util.FactionRegex;
import mapwriter.util.Logging;
import mapwriter.util.Reference;
import mapwriter.util.Utils;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.util.*;

public class WorldConfig {
	private static WorldConfig instance = null;

	public Configuration worldConfiguration = null;

	// list of available dimensions
	public List<Integer> dimensionList = new ArrayList<Integer>();


	// Multiverse Worlds registered.
	public List<String> worldList = new ArrayList<String>();
	public String[] worldListDefault = new String[0];

	public String worldName = "none";

	// Use "Multiverse"
	private boolean usingMultiverseDefault = false;
	public boolean usingMultiverse = usingMultiverseDefault;

	private WorldConfig() {
		// load world specific config file
		Logging.log("Loading WorldConfig");
		File worldConfigFile = new File(Mw.getInstance().worldDir, Reference.worldDirConfigName);
		this.worldConfiguration = new Configuration(worldConfigFile);

		this.init();
	}

	public void init() {
		// Dimension List
		FactionRegex.clear();
		this.dimensionList.clear();

		this.worldConfiguration.get(Reference.catWorld, "dimensionList", Utils.integerListToIntArray(this.dimensionList));
		this.addDimension(0);
		this.cleanDimensionList();

		// Options
		if (this.worldConfiguration.hasCategory(Reference.catOptions)) {
			this.usingMultiverse = this.worldConfiguration.get(Reference.catOptions, "multiverse", this.usingMultiverseDefault).getBoolean();
		}

		// Regex
		if (this.worldConfiguration.hasCategory(Reference.catRegex)) {
			Collection<String> keys = this.worldConfiguration.getCategory(Reference.catRegex).keySet();
			Iterator<String> itr = keys.iterator();

			while (itr.hasNext()) {
				String name = itr.next();
				FactionRegex.addRegex(name, this.worldConfiguration.get(Reference.catRegex, name, FactionRegex.getRegex(name)).getString());
			}
		}

		// Worlds
		if (this.worldConfiguration.hasCategory(Reference.catMultiverse)) {
			Collections.addAll(this.worldList, this.worldConfiguration.get(Reference.catMultiverse, "worldList", this.worldListDefault).getStringList());
		}
	}

	public void saveWorldConfig() {
		// Options
		this.worldConfiguration.get(Reference.catOptions, "multiverse", this.usingMultiverseDefault).set(this.usingMultiverse);

		// Multiverse
		String[] worlds = this.worldList.toArray(new String[this.worldList.size()]);
		this.worldConfiguration.get(Reference.catMultiverse, "worldList", this.worldListDefault).set(worlds);

		// Regex
		Iterator<String> itr = FactionRegex.keys().iterator();

		while (itr.hasNext()) {
			String name = itr.next();
			this.worldConfiguration.get(Reference.catRegex, name, "").set(FactionRegex.getRegex(name));
		}


		// Save
		if (this.worldConfiguration.hasChanged()) {
			this.worldConfiguration.save();
		}
	}

	public void addDimension(int dimension) {
		int i = this.dimensionList.indexOf(dimension);
		if (i < 0) this.dimensionList.add(dimension);
	}

	public void cleanDimensionList() {
		List<Integer> dimensionListCopy = new ArrayList<Integer>(this.dimensionList);
		this.dimensionList.clear();
		for (int dimension : dimensionListCopy) {
			this.addDimension(dimension);
		}
	}

	public static WorldConfig getInstance() {
		if (WorldConfig.instance == null) {
			synchronized (WorldConfig.class) {
				if (WorldConfig.instance == null) {
					WorldConfig.instance = new WorldConfig();
				}
			}
		}

		return WorldConfig.instance;
	}

	public void nullify() {
		Logging.log("Closing WorldConfig");
		WorldConfig.instance = null;
	}
}