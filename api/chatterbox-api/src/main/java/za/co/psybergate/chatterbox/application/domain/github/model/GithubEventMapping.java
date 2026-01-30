package za.co.psybergate.chatterbox.application.domain.github.model;

import lombok.Data;
import lombok.Getter;

import java.util.Map;

// TODO BlakeGoudemond 2026/01/30 | should this be in infra? github could we swapped for gitlab
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

        private final String fieldName;

        GithubIncomingMappingFieldKeys(String fieldName) {
            this.fieldName = fieldName;
        }
    }

}
