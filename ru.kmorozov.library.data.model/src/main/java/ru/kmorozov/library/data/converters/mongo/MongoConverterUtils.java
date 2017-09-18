package ru.kmorozov.library.data.converters.mongo;

import com.mongodb.DBObject;
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

@Component
//TODO Перенесено из Spring Data MongoTemplate - нужно вернуть обратно
public class MongoConverterUtils {

    @Autowired
    private MongoTemplate mongoTemplate;

    public <O> List<O> mapAggregationResults(Class<O> outputType, Document commandResult, String collectionName) {

        @SuppressWarnings("unchecked")
        // Изменено здесь
        Iterable<Document> resultSet = (Iterable<Document>) ((Document) commandResult.get("cursor")).get("firstBatch");
        if (resultSet == null) {
            return Collections.emptyList();
        }

        DocumentCallback<O> callback = new UnwrapAndReadDocumentCallback<O>(mongoTemplate.getConverter(), outputType, collectionName);

        List<O> mappedResults = new ArrayList<O>();
        for (Document document : resultSet) {
            mappedResults.add(callback.doWith(document));
        }

        return mappedResults;
    }

    class UnwrapAndReadDocumentCallback<T> extends ReadDocumentCallback<T> {

        public UnwrapAndReadDocumentCallback(EntityReader<? super T, Bson> reader, Class<T> type, String collectionName) {
            super(reader, type, collectionName);
        }

        @Override
        public T doWith(@Nullable Document object) {

            if (object == null) {
                return null;
            }

            Object idField = object.get(Fields.UNDERSCORE_ID);

            if (!(idField instanceof Document)) {
                return super.doWith(object);
            }

            Document toMap = new Document();
            Document nested = (Document) idField;
            toMap.putAll(nested);

            for (String key : object.keySet()) {
                if (!Fields.UNDERSCORE_ID.equals(key)) {
                    toMap.put(key, object.get(key));
                }
            }

            return super.doWith(toMap);
        }
    }

    private class ReadDocumentCallback<T> implements DocumentCallback<T> {

        private final EntityReader<? super T, Bson> reader;
        private final Class<T> type;
        private final String collectionName;

        public ReadDocumentCallback(EntityReader<? super T, Bson> reader, Class<T> type, String collectionName) {

            Assert.notNull(reader, "EntityReader must not be null!");
            Assert.notNull(type, "Entity type must not be null!");

            this.reader = reader;
            this.type = type;
            this.collectionName = collectionName;
        }

        @Nullable
        public T doWith(Document object) {
            T source = reader.read(type, object);
            return source;
        }
    }

    interface DocumentCallback<T> {

        @Nullable
        T doWith(@Nullable Document object);
    }
}
