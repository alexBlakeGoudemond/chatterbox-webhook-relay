package za.co.psybergate.chatterbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

// TODO BlakeGoudemond 2026/01/26 | place polled work in outbound Port AND make agnostic of vendor (GITHUB)
// TODO BlakeGoudemond 2026/01/26 | domain: AcceptedChannel; DiscordAcceptedChannel is an adapter of this
// TODO BlakeGoudemond 2026/01/26 | if we swap to gitlab, or remove discord - is domain touched?
// TODO BlakeGoudemond 2026/01/26 | DDD Ubiquitous language - terms used to discuss domain
// TODO BlakeGoudemond 2026/01/26 | domain should only contain ubiquitous lang - pretend showing Dudu

@EnableAsync
@SpringBootApplication
public class ChatterboxApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatterboxApplication.class, args);
    }

}
