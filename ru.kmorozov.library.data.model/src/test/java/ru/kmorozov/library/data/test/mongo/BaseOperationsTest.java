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

import static org.hamcrest.CoreMatchers.*;

/**
 * Created by sbt-morozov-kv on 28.11.2016.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MongoConfiguration.class)
public class BaseOperationsTest {

    @Autowired
    BooksRepository booksRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void connectTest() {
        Assert.assertThat(booksRepository, notNullValue());
        booksRepository.deleteAll();
        Assert.assertThat(booksRepository.count(), is(0));
    }

    @Test
    public void crudTest() {
        final long countBefore = booksRepository.count();

        final Book book = new Book("Test title", "Test author");

        final Book savedBook = booksRepository.save(book);
        Assert.assertThat(savedBook, notNullValue());
        Assert.assertThat(savedBook, is(book));
        Assert.assertThat(booksRepository.count(), is(countBefore + 1L));
        booksRepository.delete(book);
        Assert.assertThat(booksRepository.count(), is(countBefore));
    }

    @Test
    public void searchTest() {
        final List<Book> books = Arrays.asList(new Book("Test tit1le", "Test aut1hor"), new Book("Test tit2le", "Test aut2hor"));

        booksRepository.saveAll(books);

        try {
            final LikeTextSearch likeTextSearch = new LikeTextSearch(Book.class.getSimpleName(), mongoTemplate);
            Assert.assertThat(likeTextSearch.findMatchingIds("%aut1%").size(), is(0));
            final TextCriteria criteria2 = TextCriteria.forDefaultLanguage().matching("Test");
            Assert.assertThat(booksRepository.findAllBy(criteria2).size(), is(2));
        } finally {
            booksRepository.deleteAll(books);
        }
    }

    @Test
    public void findLinks() {
        final Stream<Book> lnkBooks = booksRepository.streamByBookInfoFormat("LNK");
        Assert.assertTrue(10L > lnkBooks.count());
    }
}
