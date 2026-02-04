package za.co.psybergate.chatterbox.application.port.in.event.handler;

import java.util.List;

// TODO BlakeGoudemond 2026/01/17 | test?
public interface CatchUpHandlerPort {

    List<String> getAllRepositories();

    void processMissedEvents(List<String> repositories);

}
