package ru.kmorozov.library.data.test.mongo;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.kmorozov.library.data.config.MongoConfiguration;
import ru.kmorozov.library.data.model.book.Book;
import ru.kmorozov.library.data.repository.BooksRepository;
import ru.kmorozov.library.data.storage.mongo.LikeTextSearch;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by sbt-morozov-kv on 28.11.2016.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MongoConfiguration.class})
public class BaseOperationsTest {

    @Autowired
    BooksRepository booksRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void connectTest() {
        Assert.assertNotNull(booksRepository);
        booksRepository.deleteAll();
        Assert.assertEquals(0, booksRepository.count());
    }

    @Test
    public void crudTest() {
        long countBefore = booksRepository.count();

        Book book = new Book("Test title", "Test author");

        Book savedBook = booksRepository.save(book);
        Assert.assertNotNull(savedBook);
        Assert.assertEquals(book, savedBook);
        Assert.assertEquals(countBefore + 1, booksRepository.count());
        booksRepository.delete(book);
        Assert.assertEquals(countBefore, booksRepository.count());
    }

    @Test
    public void searchTest() {
        List<Book> books = Arrays.asList(new Book("Test tit1le", "Test aut1hor"), new Book("Test tit2le", "Test aut2hor"));

        booksRepository.saveAll(books);

        try {
            LikeTextSearch likeTextSearch = new LikeTextSearch(Book.class.getSimpleName(), mongoTemplate);
            Assert.assertEquals(0, likeTextSearch.findMatchingIds("%aut1%").size());
            TextCriteria criteria2 = TextCriteria.forDefaultLanguage().matching("Test");
            Assert.assertEquals(2, booksRepository.findAllBy(criteria2).size());
        } finally {
            booksRepository.deleteAll(books);
        }
    }

    @Test
    public void findLinks() {
        Stream<Book> lnkBooks = booksRepository.streamByBookInfoFormat("LNK");
        Assert.assertTrue(lnkBooks.count() > 10);
    }
}
