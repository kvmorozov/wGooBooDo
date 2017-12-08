package ru.kmorozov.library.data.converters.mongo;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.convert.EntityReader;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Component
//TODO Перенесено из Spring Data MongoTemplate - нужно вернуть обратно
public class MongoConverterUtils {

    @Autowired
    private MongoTemplate mongoTemplate;

    public <O> List<O> mapAggregationResults(final Class<O> outputType, final Document commandResult, final String collectionName) {

        // Изменено здесь
        final Iterable<Document> resultSet = (Iterable<Document>) ((Map<String, Object>) commandResult.get("cursor")).get("firstBatch");
        if (null == resultSet) {
            return Collections.emptyList();
        }

        final DocumentCallback<O> callback = new UnwrapAndReadDocumentCallback<>(mongoTemplate.getConverter(), outputType, collectionName);

        final List<O> mappedResults = new ArrayList<>();
        for (final Document document : resultSet) {
            mappedResults.add(callback.doWith(document));
        }

        return mappedResults;
    }

    class UnwrapAndReadDocumentCallback<T> extends ReadDocumentCallback<T> {

        UnwrapAndReadDocumentCallback(final EntityReader<? super T, Bson> reader, final Class<T> type, final String collectionName) {
            super(reader, type, collectionName);
        }

        @Override
        public T doWith(@Nullable final Document object) {

            if (null == object) {
                return null;
            }

            final Object idField = object.get(Fields.UNDERSCORE_ID);

            if (!(idField instanceof Document)) {
                return super.doWith(object);
            }

            final Document toMap = new Document();
            final Map nested = (Map) idField;
            toMap.putAll(nested);

            for (final Entry<String, Object> stringObjectEntry : object.entrySet()) {
                if (!Fields.UNDERSCORE_ID.equals(stringObjectEntry.getKey())) {
                    toMap.put(stringObjectEntry.getKey(), stringObjectEntry.getValue());
                }
            }

            return super.doWith(toMap);
        }
    }

    private static class ReadDocumentCallback<T> implements DocumentCallback<T> {

        private final EntityReader<? super T, Bson> reader;
        private final Class<T> type;
        private final String collectionName;

        ReadDocumentCallback(final EntityReader<? super T, Bson> reader, final Class<T> type, final String collectionName) {

            Assert.notNull(reader, "EntityReader must not be null!");
            Assert.notNull(type, "Entity type must not be null!");

            this.reader = reader;
            this.type = type;
            this.collectionName = collectionName;
        }

        @Nullable
        public T doWith(final Document object) {
            return reader.read(type, object);
        }
    }

    @FunctionalInterface
    interface DocumentCallback<T> {

        @Nullable
        T doWith(@Nullable Document object);
    }
}
