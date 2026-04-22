package com.webhook.relay.chatterbox.application.port.in.event.handler;

import java.util.List;

public interface CatchUpHandlerPort {

    List<String> getAllRepositories();

    void processMissedEvents(List<String> repositories);

}
