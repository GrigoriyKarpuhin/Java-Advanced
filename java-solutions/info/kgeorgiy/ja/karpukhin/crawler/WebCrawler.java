package info.kgeorgiy.ja.karpukhin.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class WebCrawler implements Crawler {
    private final ExecutorService downloaders;
    private final ExecutorService extractors;
    private final Downloader downloader;

    public WebCrawler(Downloader downloader, int downloaders, int extractors) {
        this.downloaders = Executors.newFixedThreadPool(downloaders);
        this.extractors = Executors.newFixedThreadPool(extractors);
        this.downloader = downloader;
    }

    @Override
    public Result download(String url, int depth) {
        Set<String> downloaded = new ConcurrentSkipListSet<>();
        Map<String, IOException> errors = new ConcurrentHashMap<>();
        downloadHelper(url, depth, downloaded, errors);
        return new Result(new ArrayList<>(downloaded), errors);
    }

    @Override
    public void close() {
        downloaders.shutdown();
        extractors.shutdown();
    }

    private void downloadHelper(String url, int depth, Set<String> downloaded, Map<String, IOException> errors) {
        if (depth == 0 || downloaded.contains(url)) {
            return;
        }
        downloaded.add(url);
        try {
            Document document = downloader.download(url);
            List<String> links = document.extractLinks();
            for (String link : links) {
                downloadHelper(link, depth - 1, downloaded, errors);
            }
        } catch (IOException e) {
            errors.put(url, e);
        }

    }

    public static void main(String[] args) {
        if (args == null || args.length < 1 || args.length > 4) {
            System.err.println("Usage: WebCrawler url [depth [downloads [extractors]]]");
        }
    }
}
