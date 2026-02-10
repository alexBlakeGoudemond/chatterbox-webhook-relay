package za.co.psybergate.chatterbox.application.common.logging;

public interface MdcContext {

    void initialize();

    void setRepositoryName(String repositoryName);

    void clear();

}
