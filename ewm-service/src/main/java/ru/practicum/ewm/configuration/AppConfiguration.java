package ru.practicum.ewm.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.StatClient;

@Configuration
public class AppConfiguration {
    @Value("${client.url}")
    String serverUrl;
    @Bean
    public StatClient createClient() {
        StatClient statClient = new StatClient();
        statClient.setUpStatClient(serverUrl);
        return statClient;
    }
}