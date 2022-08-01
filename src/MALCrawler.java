import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MALCrawler {

	private static class Args {
		public int startRank = 0;
		public int endRank = 0;
		public String file = "";
		public boolean overwrite = false;

		public Args(String[] arguments) {
			List<String> args = Arrays.asList(arguments);
			int intCount = 0;
			for (String arg : args) {
				try {
					int i = Integer.parseInt(arg);
					intCount++;
					if (intCount == 1) startRank = i; // first int is start rank
					else if (intCount == 2) endRank = i; // second int is end rank
				} catch (NumberFormatException e) {
					if (arg.equals("-overwrite") || arg.equals("-o"))
						overwrite = true; // use -o to overwrite the file
					else file = arg; // last string is output csv filename
				}
			}
			if (startRank < 1) startRank = 1;
			if (endRank < startRank) endRank = startRank;
		}
	}

	public static void main(String[] arguments) throws Exception {

		Args args = new Args(arguments);

		PrintWriter out = args.file.isEmpty() ? null
				: new PrintWriter(new FileWriter(args.file, !args.overwrite), true);
		if (args.overwrite) out.println(Anime.CSV_HEADER);

		// String baseUrl = "https://myanimelist.net/topanime.php?type=tv&limit=";
		String baseUrl = "https://myanimelist.net/topanime.php?limit=";
		for (int i = args.startRank; i <= args.endRank; i += 50) {
			Document doc = Jsoup.connect(baseUrl + (i - 1)).get();
			Elements animeList = doc.selectFirst("#content > div.pb12 > table.top-ranking-table > tbody").children();
			for (Element element : animeList) {
				if (!element.classNames().contains("ranking-list")) continue; // ignore header

				int rank = Integer.parseInt(element.selectFirst("td.rank > span").ownText());
				if (rank < args.startRank || rank > args.endRank) continue; // ignore rank out of specified range

				String url = element.selectFirst("td.title > a").attr("href");
				try {
					Anime anime = new Anime(Jsoup.connect(url).get());
	
					if (out == null) {
						System.out.println(anime.toCSV());
					} else {
						System.out.println(anime);
						out.println(anime.toCSV());
					}
				} catch (Exception e) {
					System.err.println("Error on anime: " + url);
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
		if (out != null) out.close();
	}
}
