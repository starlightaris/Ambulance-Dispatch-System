package com.ambulance.dispatch_system.triage.util;

import com.ambulance.dispatch_system.triage.entity.TriageAssessment;
import com.ambulance.dispatch_system.triage.model.enums.TriageCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PriorityDispatchQueueTest {

    private PriorityDispatchQueue queue;

    @BeforeEach
    void setUp() {
        queue = new PriorityDispatchQueue();
    }

    private TriageAssessment createAssessment(TriageCategory category, double score, LocalDateTime time) {
        TriageAssessment ta = new TriageAssessment();
        ta.setId(UUID.randomUUID());
        ta.setAssignedCategory(category);
        ta.setTieBreakerScore(score);
        ta.setCreatedAt(time);
        return ta;
    }

    @Test
    void testInsertOutOfOrderAndExtractMaxOrder() {
        LocalDateTime now = LocalDateTime.now();
        
        TriageAssessment a1 = createAssessment(TriageCategory.GREEN, 5.0, now.minusMinutes(10));
        TriageAssessment a2 = createAssessment(TriageCategory.RED, 15.0, now.minusMinutes(5));
        TriageAssessment a3 = createAssessment(TriageCategory.YELLOW, 10.0, now.minusMinutes(2));
        TriageAssessment a4 = createAssessment(TriageCategory.RED, 20.0, now.minusMinutes(1));
        TriageAssessment a5 = createAssessment(TriageCategory.RED, 20.0, now.minusMinutes(15)); // Older tiebreaker
        
        queue.insert(a1);
        queue.insert(a2);
        queue.insert(a3);
        queue.insert(a4);
        queue.insert(a5);
        
        assertEquals(5, queue.size());
        
        // a5 should be first because RED, Score=20, and older time
        assertSame(a5, queue.extractMax());
        
        // a4 should be second because RED, Score=20, newer time
        assertSame(a4, queue.extractMax());
        
        // a2 should be third because RED, Score=15
        assertSame(a2, queue.extractMax());
        
        // a3 should be fourth because YELLOW
        assertSame(a3, queue.extractMax());
        
        // a1 should be fifth because GREEN
        assertSame(a1, queue.extractMax());
        
        assertTrue(queue.isEmpty());
    }

    @Test
    void testPeekDoesNotMutate() {
        TriageAssessment a1 = createAssessment(TriageCategory.RED, 10.0, LocalDateTime.now());
        queue.insert(a1);
        
        assertEquals(1, queue.size());
        assertSame(a1, queue.peek());
        assertEquals(1, queue.size()); // Size should remain the same
        assertSame(a1, queue.peek()); // Subsequent peek should return the same
    }

    @Test
    void testBehaviorOnEmptyQueue() {
        assertTrue(queue.isEmpty());
        
        assertThrows(NoSuchElementException.class, () -> queue.peek());
        assertThrows(NoSuchElementException.class, () -> queue.extractMax());
    }
}
