package com.ambulance.dispatch_system.triage.service.impl.algorithms;

import com.ambulance.dispatch_system.triage.model.dto.TriageRequestDTO;
import com.ambulance.dispatch_system.triage.model.enums.ConsciousnessLevel;
import com.ambulance.dispatch_system.triage.model.enums.TriageCategory;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Brute-force k-Nearest Neighbors (k-NN) classifier for triage categorization.
 * 
 * IMPORTANT: This algorithm exists purely for comparative benchmarking in the 
 * project report. It is NOT intended for production dispatch decisions, as it relies 
 * on synthetic historical data rather than exact clinical rules.
 * 
 * Time Complexity: O(N * D)
 * Justification: For a dataset of N historical records and D physiological features,
 * classifying a new request requires calculating the Euclidean distance to every 
 * record (O(N * D)), then finding the k nearest neighbors (O(N) with quickselect or 
 * O(N log N) with sorting, here implemented via sorting for simplicity).
 */
@Component
public class KNNClassifier {

    private static final int K = 5;
    private static final int SYNTHETIC_DATASET_SIZE = 500;
    private final List<HistoricalRecord> dataset = new ArrayList<>();

    // Removed @PostConstruct to prevent automatic generation of the synthetic dataset 
    // on app startup. It is now only populated when benchmarking code requests it.
    public void initSyntheticDataset() {
        initSyntheticDataset(SYNTHETIC_DATASET_SIZE);
    }

    public void initSyntheticDataset(int size) {
        dataset.clear();
        Random random = new Random(42); // Seeded for reproducibility
        for (int i = 0; i < size; i++) {
            dataset.add(generateRandomRecord(random));
        }
    }

    public TriageCategory classify(TriageRequestDTO request) {
        double[] targetFeatures = extractFeatures(request);

        // Calculate distances
        List<Neighbor> neighbors = new ArrayList<>();
        for (HistoricalRecord record : dataset) {
            double distance = euclideanDistance(targetFeatures, record.features);
            neighbors.add(new Neighbor(distance, record.category));
        }

        // Sort by distance (ascending)
        neighbors.sort(Comparator.comparingDouble(n -> n.distance));

        // Get top K and vote
        int[] votes = new int[TriageCategory.values().length];
        for (int i = 0; i < Math.min(K, neighbors.size()); i++) {
            votes[neighbors.get(i).category.ordinal()]++;
        }

        // Find majority category
        int maxVotes = -1;
        TriageCategory predictedCategory = TriageCategory.BLUE;
        for (TriageCategory cat : TriageCategory.values()) {
            if (votes[cat.ordinal()] > maxVotes) {
                maxVotes = votes[cat.ordinal()];
                predictedCategory = cat;
            }
        }

        return predictedCategory;
    }

    private double[] extractFeatures(TriageRequestDTO request) {
        // Normalize features approximately to [0, 1] range for Euclidean distance
        return new double[]{
                Boolean.TRUE.equals(request.getBreathing()) ? 1.0 : 0.0,
                request.getPulseRate() / 300.0,
                request.getAvpu().ordinal() / 3.0,
                request.getOxygenSaturation() / 100.0,
                request.getSystolicBP() / 300.0,
                request.getPainScore() / 10.0,
                (request.getTemperature() - 20) / 25.0,
                request.getAge() / 130.0,
                Boolean.TRUE.equals(request.getHazardPresent()) ? 1.0 : 0.0
        };
    }

    private double euclideanDistance(double[] f1, double[] f2) {
        double sumSq = 0.0;
        for (int i = 0; i < f1.length; i++) {
            double diff = f1[i] - f2[i];
            sumSq += diff * diff;
        }
        return Math.sqrt(sumSq);
    }

    private HistoricalRecord generateRandomRecord(Random random) {
        TriageRequestDTO req = new TriageRequestDTO();
        req.setBreathing(random.nextDouble() > 0.05); // 95% breathing
        req.setPulseRate(50 + random.nextInt(100)); // 50 to 150
        req.setAvpu(ConsciousnessLevel.values()[random.nextInt(4)]);
        req.setOxygenSaturation(80 + random.nextInt(21)); // 80 to 100
        req.setSystolicBP(80 + random.nextInt(80)); // 80 to 160
        req.setPainScore(random.nextInt(11));
        req.setTemperature(35.0 + random.nextDouble() * 5.0); // 35.0 to 40.0
        req.setAge(random.nextInt(100));
        req.setHazardPresent(random.nextDouble() > 0.9); // 10% hazard

        TriageCategory randomCat = TriageCategory.values()[random.nextInt(TriageCategory.values().length)];
        return new HistoricalRecord(extractFeatures(req), randomCat);
    }

    private static class HistoricalRecord {
        double[] features;
        TriageCategory category;

        HistoricalRecord(double[] features, TriageCategory category) {
            this.features = features;
            this.category = category;
        }
    }

    private static class Neighbor {
        double distance;
        TriageCategory category;

        Neighbor(double distance, TriageCategory category) {
            this.distance = distance;
            this.category = category;
        }
    }
}
