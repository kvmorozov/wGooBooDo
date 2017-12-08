package ru.kmorozov.library.data.storage.mongo;

import com.mongodb.BasicDBObject;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Created by sbt-morozov-kv on 29.11.2016.
 */
public class LikeTextSearch {

    private static final int SEARCH_LIMIT = 100;

    private final String collectionName;
    private final MongoTemplate mongoTemplate;

    public LikeTextSearch(final String collectionName, final MongoTemplate mongoTemplate) {
        this.collectionName = collectionName;
        this.mongoTemplate = mongoTemplate;
    }

    public Collection<ObjectId> findMatchingIds(final String searchString) {
        final Document result = executeFullTextSearch(searchString);
        return extractSearchResultIds(result);
    }

    private Document executeFullTextSearch(final String searchString) {
        final Document textSearch = new Document();
        textSearch.put("$text", collectionName);
        textSearch.put("search", searchString);
        textSearch.put("limit", SEARCH_LIMIT); // override default of 100
        textSearch.put("project", new Document("_id", 1));
        return mongoTemplate.executeCommand(textSearch);
    }

    private static Collection<ObjectId> extractSearchResultIds(final Document commandResult) {
        final Collection<ObjectId> objectIds = new HashSet<>();
        final Iterable resultList = (Iterable) commandResult.get("results");

        if (null == resultList) return Collections.emptyList();

        for (final Object aResultList : resultList) {
            final BasicDBObject resultContainer = (BasicDBObject) aResultList;
            final BasicDBObject resultObj = (BasicDBObject) resultContainer.get("obj");
            final ObjectId resultId = (ObjectId) resultObj.get("_id");
            objectIds.add(resultId);
        }
        return objectIds;
    }

}
