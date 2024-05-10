package info.kgeorgiy.ja.karpukhin.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

// :NOTE: javadoc

public class WebCrawler implements NewCrawler {
    private final ExecutorService downloaders;
    private final ExecutorService extractors;
    private final Downloader downloader;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloaders = Executors.newFixedThreadPool(downloaders);
        this.extractors = Executors.newFixedThreadPool(extractors);
        this.downloader = downloader;
    }

    @Override
    public Result download(String url, int depth, Set<String> excludes) {
        Phaser phaser = new Phaser(1);
        Set<String> downloaded = ConcurrentHashMap.newKeySet();
        Queue<String> urls = new ConcurrentLinkedQueue<>();
        Set<String> used = ConcurrentHashMap.newKeySet();
        Map<String, IOException> errors = new ConcurrentHashMap<>();
        urls.add(url);
        recursiveDownload(urls, downloaded, errors, depth, phaser, used, excludes);
        return new Result(new ArrayList<>(downloaded), errors);
    }

    private void recursiveDownload(Queue<String> urls, Set<String> downloaded, Map<String, IOException> errors, int depth, Phaser phaser, Set<String> used, Set<String> excludes) {
        if (depth == 0) {
            return;
        }
        Queue<String> nextUrls = new ConcurrentLinkedQueue<>();
        for (String url : urls) {
            if (!used.add(url) || excludes.stream().anyMatch(url::contains)) {
                continue;
            }
            phaser.register();
            downloaders.submit(() -> {
                try {
                    final Document document = downloader.download(url);
                    downloaded.add(url);
                    phaser.register();
                    extractors.submit(() -> {
                        try {
                            nextUrls.addAll(document.extractLinks());
                        } catch (IOException e) {
                            errors.put(url, e);
                        } finally {
                            phaser.arriveAndDeregister();
                        }
                    });
                } catch (IOException e) {
                    errors.put(url, e);
                } finally {
                    phaser.arriveAndDeregister();
                }
            });
        }
        phaser.arriveAndAwaitAdvance();
        recursiveDownload(nextUrls, downloaded, errors, depth - 1, phaser, used, excludes);
    }

    @Override
    public void close() {
        downloaders.shutdown();
        extractors.shutdown();
    }

    public static void main(String[] args) {
        if (args == null || args.length == 0 || args.length > 5) {
            System.err.println("Usage: WebCrawler url [depth [downloads [extractors [perHost]]]]");
            return;
        }

        String url = args[0];
        int depth = args.length > 1 ? Integer.parseInt(args[1]) : 1;
        int downloads = args.length > 2 ? Integer.parseInt(args[2]) : Runtime.getRuntime().availableProcessors();
        int extractors = args.length > 3 ? Integer.parseInt(args[3]) : Runtime.getRuntime().availableProcessors();
        int perHost = args.length > 4 ? Integer.parseInt(args[4]) : Integer.MAX_VALUE;

        try {
            Downloader downloader = new CachingDownloader(1);
            try (Crawler crawler = new WebCrawler(downloader, downloads, extractors, perHost)) {
                Result result = crawler.download(url, depth);
                result.getDownloaded().forEach(System.out::println);
            }
        } catch (IOException e) {
            System.err.println("Error during crawling: " + e.getMessage());
        }
    }
}
