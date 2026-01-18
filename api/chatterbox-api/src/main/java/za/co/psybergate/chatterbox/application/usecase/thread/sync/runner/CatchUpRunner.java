package za.co.psybergate.chatterbox.application.usecase.thread.sync.runner;

import java.util.List;

// TODO BlakeGoudemond 2026/01/17 | test?
public interface CatchUpRunner {

    List<String> getAllRepositories();

    void processMissedEvents(List<String> repositories);

}
