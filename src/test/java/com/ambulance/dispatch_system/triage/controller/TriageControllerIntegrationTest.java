package com.ambulance.dispatch_system.triage.controller;

import com.ambulance.dispatch_system.triage.entity.TriageAssessment;
import com.ambulance.dispatch_system.triage.model.dto.TriageRequestDTO;
import com.ambulance.dispatch_system.triage.model.enums.ConsciousnessLevel;
import com.ambulance.dispatch_system.triage.model.enums.TriageCategory;
import com.ambulance.dispatch_system.triage.repository.TriageAssessmentRepository;
import com.ambulance.dispatch_system.triage.service.impl.TriageServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class TriageControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private TriageAssessmentRepository assessmentRepository;
    
    @Autowired
    private TriageServiceImpl triageService;


    @BeforeEach
    void setUp() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        assessmentRepository.deleteAll();
        // Clear the in-memory queue to isolate tests
        triageService.initQueue();
    }

    @Autowired
    private tools.jackson.databind.ObjectMapper objectMapper;

    @Test
    void testEvaluateReturnsCorrectQueuePosition() throws Exception {
        // First patient (RED)
        TriageRequestDTO req1 = new TriageRequestDTO();
        req1.setBreathing(false);
        req1.setPulseRate(0);
        req1.setAvpu(ConsciousnessLevel.UNRESPONSIVE);
        req1.setOxygenSaturation(95);
        req1.setSystolicBP(120);
        req1.setPainScore(0);
        req1.setTemperature(37.0);
        req1.setAge(30);
        req1.setHazardPresent(false);

        mockMvc.perform(post("/api/v1/triage/evaluate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.category").value(TriageCategory.RED.name()))
                .andExpect(jsonPath("$.queuePosition").value(1));

        // Second patient (BLUE)
        TriageRequestDTO req2 = new TriageRequestDTO();
        req2.setBreathing(true);
        req2.setPulseRate(80);
        req2.setAvpu(ConsciousnessLevel.ALERT);
        req2.setOxygenSaturation(98);
        req2.setSystolicBP(120);
        req2.setPainScore(2);
        req2.setTemperature(37.0);
        req2.setAge(25);
        req2.setHazardPresent(false);

        mockMvc.perform(post("/api/v1/triage/evaluate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.category").value(TriageCategory.GREEN.name()))
                .andExpect(jsonPath("$.queuePosition").value(2)); // behind RED

        // Third patient (ORANGE)
        TriageRequestDTO req3 = new TriageRequestDTO();
        req3.setBreathing(true);
        req3.setPulseRate(130);
        req3.setAvpu(ConsciousnessLevel.VOICE);
        req3.setOxygenSaturation(95);
        req3.setSystolicBP(120);
        req3.setPainScore(8);
        req3.setTemperature(37.0);
        req3.setAge(40);
        req3.setHazardPresent(false);

        // Should rank 2nd (behind RED, ahead of BLUE)
        mockMvc.perform(post("/api/v1/triage/evaluate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req3)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.category").value(TriageCategory.ORANGE.name()))
                .andExpect(jsonPath("$.queuePosition").value(2));
                
        // Test /queue endpoint correctly returns heap snapshot
        mockMvc.perform(get("/api/v1/triage/queue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].category").value("RED"))
                .andExpect(jsonPath("$[1].category").value("ORANGE"))
                .andExpect(jsonPath("$[2].category").value("GREEN"));
    }
    
    @Test
    void testInvalidPayloadReturns400() throws Exception {
        String invalidReq = "{\"breathing\":true,\"painScore\":15}"; // Out of range

        mockMvc.perform(post("/api/v1/triage/evaluate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidReq))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testMarkResolvedRemovesFromQueue() throws Exception {
        String req1 = "{\"breathing\":false,\"pulseRate\":0,\"avpu\":\"UNRESPONSIVE\",\"oxygenSaturation\":95,\"systolicBP\":120,\"painScore\":0,\"temperature\":37.0,\"age\":30,\"hazardPresent\":false}";

        mockMvc.perform(post("/api/v1/triage/evaluate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req1))
                .andExpect(status().isCreated());
                
        // Fetch ID from repo
        TriageAssessment assessment = assessmentRepository.findAll().get(0);
        UUID id = assessment.getId();
        
        // Assert in queue
        mockMvc.perform(get("/api/v1/triage/queue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
                
        // Resolve
        mockMvc.perform(put("/api/v1/triage/" + id + "/resolve"))
                .andExpect(status().isNoContent());
                
        // Assert resolved in DB
        TriageAssessment resolvedAssessment = assessmentRepository.findById(id).orElseThrow();
        assertTrue(resolvedAssessment.getResolved());
        
        // Assert not in queue
        mockMvc.perform(get("/api/v1/triage/queue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
    
    @Test
    @Transactional
    void testHeapRehydrationOnStartup() throws Exception {
        // Bypass heap and insert directly into DB to simulate existing unresolved assessments
        TriageAssessment a1 = new TriageAssessment();
        a1.setAssignedCategory(TriageCategory.RED);
        a1.setTieBreakerScore(15.0);
        a1.setSeverityRank(TriageCategory.RED.getSeverity());
        assessmentRepository.saveAndFlush(a1);
        
        TriageAssessment a2 = new TriageAssessment();
        a2.setAssignedCategory(TriageCategory.GREEN);
        a2.setTieBreakerScore(5.0);
        a2.setSeverityRank(TriageCategory.GREEN.getSeverity());
        assessmentRepository.saveAndFlush(a2);
        
        // Call the @PostConstruct method manually to simulate app startup
        triageService.initQueue();
        
        // Assert the heap is correctly ordered
        mockMvc.perform(get("/api/v1/triage/queue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].category").value("RED"))
                .andExpect(jsonPath("$[1].category").value("GREEN"));
    }
}
