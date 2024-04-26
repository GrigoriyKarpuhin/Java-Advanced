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
        return null;
    }

    @Override
    public void close() {
        downloaders.shutdown();
        extractors.shutdown();
    }

    public static void main(String[] args) {
        if (args == null || args.length == 0 || args.length > 5) {
            System.err.println("Usage: WebCrawler url [depth [downloads [extractors [perHost]]]]");
        }
    }
}
