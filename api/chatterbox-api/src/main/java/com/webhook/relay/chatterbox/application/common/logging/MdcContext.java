package com.webhook.relay.chatterbox.application.common.logging;

public interface MdcContext {

    void initialize();

    void setRepositoryName(String repositoryName);

    void clear();

}
