package com.example.openaiImage.controller;

import com.example.openaiImage.service.HybridSearchService;
import com.example.openaiImage.service.IndexingProgressService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.HashMap;

@RestController
public class IndexManagementController {

    private final HybridSearchService hybridSearchService;
    private final IndexingProgressService progressService;

    public IndexManagementController(HybridSearchService hybridSearchService, IndexingProgressService progressService) {
        this.hybridSearchService = hybridSearchService;
        this.progressService = progressService;
    }

    @PostMapping("/admin/index/init")
    public Map<String, String> initializeIndex() {
        try {
            hybridSearchService.reindexData();
            long count = hybridSearchService.getDocumentCount();
            return Map.of(
                    "status", "success",
                    "message", "Index initialized and " + count + " documents indexed successfully"
            );
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of(
                    "status", "error",
                    "message", "Failed to initialize index: " + e.getMessage()
            );
        }
    }

    @GetMapping("/admin/index/status")
    public Map<String, String> getIndexStatus() {
        try {
            long count = hybridSearchService.getDocumentCount();
            return Map.of(
                    "status", "success",
                    "documentCount", String.valueOf(count)
            );
        } catch (Exception e) {
            return Map.of(
                    "status", "error",
                    "message", e.getMessage()
            );
        }
    }

    @GetMapping("/admin/index/progress")
    public Map<String, Object> getIndexingProgress() {
        Map<String, Object> progress = new HashMap<>();
        progress.put("isIndexing", progressService.isIndexing());
        progress.put("currentCount", progressService.getCurrentCount());
        progress.put("totalCount", progressService.getTotalCount());
        progress.put("message", progressService.getCurrentMessage());
        return progress;
    }
}
