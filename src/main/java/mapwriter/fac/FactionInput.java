package mapwriter.fac;

import mapwriter.Mw;
import mapwriter.fac.Faction.Claim;
import mapwriter.util.ColorCodes;
import mapwriter.util.FactionRegex;
import mapwriter.util.HashMapXY;
import mapwriter.util.Logging;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StringUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class FactionInput {
	private FactionRegex fr = new FactionRegex();

	public boolean locked = false;
	public boolean enabled = true;
	public boolean showMap = true;

	// Stores Chat Faction Map - Top to Bottom
	public String[] factionMap = new String[0];
	
	// Stores Ascii code and Faction Name with its Color code Prepending it.
	public Map<String, String> factionNames = new LinkedHashMap<String, String>();
	
	// Current position of faction map row showing.
	public int pos = 0;
	
	// Length of Faction Map (Center[+] * 2)
	public int length = 14;
	public int maxLength = 40;
	
	public HashMapXY<Integer, String> facCoords = new HashMapXY<Integer, String>();
	
	//Changed to TreeMap for String.CASE_INSENSITIVE_ORDER
	public Map<String, Faction> factions = new TreeMap<String, Faction>(String.CASE_INSENSITIVE_ORDER);
	
	public void removeFaction(Faction faction) {
		for(Claim claim : faction.getClaims()) {
			this.setClaim(null, claim.getX(), claim.getZ());
		}
		this.factions.remove(faction.getName());
	}
	
	public void changeFactionName(String oldName, String newName) {
		if (oldName.equalsIgnoreCase(newName)) return;
		
		System.out.println(oldName + " -> " + newName);
		
		Faction newFaction = this.getFaction(newName);
		
		//Overwriting a faction.
		if (newFaction != null) {
			Faction oldFaction = this.getFaction(oldName);
			
			System.out.println("Transferring claims from " + oldName + " -> " + newName);
			
			//Add claims to new faction from old faction.
			
			for(Claim claim : oldFaction.getClaims()) {
				newFaction.addClaim(claim.getX(), claim.getZ());
			}
			
			System.out.println("Added " + oldFaction.getClaims().size() + " claim(s) to " + newName + " totaling to " + newFaction.getClaims().size());
			
			//Remove old faction.
			this.factions.remove(oldName);
			
			//Update the faction.
			for(Claim claim : newFaction.getClaims()) {
				claim.update();
			}
		} else {
			newFaction = Faction.createCopy(this.getFaction(oldName));
			newFaction.setName(newName);
			
			this.factions.remove(oldName);
			this.factions.put(newName, newFaction);
		}
	}
	
	public void addOrEditFaction(Faction faction, int x, int z) {
		//See if claims already there.
		Faction remFac = this.getFaction(x, z);
		
		//faction equals null, remove any claim at the x, z
		if (faction == null) {
			if (remFac == null) return;
			
			remFac.remClaim(x, z);
			this.setClaim(null, x, z);
			return;
		}
		
		if (!this.factions.containsKey(faction.getName())) {
			this.factions.put(faction.getName(), faction);
		}
		
		// If Claim is there, remove it from other faction.
		if(remFac != null) remFac.remClaim(x, z);

		this.setFactionClaim(faction.getName(), x, z);

		Faction fac = this.getFaction(faction.getName());

		// Update color if user hasn't changed it manually.
		if (!fac.hasCustomColor()) fac.updateColor(faction.getColor());
	}
	
	public void setFactionClaim(String name, int x, int z) {
		this.getFaction(name).addClaim(x, z);
	}
	
	public void setClaim(String name, int x, int z) {
		if(name == null) this.facCoords.removeValue(x, z);
		else this.facCoords.put(x, z, name);
	}
	
	public String getFactionName(int x, int z) {
		return this.facCoords.get(x, z);
	}
	
	public Faction getFaction(int x, int z) {
		String name = this.getFactionName(x, z);
		if (name == null) return null;
		return this.factions.get(name);
	}
	
	public Faction getFaction(String name) {
		return this.factions.get(name);
	}
	
	public Faction.Claim getClaim(int x, int z) {
		Faction fac = this.getFaction(x, z);
		if (fac == null) return null;
		return fac.getClaim(x, z);
	}
	
	private void unsetMap() {
		this.pos = 0;
		this.factionMap = new String[0];
		this.length = 14;
	}
	
	public boolean isFactionMap(IChatComponent chat) {
		if (!this.enabled) return false;

		String cText = chat.getFormattedText();
		String uText = chat.getUnformattedText();
		
		// Temporary fix; if the map stops detecting, it will unset it.
		if (this.pos > this.maxLength) this.unsetMap();
		
		if (this.fr.didFactionMap(uText) || this.pos != 0) {
			if (this.pos == 0) this.unsetMap();
			
			else if (this.pos > 0 && this.pos < 4) {
				uText = uText.substring(3, uText.length());
				uText = "```" + uText;
			}
			
			if (uText.contains("+"))
				this.length = this.pos * 2;
			
			this.pos++;
			this.factionMap = ArrayUtils.add(this.factionMap, (this.pos >= this.length || this.pos == 1) ? cText : uText);
			
			if (this.pos >= this.length) {
				this.pos = 0;
				
				if (this.factionMap.length > 1) {
					// Split Faction Char and Names.
					String[] factions = this.factionMap[this.factionMap.length - 1].split(" ");
					String[] coords = this.factionMap[0].split(" ")[1].replace("(", "").replace(")", "").split(",");
					coords[0] = StringUtils.stripControlCodes(coords[0]);
					
					this.factionNames.clear();
					
					this.saveFactionMap(factions, coords);
				}
			}
			return true;
		}
		return false;
	}
	
	private void saveFactionMap(String[] factionLegend, String[] coords) {
		if (factionLegend.length >= 2) {
			// TreeMap: factionNames; Ascii Code, Faction Name
			
			// ascii = pos * 2, name = pos * 2 + 1
			for (int pos = 0; pos < factionLegend.length / 2; pos++) {
				int facNamePos = (pos * 2);
				String colorCode = factionLegend[facNamePos].replace("\u00A7r", "").substring(0, 2);
				String asciiSymbol = StringUtils.stripControlCodes(factionLegend[facNamePos]).replace(":", "");
				String factionName = colorCode + StringUtils.stripControlCodes(factionLegend[facNamePos + 1]);

				this.factionNames.put(asciiSymbol, factionName);
			}
			
			String currentLand = this.factionMap[0].split(" ")[2].replace("\u00A7r", "");
			String currentColor = currentLand.substring(0, 2);
			currentLand = currentColor + StringUtils.stripControlCodes(currentLand);
			
			this.factionNames.put("+", currentLand);
			
			int xCurrentChunk = Integer.parseInt(coords[0]);
			int zCurrentChunk = Integer.parseInt(coords[1]);
			int xCornerChunk = xCurrentChunk - (this.factionMap[1].length() / 2);
			int zCornerChunk = zCurrentChunk - ((this.factionMap.length / 2) - 1);

			for(int mapY = 1; mapY < this.factionMap.length - 1; mapY++) {
				String facLine = this.factionMap[mapY];
				
				for(int mapX = 0; mapX < facLine.length(); mapX++) {
					int chunkX = (xCornerChunk + mapX);
					int chunkZ = (zCornerChunk + mapY - 1);
					
					String charAtXY = String.valueOf(facLine.charAt(mapX));
					
					//Continue if compass portion of map
					if (charAtXY.equals("`")) continue;
					
					charAtXY = (this.factionNames.keySet().contains(charAtXY) ? this.factionNames.get(charAtXY) : "-");
					
					String factionName = StringUtils.stripControlCodes(charAtXY);
					
					if (factionName.equals("-") || factionName.equals("Wilderness")) {
						this.addOrEditFaction(null, chunkX, chunkZ);
					} else {
						String colorCode = charAtXY.substring(0, 2).replace("\u00A7", "");
						this.addOrEditFaction(new Faction(factionName, ColorCodes.getColor(colorCode)), chunkX, chunkZ);
					}
				}
			}

			new Thread(new Runnable() {
				@Override
				public void run() {
					int updates = 0;

					for (String name : Mw.getInstance().facInput.factionNames.values()) {
						Faction fac = Mw.getInstance().facInput.getFaction(StringUtils.stripControlCodes(name));
						if (fac != null) {
							for (Claim claim : fac.getClaims()) {
								claim.update();
								updates++;
							}
						}
					}

					Logging.log("Claim updates: " + updates);
				}
			}, "Claim Updates").start();
		}
	}
}