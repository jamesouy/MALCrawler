# MALCrawler

Crawl through rank 1-50:
```
java -cp bin:lib/jsoup-1.14.3.jar MALCrawler 1 50
```

Save rank 1-50 to file in CSV format:
```
java -cp bin:lib/jsoup-1.14.3.jar MALCrawler 1 50 anime.csv
```

Append existing CSV file with rank 51-100:
```
java -cp bin:lib/jsoup-1.14.3.jar MALCrawler 51 100 anime.csv
```

Replace existing CSV file with rank 101-200
```
java -cp bin:lib/jsoup-1.14.3.jar MALCrawler 101 200 anime.csv -override
```
