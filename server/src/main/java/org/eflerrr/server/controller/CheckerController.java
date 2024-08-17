package org.eflerrr.server.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;

//TODO! ONLY FOR STUDY! REMOVE BEFORE USING IN PRODUCTION
@RestController
@Slf4j
public class CheckerController {

    @RequestMapping(path = "/chat/public-key", method = RequestMethod.GET)
    public ResponseEntity<BigInteger> getPublicKey() {
        log.warn("CHECKER CONTROLLER: getting Public Key");
        return ResponseEntity.ok(BigInteger.valueOf(9999999));
    }

    @RequestMapping(path = "/chat/public-key", method = RequestMethod.POST)
    public ResponseEntity<Void> sendPublicKey(@RequestBody BigInteger publicKey) {
        log.warn("CHECKER CONTROLLER: sending Public Key, KEY: " + publicKey);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/chat/client-leaving")
    public ResponseEntity<Void> notifyClientLeaving() {
        log.warn("CHECKER CONTROLLER: notifying Client Leaving");
        return ResponseEntity.ok().build();
    }

}
