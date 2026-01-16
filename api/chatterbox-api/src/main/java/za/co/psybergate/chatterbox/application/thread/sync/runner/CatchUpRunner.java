package za.co.psybergate.chatterbox.application.thread.sync.runner;

import java.util.List;

public interface CatchUpRunner {

    List<String> getAllRepositories();

    void processMissedEvents(List<String> repositories);

}
