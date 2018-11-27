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
        Assert.assertThat(this.booksRepository, notNullValue());
        this.booksRepository.deleteAll();
        Assert.assertThat(this.booksRepository.count(), is(0));
    }

    @Test
    public void crudTest() {
        long countBefore = this.booksRepository.count();

        Book book = new Book("Test title", "Test author");

        Book savedBook = this.booksRepository.save(book);
        Assert.assertThat(savedBook, notNullValue());
        Assert.assertThat(savedBook, is(book));
        Assert.assertThat(this.booksRepository.count(), is(countBefore + 1L));
        this.booksRepository.delete(book);
        Assert.assertThat(this.booksRepository.count(), is(countBefore));
    }

    @Test
    public void searchTest() {
        List<Book> books = Arrays.asList(new Book("Test tit1le", "Test aut1hor"), new Book("Test tit2le", "Test aut2hor"));

        this.booksRepository.saveAll(books);

        try {
            LikeTextSearch likeTextSearch = new LikeTextSearch(Book.class.getSimpleName(), this.mongoTemplate);
            Assert.assertThat(likeTextSearch.findMatchingIds("%aut1%").size(), is(0));
            TextCriteria criteria2 = TextCriteria.forDefaultLanguage().matching("Test");
            Assert.assertThat(this.booksRepository.findAllBy(criteria2).size(), is(2));
        } finally {
            this.booksRepository.deleteAll(books);
        }
    }

    @Test
    public void findLinks() {
        Stream<Book> lnkBooks = this.booksRepository.streamByBookInfoFormat("LNK");
        Assert.assertTrue(10L > lnkBooks.count());
    }
}
