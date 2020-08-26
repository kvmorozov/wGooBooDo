package ru.kmorozov.library.data.storage.mongo

import com.mongodb.BasicDBObject
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import java.util.HashSet

/**
 * Created by sbt-morozov-kv on 29.11.2016.
 */
class LikeTextSearch(private val collectionName: String, private val mongoTemplate: MongoTemplate) {

    fun findMatchingIds(searchString: String): Collection<ObjectId> {
        val result = executeFullTextSearch(searchString)
        return extractSearchResultIds(result)
    }

    private fun executeFullTextSearch(searchString: String): Document {
        val textSearch = Document()
        textSearch["\$text"] = collectionName
        textSearch["search"] = searchString
        textSearch["limit"] = SEARCH_LIMIT // override default of 100
        textSearch["project"] = Document("_id", 1)
        return mongoTemplate.executeCommand(textSearch)
    }

    companion object {

        private const val SEARCH_LIMIT = 100

        private fun extractSearchResultIds(commandResult: Document): Collection<ObjectId> {
            val objectIds = HashSet<ObjectId>()
            val resultList = commandResult["results"] as Iterable<*>

            for (aResultList in resultList) {
                val resultContainer = aResultList as BasicDBObject
                val resultObj = resultContainer.get("obj") as BasicDBObject
                val resultId = resultObj.get("_id") as ObjectId
                objectIds.add(resultId)
            }
            return objectIds
        }
    }

}
