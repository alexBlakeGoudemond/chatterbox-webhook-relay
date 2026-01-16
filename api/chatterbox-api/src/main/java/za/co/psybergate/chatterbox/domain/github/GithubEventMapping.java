package za.co.psybergate.chatterbox.domain.github;

import lombok.Data;
import lombok.Getter;
import za.co.psybergate.chatterbox.infrastructure.config.properties.ChatterboxSourceGithubPayloadProperties;

import java.util.Map;

@Data
public class GithubEventMapping {

    private String displayName;

    private Map<GithubIncomingMappingFieldKeys, String> fields;

    @Getter
    public enum GithubIncomingMappingFieldKeys {
        REPOSITORYNAME("repositoryName"),
        SENDERNAME("senderName"),
        URL("url"),
        URLDISPLAYTEXT("urlDisplayText"),
        EXTRADETAIL("extraDetail");

        GithubIncomingMappingFieldKeys(String fieldName) {
            this.fieldName = fieldName;
        }

        private final String fieldName;
    }

}
