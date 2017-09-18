package ru.kmorozov.library.data.config;

import com.mongodb.MongoClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Created by sbt-morozov-kv on 28.11.2016.
 */

@Configuration
@EnableMongoRepositories(basePackages = "ru.kmorozov.library.data.repository")
public class MongoConfiguration extends AbstractMongoConfiguration {

    public static final String BOOKS_MONGO_DB_NAME = "BOOKS";

    @Override
    protected String getDatabaseName() {
        return BOOKS_MONGO_DB_NAME;
    }

    @Override
    public MongoClient mongoClient() {
        return new MongoClient();
    }

    @Override
    protected String getMappingBasePackage() {
        return "ru.kmorozov.library.data.model";
    }
}
