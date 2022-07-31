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
	public int score;
	public int members;
	public int year;
	public String episodes;
	public List<String> genreTheme;
	public List<String> licensors;

	private static String getText(Element info, String key) throws Exception {
		Element e = info.selectFirst("div.spaceit_pad:has(span:contains(" + key + ":))");
		if (e == null) throw new Exception("Cannot find info element for key \"" + key + "\"");
		return e.ownText().trim();
	}

	private static List<String> getUrls(Element info, String key) {
		return info.select("div.spaceit_pad > span:contains(" + key + ":) ~ a").stream()
				.map((element) -> element.text().trim()).collect(Collectors.toList());
	}

	public Anime(Document doc) throws Exception {

		Element info = doc.selectFirst("#content > table > tbody > tr > td > div.leftside");

		rank = Integer.parseInt(getText(info, "Ranked").substring(1));

		title = doc.select("#contentWrapper > div > div.edit-info > div.h1-title > div > h1.title-name > strong").text();

		// Url
		Matcher matcher = Pattern.compile("https://myanimelist\\.net/anime/\\d+").matcher(doc.location());
		url = matcher.find() ? matcher.group() : doc.location();

		// Licensing
		licensors = getUrls(info, "Licensors");
		members = Integer.parseInt(getText(info, "Members"));
		year = Integer.parseInt(getUrls(info, "Premiered").get(0).split(" ")[1]);
		episodes = getText(info, "Episodes");
		genreTheme = getUrls(info, "Genre");
		genreTheme = getUrls(info, "Genres");
		genreTheme.addAll(getUrls(info, "Theme"));
		genreTheme.addAll(getUrls(info, "Themes"));
	}

	public String toString() {
		return "#" + rank + ": " + title + " (" + url + ")";
	}

	private String addQuotes(String s) {
		return "\"" + s + "\"";
	}

	public String toCSV() {
		return String.join(",", Arrays.asList(
				rank + "",
				addQuotes(title),
				addQuotes(url),
				addQuotes(String.join(",", licensors))));
	}
}
