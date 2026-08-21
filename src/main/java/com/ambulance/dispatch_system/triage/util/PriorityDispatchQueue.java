package com.ambulance.dispatch_system.triage.util;

import com.ambulance.dispatch_system.triage.entity.TriageAssessment;
import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * Custom binary max-heap for priority dispatch queue.
 * Orders elements by TriageCategory severity (descending), 
 * then by tieBreakerScore (descending), 
 * then by arrival time (ascending) as a final tiebreaker.
 */
public class PriorityDispatchQueue {

    private TriageAssessment[] heap;
    private int size;
    private static final int DEFAULT_CAPACITY = 10;

    public PriorityDispatchQueue() {
        this.heap = new TriageAssessment[DEFAULT_CAPACITY];
        this.size = 0;
    }

    /**
     * Inserts a new assessment into the heap.
     * Time Complexity: O(log N) where N is the current size of the heap.
     * Explanation: Insertion adds the element at the end and 'swims' it up, 
     * which takes at most the height of the tree (log N) comparisons/swaps.
     *
     * @param assessment the TriageAssessment to insert
     */
    public void insert(TriageAssessment assessment) {
        if (size == heap.length) {
            heap = Arrays.copyOf(heap, heap.length * 2);
        }
        heap[size] = assessment;
        swim(size);
        size++;
    }

    /**
     * Returns the highest priority assessment without removing it.
     * Time Complexity: O(1)
     * Explanation: The max element is always at the root of the heap (index 0).
     *
     * @return the highest priority TriageAssessment
     * @throws NoSuchElementException if the queue is empty
     */
    public TriageAssessment peek() {
        if (size == 0) {
            throw new NoSuchElementException("Queue is empty");
        }
        return heap[0];
    }

    /**
     * Removes and returns the highest priority assessment.
     * Time Complexity: O(log N) where N is the current size of the heap.
     * Explanation: Removing the max element replaces the root with the last element 
     * and 'sinks' it down, taking at most the height of the tree (log N) comparisons/swaps.
     *
     * @return the highest priority TriageAssessment
     * @throws NoSuchElementException if the queue is empty
     */
    public TriageAssessment extractMax() {
        if (size == 0) {
            throw new NoSuchElementException("Queue is empty");
        }
        TriageAssessment max = heap[0];
        heap[0] = heap[size - 1];
        heap[size - 1] = null; // Prevent memory leak
        size--;
        if (size > 0) {
            sink(0);
        }
        return max;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    private void swim(int k) {
        while (k > 0) {
            int parent = (k - 1) / 2;
            if (compare(heap[k], heap[parent]) > 0) {
                swap(k, parent);
                k = parent;
            } else {
                break;
            }
        }
    }

    private void sink(int k) {
        while (2 * k + 1 < size) {
            int j = 2 * k + 1; // left child
            // If right child exists and is greater than left child
            if (j + 1 < size && compare(heap[j + 1], heap[j]) > 0) {
                j++;
            }
            // If current node is greater or equal to largest child, stop
            if (compare(heap[k], heap[j]) >= 0) {
                break;
            }
            swap(k, j);
            k = j;
        }
    }

    private void swap(int i, int j) {
        TriageAssessment temp = heap[i];
        heap[i] = heap[j];
        heap[j] = temp;
    }

    /**
     * Compares two assessments.
     * Returns > 0 if a has higher priority than b
     * Returns < 0 if a has lower priority than b
     * Returns 0 if equal priority
     */
    private int compare(TriageAssessment a, TriageAssessment b) {
        int severityA = a.getAssignedCategory() != null ? a.getAssignedCategory().getSeverity() : 0;
        int severityB = b.getAssignedCategory() != null ? b.getAssignedCategory().getSeverity() : 0;
        
        if (severityA != severityB) {
            return Integer.compare(severityA, severityB);
        }
        
        double scoreA = a.getTieBreakerScore() != null ? a.getTieBreakerScore() : 0.0;
        double scoreB = b.getTieBreakerScore() != null ? b.getTieBreakerScore() : 0.0;
        
        if (Double.compare(scoreA, scoreB) != 0) {
            return Double.compare(scoreA, scoreB);
        }
        
        // Final tiebreaker: earlier arrival gets higher priority
        if (a.getCreatedAt() != null && b.getCreatedAt() != null) {
            return b.getCreatedAt().compareTo(a.getCreatedAt());
        }
        
        return 0;
    }
}
