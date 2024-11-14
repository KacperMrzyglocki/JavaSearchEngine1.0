package com.example;

import org.openjdk.jmh.annotations.*;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class BenchmarkTest {
    private final InvertedIndex index = new InvertedIndex();

    @Benchmark
    public void testFetchBooks() {
        crawler.fetchBooks("adventure", "en", "books.json");
    }

    @Benchmark
    public void testBuildIndex() {
        index.buildIndex("books.json");
    }
}
