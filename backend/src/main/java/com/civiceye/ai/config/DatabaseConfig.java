package com.civiceye.ai.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${spring.datasource.url}")
    private String rawUrl;

    @Value("${spring.datasource.username:postgres}")
    private String defaultUsername;

    @Value("${spring.datasource.password:postgres}")
    private String defaultPassword;

    @Value("${spring.datasource.driver-class-name:}")
    private String driverClassName;

    @Bean
    @Primary
    public DataSource dataSource() {
        String url = rawUrl != null ? rawUrl.trim() : "";

        // Check if raw URL is a standard cloud URI like postgres://user:pass@host:port/dbname
        if (url.startsWith("postgres://") || url.startsWith("postgresql://")) {
            try {
                String parseable = url.replaceFirst("^(postgres|postgresql)://", "http://");
                URI uri = new URI(parseable);

                String host = uri.getHost();
                int port = uri.getPort() == -1 ? 5432 : uri.getPort();
                String path = uri.getPath(); // /dbname
                String query = uri.getQuery();

                String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + path + (query != null ? "?" + query : "");

                String username = defaultUsername;
                String password = defaultPassword;

                if (uri.getUserInfo() != null) {
                    String[] userInfo = uri.getUserInfo().split(":", 2);
                    username = userInfo[0];
                    if (userInfo.length > 1) {
                        password = userInfo[1];
                    }
                }

                log.info("Successfully converted cloud PostgreSQL URI to JDBC URL for host: {}:{}", host, port);

                return DataSourceBuilder.create()
                        .url(jdbcUrl)
                        .username(username)
                        .password(password)
                        .driverClassName("org.postgresql.Driver")
                        .build();
            } catch (Exception e) {
                log.warn("Failed to parse PostgreSQL URI as URI, falling back to raw configuration: {}", e.getMessage());
            }
        }

        var builder = DataSourceBuilder.create()
                .url(url)
                .username(defaultUsername)
                .password(defaultPassword);

        if (driverClassName != null && !driverClassName.isBlank()) {
            builder.driverClassName(driverClassName);
        } else if (url.startsWith("jdbc:h2:")) {
            builder.driverClassName("org.h2.Driver");
        } else {
            builder.driverClassName("org.postgresql.Driver");
        }

        return builder.build();
    }
}
