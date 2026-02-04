package za.co.psybergate.chatterbox.adapter.out.github.model;

import lombok.Data;
import lombok.Getter;

import java.util.Map;

// TODO BlakeGoudemond 2026/02/03 | do we need this anymore?
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
