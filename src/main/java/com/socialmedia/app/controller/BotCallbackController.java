//package com.socialmedia.app.controller;
//
//import java.util.Map;
//import java.util.Objects;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestHeader;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.socialmedia.app.service.UserService;
//import com.socialmedia.app.service.impl.UserServiceImpl;
//
//@RestController
//@RequestMapping("/api/auth")
//public class BotCallbackController {
//
//    @Value("${bot.callback.api-key}")
//    private String apiKey;
//    
//    @Autowired
//    private UserService userService;
//
//    @PostMapping("/verify-phone-callback")
//    public ResponseEntity<?> verifyPhoneCallback(@RequestHeader("x-bot-api-key") String key,
//                                                 @RequestBody Map<String,Object> payload) {
//        if (!Objects.equals(apiKey, key)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//        String phone = (String) payload.get("phone");
//        // find user by phone or by id mapping, update verified flag
//        userService.markPhoneVerified(phone);
//        return ResponseEntity.ok(Map.of("ok", true));
//    }
//}
//
