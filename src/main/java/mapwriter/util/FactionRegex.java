package mapwriter.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FactionRegex {
	private static final Map<String, String> facRegex = new HashMap<String, String>();
	private static final Map<String, String> facDefaults = new HashMap<String, String>();
	private static final Map<String, Pattern> facPattern = new HashMap<String, Pattern>();

	static {
		FactionRegex.addRegex("mapHeader", "_{1,20}\\.\\[ \\((.+)\\) (.+) \\]\\.");
		FactionRegex.addRegex("showHeader", "_{2,30}\\.\\[ ([^\\(].*) \\]\\.");
		FactionRegex.addRegex("desc", "Description: (.*)");
		FactionRegex.addRegex("pOnline", "(Followers Online|Members online) \\((\\d+)\\):( (?:.+)|)");
		FactionRegex.addRegex("pOffline", "(Followers Online|Members offline) \\(\\d+\\): ((?:.+))");
		FactionRegex.addRegex("landPow", "Land \\/ Power \\/ Maxpower:  (\\d+)\\/(\\d+)\\/(\\d+)");
		FactionRegex.addRegex("facClosed", "Joining: invitation is required");
		FactionRegex.addRegex("allies", "Allied to: (.*)");
		FactionRegex.addRegex("enemies", "Enemies: (.*)");
		FactionRegex.addRegex("truces", "In Truce with: (.*)");
		FactionRegex.addRegex("wealth", "Total wealth: (.*)");
	}

	public static void addRegex(String name, String regex) {
		if (!FactionRegex.facDefaults.containsKey(name)) {
			FactionRegex.facDefaults.put(name, regex);
		}

		FactionRegex.facRegex.put(name, regex);
		FactionRegex.facPattern.put(name, Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL));
	}

	public static void clear() {
		FactionRegex.facPattern.clear();
		FactionRegex.facRegex.clear();

		Iterator<String> itr = FactionRegex.facDefaults.keySet().iterator();

		while (itr.hasNext()) {
			String name = itr.next();

			FactionRegex.addRegex(name, FactionRegex.facDefaults.get(name));
		}
	}

	public static Set<String> keys() {
		return FactionRegex.facRegex.keySet();
	}
	
	public static String getRegex(String name) {
		return FactionRegex.facRegex.get(name);
	}
	
	public static Pattern getPattern(String name) {
		return FactionRegex.facPattern.get(name);
	}
	
	public boolean didRegex(String line, String regexName) {
		Pattern regex = FactionRegex.getPattern(regexName);
		if (regex == null) return false;
		Matcher m = regex.matcher(line);
		if (m.find()) return true;
		return false;
	}
	
	public boolean didFactionShow(String line) {
		Matcher m = FactionRegex.facPattern.get("showHeader").matcher(line);
		if (m.find()) return true;
		return false;
	}
	
	public boolean didFactionMap(String line) {
		Matcher m = FactionRegex.facPattern.get("mapHeader").matcher(line);
		if (m.find()) return true;
		return false;
	}
}