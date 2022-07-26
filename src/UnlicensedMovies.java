import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class UnlicensedMovies {

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
					if (intCount == 1) startRank = i;
					else if (intCount == 2) endRank = i;
				} catch (NumberFormatException e) {
					if (arg.equals("-overwrite") || arg.equals("-o")) 
						overwrite = true;
					else file = arg;
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
		if (args.overwrite) out.println("rank,title,url,licensors");

		// String baseUrl = "https://myanimelist.net/topanime.php?type=tv&limit=";
		String baseUrl = "https://myanimelist.net/topanime.php?limit=";
		for (int i = args.startRank; i <= args.endRank; i+= 50) {
			Document doc = Jsoup.connect(baseUrl + (i-1)).get();
			doc.selectFirst("#content > div.pb12 > table.top-ranking-table > tbody").children().stream()
				.filter((child) -> child.classNames().contains("ranking-list"))
				.filter((child) -> {
					int rank = Integer.parseInt(child.selectFirst("td.rank > span").ownText());
					return rank >= args.startRank && rank <= args.endRank;
				})
				.map((child) -> child.selectFirst("td.title > a").attr("href"))
				.forEach((url) -> {
					try {
						Anime anime = new Anime(Jsoup.connect(url).get());

						if (out == null) {
							System.out.println(anime.toCSV());
						} else {
							System.out.println(anime);
							out.println(anime.toCSV());
						}
					} catch (Exception e) {
						System.err.println("Error while parsing anime: " + url);
						e.printStackTrace();
					}
				});
		}
		if (out != null) out.close();
	}
}
