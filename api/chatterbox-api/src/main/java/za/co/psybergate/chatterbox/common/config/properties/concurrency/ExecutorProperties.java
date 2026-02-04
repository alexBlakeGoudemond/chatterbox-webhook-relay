package za.co.psybergate.chatterbox.common.config.properties.concurrency;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ExecutorProperties {

    private int threadCount;

    private int maxPoolSize;

    private int queueCapacity;

    private String threadNamePrefix;

}
