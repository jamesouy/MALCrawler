// import java.util.function.;

import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Anime {

	public static final String CSV_HEADER = "title,genres,year,score,members,episodes,licensors";
	public String url;
	public String title;
	public int rank;
	public String score;
	public String members;
	public int year;
	public String episodes;
	public List<String> genreTheme;
	public List<String> licensors;

	// gets text next to a key in the left info side-bar
	private static String getText(Element info, String key) throws Exception {
		Element e = info.selectFirst("div.spaceit_pad:has(span:contains(" + key + ":))");
		if (e == null) throw new Exception("Cannot find info element for key \"" + key + "\"");
		return e.ownText().trim();
	}

	// gets list of text in urls for a key in left info side-bar
	private static List<String> getUrls(Element info, String key) {
		return info.select("div.spaceit_pad > span:contains(" + key + ":) ~ a").stream()
			.map((element) -> element.text().trim())
			.collect(Collectors.toList());
	}

	public Anime(Document doc) throws Exception {

		Element info = doc.selectFirst("#content > table > tbody > tr > td > div.leftside");
		
		rank = Integer.parseInt(getText(info, "Ranked").substring(1));
		
		title = doc.select("#contentWrapper > div > div.edit-info > div.h1-title > div > h1.title-name > strong").text();
		
		Matcher matcher = Pattern.compile("https://myanimelist\\.net/anime/\\d+").matcher(doc.location());
		url = matcher.find() ? matcher.group() : doc.location();
		
		// licensors = getUrls(info, "Licensors");
		licensors = getUrls(info, "Licensors").stream()
			.filter(licensor -> !licensor.equals("add some"))
			.collect(Collectors.toList());

		score = doc.selectFirst("div.anime-detail-header-stats > div.stats-block > div.score > div.score-label").text();
		members = getText(info,  "Members");

		// year = Integer.parseInt(getUrls(info, "Premiered").get(0).split(" ")[1]);
		String[] yearData = getText(info, "Aired").split(" ");
		// if (yearData.length > 2) year = Integer.parseInt(yearData[2]);
		// else year = Integer.parseInt(yearData[1]);
		year = Integer.parseInt(yearData[yearData.length > 2 ? 2 : 1]);
		episodes = getText(info, "Episodes");

		genreTheme = getUrls(info, "Genre");
		genreTheme.addAll(getUrls(info, "Genres"));
		genreTheme.addAll(getUrls(info, "Theme"));
		genreTheme.addAll(getUrls(info, "Themes"));
	}

	public String toString() {
		return "#" + rank + ": " + title + " (" + url + ")";
	}

	private String addQuotes(String s) { return "\"" + s + "\""; }
	private String dupeQuotes(String s) { return s.replace("\"", "\"\""); }

	// returns row in CSV format
	public String toCSV() {
		return addQuotes(String.join("\",\"", Arrays.asList(
			"=HYPERLINK(\"" + url + "\",\"" + dupeQuotes(title) + "\")",
			String.join(",", genreTheme),
			year+"",
			score,
			members,
			episodes,
			String.join(",", licensors)
		).stream()
			.map(this::dupeQuotes)
			.collect(Collectors.toList())
		));
	}
}
