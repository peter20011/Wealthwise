package com.example.wealthwise_api.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Text {
    @GetMapping("/text")
    public String text(){
        return "Hello World";
    }
}
