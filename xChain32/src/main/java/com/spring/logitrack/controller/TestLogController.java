package com.spring.logitrack.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/api")
public class TestLogController {

    @GetMapping("/log")
    public String logTest() {
        log.info("INFO log for testing Logstash!");
        log.error("ERROR log for testing Logstash!");
        return "Logs sent to Logstash!";
    }
}
