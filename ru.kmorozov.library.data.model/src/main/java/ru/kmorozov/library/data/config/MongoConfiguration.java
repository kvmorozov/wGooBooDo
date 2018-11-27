package ru.kmorozov.library.data.config;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by sbt-morozov-kv on 28.11.2016.
 */

@Configuration
public class MongoConfiguration extends AbstractMongoConfiguration {

    public static final String BOOKS_MONGO_DB_NAME = "BOOKS";

    @Value("${mongo.uri}")
    private String mongoUri;

    @Override
    protected String getDatabaseName() {
        return MongoConfiguration.BOOKS_MONGO_DB_NAME;
    }

    @Override
    public MongoClient mongoClient() {
        return new MongoClient(new MongoClientURI(this.mongoUri));
    }

    @Override
    protected Collection<String> getMappingBasePackages() {
        return Collections.singleton("ru.kmorozov.library.data.model");
    }
}
