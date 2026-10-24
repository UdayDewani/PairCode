package dev.paircode.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication @EnableScheduling
public class PairCodeApplication {
  public static void main(String[] args) { SpringApplication.run(PairCodeApplication.class, args); }
}

/** Wire RealtimeService -> RoomService after both beans are created to break circular dependency. */
@Configuration
class WiringConfig {
  WiringConfig(RoomService roomService,RealtimeService realtimeService){roomService.setRealtime(realtimeService);}
}

