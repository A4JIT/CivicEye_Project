package com.civiceye.ai.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    // Regex to parse postgres[ql]://[username]:[password]@[host][:port]/[database][?query]
    // Safely handles special characters (#, %, &, !) in passwords without URISyntaxException
    private static final Pattern PG_URI_PATTERN = Pattern.compile(
            "^(?:postgres|postgresql)://([^:]+):(.+)@([^:/]+)(?::(\\d+))?/([^?]+)(?:\\?(.*))?$"
    );

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

        if (url.startsWith("postgres://") || url.startsWith("postgresql://")) {
            Matcher matcher = PG_URI_PATTERN.matcher(url);
            if (matcher.matches()) {
                String username = matcher.group(1);
                String password = matcher.group(2);
                String host = matcher.group(3);
                String port = matcher.group(4) != null ? matcher.group(4) : "5432";
                String dbName = matcher.group(5);
                String query = matcher.group(6);

                String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + dbName
                        + (query != null && !query.isBlank() ? "?" + query : "");

                log.info("Configured PostgreSQL DataSource from URI: jdbc:postgresql://{}:{}/{}", host, port, dbName);

                return DataSourceBuilder.create()
                        .url(jdbcUrl)
                        .username(username)
                        .password(password)
                        .driverClassName("org.postgresql.Driver")
                        .build();
            } else {
                log.warn("PostgreSQL URI did not match expected pattern: {}", url);
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
