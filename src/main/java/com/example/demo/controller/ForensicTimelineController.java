
package com.example.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ForensicTimelineResponse;
import com.example.demo.service.ForensicTimelineService;

import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/organization/forensic-timelines")
public class ForensicTimelineController {

    private final ForensicTimelineService timelineService;

    public ForensicTimelineController(
            ForensicTimelineService timelineService) {
        this.timelineService = timelineService;
    }

    @GetMapping
    public ResponseEntity<List<ForensicTimelineResponse>>
            getTimelines() {

        return ResponseEntity.ok(
                timelineService.reconstructTimelines()
        );
    }

@GetMapping("/investigation/{investigationId}")
public ResponseEntity<List<ForensicTimelineResponse>> getInvestigationTimeline(
        @PathVariable Long investigationId) {

    return ResponseEntity.ok(
            timelineService.reconstructInvestigationTimeline(investigationId)
    );
}

}
