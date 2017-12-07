package ru.kmorozov.library.data.storage.mongo;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by sbt-morozov-kv on 29.11.2016.
 */
public class LikeTextSearch {

    private static int SEARCH_LIMIT = 100;

    private String collectionName;
    private MongoTemplate mongoTemplate;

    public LikeTextSearch(String collectionName, MongoTemplate mongoTemplate) {
        this.collectionName = collectionName;
        this.mongoTemplate = mongoTemplate;
    }

    public Collection<ObjectId> findMatchingIds(String searchString) {
        Document result = executeFullTextSearch(searchString);
        return extractSearchResultIds(result);
    }

    private Document executeFullTextSearch(String searchString) {
        Document textSearch = new Document();
        textSearch.put("$text", collectionName);
        textSearch.put("search", searchString);
        textSearch.put("limit", SEARCH_LIMIT); // override default of 100
        textSearch.put("project", new Document("_id", 1));
        return mongoTemplate.executeCommand(textSearch);
    }

    private static Collection<ObjectId> extractSearchResultIds(Document commandResult) {
        Set<ObjectId> objectIds = new HashSet<>();
        BasicDBList resultList = (BasicDBList) commandResult.get("results");

        if (resultList == null) return Collections.emptyList();

        for (Object aResultList : resultList) {
            BasicDBObject resultContainer = (BasicDBObject) aResultList;
            BasicDBObject resultObj = (BasicDBObject) resultContainer.get("obj");
            ObjectId resultId = (ObjectId) resultObj.get("_id");
            objectIds.add(resultId);
        }
        return objectIds;
    }

}
