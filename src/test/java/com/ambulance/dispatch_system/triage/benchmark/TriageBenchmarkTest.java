package com.ambulance.dispatch_system.triage.benchmark;

import com.ambulance.dispatch_system.triage.entity.TriageAssessment;
import com.ambulance.dispatch_system.triage.model.dto.TriageRequestDTO;
import com.ambulance.dispatch_system.triage.model.enums.ConsciousnessLevel;
import com.ambulance.dispatch_system.triage.model.enums.TriageCategory;
import com.ambulance.dispatch_system.triage.service.impl.algorithms.KNNClassifier;
import com.ambulance.dispatch_system.triage.service.impl.algorithms.MTSDecisionTree;
import com.ambulance.dispatch_system.triage.service.impl.algorithms.WeightedScoringStrategy;
import com.ambulance.dispatch_system.triage.util.PriorityDispatchQueue;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Benchmarking suite to compare algorithms.
 * Note: Falling back to System.nanoTime() with warm-up iterations to ensure 
 * strict control over the custom CSV output format requested for the report.
 */
public class TriageBenchmarkTest {

    private final MTSDecisionTree mtsDecisionTree = new MTSDecisionTree();
    private final WeightedScoringStrategy scoringStrategy = new WeightedScoringStrategy();
    private final KNNClassifier knnClassifier = new KNNClassifier();

    private static final int WARMUP_ITERATIONS = 5;
    private static final int TIMED_RUNS = 10;
    private static final int[] N_VALUES = {100, 1000, 10000, 100000};

    // Remove @Disabled to run the benchmark locally. It's disabled by default so 
    // it doesn't slow down the regular CI/CD build unless explicitly requested.
    @Test
    public void runBenchmarks() throws IOException {
        knnClassifier.initSyntheticDataset();

        System.out.println("Starting Benchmarks...");
        System.out.printf("%-20s | %-10s | %-15s | %-15s%n", "Algorithm", "N", "Avg Time (ms)", "Memory Diff (KB)");
        System.out.println("-".repeat(68));

        try (PrintWriter writer = new PrintWriter(new FileWriter("benchmark-results.csv"))) {
            writer.println("algorithm,N,avg_time_ms,memory_delta_kb");

            for (int n : N_VALUES) {
                List<TriageRequestDTO> requests = generateRequests(n);

                benchmarkMTS(requests, n, writer);
                benchmarkWeightedHeap(requests, n, writer);
                benchmarkKNN(requests, n, writer);
            }
        }
        System.out.println("Benchmarks complete. Results saved to benchmark-results.csv");
    }

    private void benchmarkMTS(List<TriageRequestDTO> requests, int n, PrintWriter writer) {
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            for (TriageRequestDTO req : requests) {
                mtsDecisionTree.evaluate(req);
            }
        }

        // Measure
        System.gc();
        long memBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long totalTimeNs = 0;

        for (int i = 0; i < TIMED_RUNS; i++) {
            long start = System.nanoTime();
            for (TriageRequestDTO req : requests) {
                mtsDecisionTree.evaluate(req);
            }
            totalTimeNs += (System.nanoTime() - start);
        }

        long memAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        
        double avgTimeMs = (totalTimeNs / (double) TIMED_RUNS) / 1_000_000.0;
        double memDiffKb = Math.max(0, (memAfter - memBefore) / 1024.0);

        logResult("MTSDecisionTree", n, avgTimeMs, memDiffKb, writer);
    }

    private void benchmarkWeightedHeap(List<TriageRequestDTO> requests, int n, PrintWriter writer) {
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            PriorityDispatchQueue queue = new PriorityDispatchQueue();
            for (TriageRequestDTO req : requests) {
                double score = scoringStrategy.calculateScore(req);
                TriageAssessment ta = new TriageAssessment();
                ta.setTieBreakerScore(score);
                ta.setAssignedCategory(TriageCategory.GREEN);
                ta.setCreatedAt(LocalDateTime.now());
                queue.insert(ta);
            }
        }

        // Measure
        System.gc();
        long memBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long totalTimeNs = 0;

        for (int i = 0; i < TIMED_RUNS; i++) {
            long start = System.nanoTime();
            PriorityDispatchQueue queue = new PriorityDispatchQueue();
            for (TriageRequestDTO req : requests) {
                double score = scoringStrategy.calculateScore(req);
                TriageAssessment ta = new TriageAssessment();
                ta.setTieBreakerScore(score);
                ta.setAssignedCategory(TriageCategory.GREEN); // Dummy for benchmarking insertion speed
                ta.setCreatedAt(LocalDateTime.now());
                queue.insert(ta);
            }
            totalTimeNs += (System.nanoTime() - start);
        }

        long memAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        double avgTimeMs = (totalTimeNs / (double) TIMED_RUNS) / 1_000_000.0;
        double memDiffKb = Math.max(0, (memAfter - memBefore) / 1024.0) / TIMED_RUNS; // Average memory diff per run

        logResult("WeightedScore+Heap", n, avgTimeMs, memDiffKb, writer);
    }

    private void benchmarkKNN(List<TriageRequestDTO> requests, int n, PrintWriter writer) {
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            for (TriageRequestDTO req : requests) {
                knnClassifier.classify(req);
            }
        }

        // Measure
        System.gc();
        long memBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long totalTimeNs = 0;

        for (int i = 0; i < TIMED_RUNS; i++) {
            long start = System.nanoTime();
            for (TriageRequestDTO req : requests) {
                knnClassifier.classify(req);
            }
            totalTimeNs += (System.nanoTime() - start);
        }

        long memAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        double avgTimeMs = (totalTimeNs / (double) TIMED_RUNS) / 1_000_000.0;
        double memDiffKb = Math.max(0, (memAfter - memBefore) / 1024.0);

        logResult("KNNClassifier", n, avgTimeMs, memDiffKb, writer);
    }

    private void logResult(String algo, int n, double timeMs, double memKb, PrintWriter writer) {
        System.out.printf("%-20s | %-10d | %-15.3f | %-15.2f%n", algo, n, timeMs, memKb);
        writer.printf("%s,%d,%.3f,%.2f%n", algo, n, timeMs, memKb);
    }

    private List<TriageRequestDTO> generateRequests(int count) {
        List<TriageRequestDTO> list = new ArrayList<>(count);
        Random random = new Random(123);
        for (int i = 0; i < count; i++) {
            TriageRequestDTO req = new TriageRequestDTO();
            req.setBreathing(random.nextBoolean());
            req.setPulseRate(60 + random.nextInt(60));
            req.setAvpu(ConsciousnessLevel.values()[random.nextInt(4)]);
            req.setOxygenSaturation(85 + random.nextInt(16));
            req.setSystolicBP(90 + random.nextInt(50));
            req.setPainScore(random.nextInt(11));
            req.setTemperature(36.0 + random.nextDouble() * 3.0);
            req.setAge(random.nextInt(90));
            req.setHazardPresent(random.nextBoolean());
            list.add(req);
        }
        return list;
    }
}
