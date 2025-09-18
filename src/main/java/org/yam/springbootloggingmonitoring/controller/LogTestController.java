package org.yam.springbootloggingmonitoring.controller;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogTestController {
    private static final Logger LOG = LogManager.getLogger(LogTestController.class);

    @GetMapping(value = "/echo")
    public String echoMessage() {
        LOG.log(Level.INFO, "Echo Triggered");
        return "Echo Triggered";
    }
}
