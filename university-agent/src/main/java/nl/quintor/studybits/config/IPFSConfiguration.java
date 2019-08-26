package nl.quintor.studybits.config;

import io.ipfs.api.IPFS;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class IPFSConfiguration {

    @Bean
    public IPFS ipfs(Environment environment) {
        return new IPFS(environment.getProperty("IPFS.node.host"), Integer.valueOf(environment.getProperty("IPFS.node.port")));
    }
}
