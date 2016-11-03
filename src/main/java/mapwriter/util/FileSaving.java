package mapwriter.util;

import com.google.gson.*;
import mapwriter.Mw;
import mapwriter.fac.Faction;

import java.io.*;
import java.util.Collection;

public class FileSaving {
	private File factionsDir = null;
	private boolean savingFactions = false;

	private Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
	public FileSaving() {}
	
	public void load(File directory) {
		this.factionsDir = new File(directory, "factions");
		if (!this.factionsDir.exists()) this.factionsDir.mkdir();
		else this.loadFactions();
	}
	
	private void loadFactions() {
		new Thread(new LoadThread(), "Factions Loading").start();
	}
	
	public void saveFactions() {
		this.savingFactions = true;
		new Thread(new SaveThread(), "Factions Saving").start();
	}

	private class LoadThread implements Runnable {
		@Override
		public void run() {
			while (savingFactions) {}

			File[] facsFile = factionsDir.listFiles();
			if (facsFile == null) return;
			Mw.getInstance().facInput.locked = true;

			for (File fac : facsFile) {
				try {
					BufferedReader loader = new BufferedReader(new FileReader(fac));
					JsonObject json = (JsonObject)new JsonParser().parse(loader);
					loader.close();

					String name 	= json.get("Name").getAsString();
					String color 	= json.get("Color").getAsString();
					boolean cColor 	= json.has("CustomColor") && json.get("CustomColor").getAsBoolean();
					String image 	= json.has("Image") ? json.get("Image").getAsString() : "";
					
					// A check because I was retarded and used a json object instead of an array.
					if (json.get("Claims").isJsonObject()) {
						int amount 			= json.get("ClaimAmount").getAsInt();
						JsonObject claim 	= json.get("Claims").getAsJsonObject();

						for (int i = 0; i < amount; i++) {
							JsonObject c = claim.get("Claim " + i).getAsJsonObject();
							Mw.getInstance().facInput.addOrEditFaction(new Faction(name, color, cColor, image), c.get("x").getAsInt(), c.get("z").getAsInt());
						}
					} else {
						JsonArray claims = json.get("Claims").getAsJsonArray();

						for (int i = 0; i < claims.size(); i++) {
							JsonObject claim = claims.get(i).getAsJsonObject();
							Mw.getInstance().facInput.addOrEditFaction(new Faction(name, color, cColor, image), claim.get("x").getAsInt(), claim.get("z").getAsInt());
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			for (Faction fac : Mw.getInstance().facInput.factions.values()) {
				for (Faction.Claim claim : fac.getClaims()) {
					claim.update();
				}
			}

			Mw.getInstance().facInput.locked = false;
		}
	}
	
	private class SaveThread implements Runnable {
		@Override 
		public void run() {
			Collection<Faction> coll = Mw.getInstance().facInput.factions.values();
			Faction[] factions = coll.toArray(new Faction[coll.size()]);

			for (Faction fac : factions) {
				try {
					File facFile = new File(factionsDir, fac.getName() + ".txt");
					if (!facFile.exists()) facFile.createNewFile();

					JsonObject jFaction = new JsonObject();
					jFaction.addProperty("Name", fac.getName());
					jFaction.addProperty("Color", fac.getColor());
					jFaction.addProperty("CustomColor", fac.hasCustomColor());
					jFaction.addProperty("Image", fac.getTexture() == null ? "" : fac.getTexture().toString());

					JsonArray jClaims = new JsonArray();

					for (Faction.Claim claim : fac.getClaims()) {
						JsonObject jClaim = new JsonObject();
						jClaim.addProperty("x", claim.getX());
						jClaim.addProperty("z", claim.getZ());
						jClaims.add(jClaim);
					}

					jFaction.add("Claims", jClaims);
					
					PrintWriter save = new PrintWriter(new FileWriter(facFile));
					save.println(gson.toJson(jFaction));
					save.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			Mw.getInstance().facInput.facCoords.clear();
			Mw.getInstance().facInput.factions.clear();

			savingFactions = false;
		}
	}
}