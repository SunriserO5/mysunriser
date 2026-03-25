package com.mysunriser.backend.controller;


//import com.mysunriser.backend.mapper.PingMapper;
import com.mysunriser.backend.Dao.PingMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

//@RestController
//@RequestMapping("/api")
//public class HealthController {
//    @GetMapping("/health")
//    public String health(){
//
//        return "ok";
//    }
//}
@RestController
@RequestMapping("/api")
public class HealthController {
    private final PingMapper pingMapper;
    public HealthController(PingMapper pingMapper) {
        this.pingMapper = pingMapper;
    }
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("ok", true, "db", pingMapper.ping() == 1);
    }
}
