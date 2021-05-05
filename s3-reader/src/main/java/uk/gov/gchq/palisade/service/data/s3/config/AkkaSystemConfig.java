package uk.gov.gchq.palisade.service.data.s3.config;

import akka.actor.ActorSystem;
import akka.stream.Materializer;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import uk.gov.gchq.palisade.service.data.s3.S3Bucket;
import uk.gov.gchq.palisade.service.data.s3.S3DataReader;

import java.io.IOException;
import java.util.Map;

@Configuration
@ConditionalOnProperty(
        value = "akka.discovery.config.services.kafka.from-config",
        havingValue = "true",
        matchIfMissing = true
)
public class AkkaSystemConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(AkkaSystemConfig.class);

    @Bean
    @ConditionalOnMissingBean
    static PropertiesConfigurer propertiesConfigurer(final ResourceLoader resourceLoader, final Environment environment) {
        return new PropertiesConfigurer(resourceLoader, environment);
    }

    @Bean
    @ConditionalOnMissingBean
    ActorSystem getActorSystem(final PropertiesConfigurer propertiesConfigurer) {
        propertiesConfigurer.getAllActiveProperties()
                .entrySet().stream().sorted(Map.Entry.comparingByKey())
                .forEach(entry -> LOGGER.debug("{} = {}", entry.getKey(), entry.getValue()));
        Config config = propertiesConfigurer
                .toHoconConfig(propertiesConfigurer.getAllActiveProperties())
                .withFallback(ConfigFactory.load());
        return ActorSystem.create("SpringAkkaStreamsSystem", config);
    }

    @Bean
    @ConditionalOnMissingBean
    Materializer getMaterialiser(final ActorSystem system) {
        return Materializer.createMaterializer(system);
    }

}
