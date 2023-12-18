package ca.dtadmi.todolist.controller;

import ca.dtadmi.todolist.service.CachingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CachingController {
    CachingService cachingService;
    public CachingController (CachingService cachingService) {
        this.cachingService = cachingService;
    }

    @GetMapping("clearAllCaches")
    public void clearAllCaches() {
        cachingService.evictAllCaches();
    }
}
