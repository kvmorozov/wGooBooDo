package ru.kmorozov.library.data.converters.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.convert.EntityReader;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Fields;
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

    public <O> List<O> mapAggregationResults(Class<O> outputType, DBObject commandResult, String collectionName) {

        @SuppressWarnings("unchecked")
                // Изменено здесь
        Iterable<DBObject> resultSet = (Iterable<DBObject>) ((DBObject) commandResult.get("cursor")).get("firstBatch");
        if (resultSet == null) {
            return Collections.emptyList();
        }

        DbObjectCallback<O> callback = new UnwrapAndReadDbObjectCallback<O>(mongoTemplate.getConverter(), outputType, collectionName);

        List<O> mappedResults = new ArrayList<O>();
        for (DBObject dbObject : resultSet) {
            mappedResults.add(callback.doWith(dbObject));
        }

        return mappedResults;
    }

    class UnwrapAndReadDbObjectCallback<T> extends ReadDbObjectCallback<T> {

        public UnwrapAndReadDbObjectCallback(EntityReader<? super T, DBObject> reader, Class<T> type,
                                             String collectionName) {
            super(reader, type, collectionName);
        }

        @Override
        public T doWith(DBObject object) {

            Object idField = object.get(Fields.UNDERSCORE_ID);

            if (!(idField instanceof DBObject)) {
                return super.doWith(object);
            }

            DBObject toMap = new BasicDBObject();
            DBObject nested = (DBObject) idField;
            toMap.putAll(nested);

            for (String key : object.keySet()) {
                if (!Fields.UNDERSCORE_ID.equals(key)) {
                    toMap.put(key, object.get(key));
                }
            }

            return super.doWith(toMap);
        }
    }

    private class ReadDbObjectCallback<T> implements DbObjectCallback<T> {

        private final EntityReader<? super T, DBObject> reader;
        private final Class<T> type;
        private final String collectionName;

        public ReadDbObjectCallback(EntityReader<? super T, DBObject> reader, Class<T> type, String collectionName) {

            Assert.notNull(reader, "EntityReader must not be null!");
            Assert.notNull(type, "Entity type must not be null!");

            this.reader = reader;
            this.type = type;
            this.collectionName = collectionName;
        }

        public T doWith(DBObject object) {
            T source = reader.read(type, object);
            return source;
        }
    }

    interface DbObjectCallback<T> {
        T doWith(DBObject object);
    }
}
