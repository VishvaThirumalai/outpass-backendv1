package com.mit.outpass;

import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class PortLogger {
    
    @EventListener
    public void onApplicationEvent(ServletWebServerInitializedEvent event) {
        System.out.println("✅ Application started on port: " + event.getWebServer().getPort());
        System.out.println("🚀 Server is running and listening for requests");
    }
}