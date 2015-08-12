package genesis.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONObject;

import genesis.Genesis;

public class WikipediaFinder {
	
	public static String getSummary(String name) {
		String simple = getSummary(name, true);
		if (simple != null)
			return simple;
		return getSummary(name, false);
	}
	
	private static String getSummary(String name, boolean simple) {
		try {
			name = matchArticle(name, simple);
			if(name == null)
				return null;
			URLConnection conn = new URL("https://" + (simple ? "simple":"en") + ".wikipedia.org/w/api.php?action=query&titles=" + name.replace(" ", "%20") + "&prop=revisions&rvprop=content&format=json").openConnection();
			conn.setRequestProperty("User-Agent", Genesis.name + "/" + Genesis.version + " (doomsdaysurvivor2013@gmail.com)");
			conn.connect();
			BufferedReader r = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			JSONObject pages = new JSONObject(r.readLine()).getJSONObject("query").getJSONObject("pages");
			JSONObject latestRevision = (JSONObject) pages.getJSONObject(JSONObject.getNames(pages)[0]).getJSONArray("revisions").get(0);
			String content = latestRevision.getString("*");
			if(!content.contains("'''"))
				return null;
			return format(content.split("'''")[1] + GenesisUtil.join(content.split("'''"), "", 2).split("\\.\\s")[0].trim());
		} catch(IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static String matchArticle(String name, boolean simple) {
		try {
			URLConnection conn = new URL("https://" + (simple ? "simple":"en") + ".wikipedia.org/wiki/Special:Search/" + name).openConnection();
			conn.setRequestProperty("User-Agent", Genesis.name + "/" + Genesis.version + " (doomsdaysurvivor2013@gmail.com)");
			conn.connect();
			BufferedReader r = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while((line = r.readLine()) != null) {
				if (line.contains("<title>")) {
					r.close();
					if(line.contains("Search results for \""))
						return null;
					return line.replace("<title>", "").replace("Simple English ", "").replace(" - Wikipedia, the free encyclopedia</title>", "").trim();
				}
			}
			r.close();
			return null;
		} catch(IOException e) {
			return null;
		}
	}
	
	private static String format(String s) {
		return s.replace("'''", "").replace("''", "").replaceAll("\\[\\[(?:[^|\\]]*\\|)?(.*?)\\]\\]", "$1").replaceAll("\\(.+?\\)", "").trim();
	}
}
