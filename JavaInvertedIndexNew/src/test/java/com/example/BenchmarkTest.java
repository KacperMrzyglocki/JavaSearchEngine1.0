package com.example;

import org.openjdk.jmh.annotations.*;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class BenchmarkTest {
    private final InvertedIndexFolders index = new InvertedIndexFolders();

    @Setup(Level.Trial)
    public void setup() {
        // Setup code here, if needed
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        // Teardown code here, if needed
    }

    @Benchmark
    public void testBuildIndex() {
        index.buildIndex("books.json");
    }

    @Benchmark
    public void testSerialization() {
        index.serialize();
    }
}
