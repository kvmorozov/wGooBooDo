package ru.kmorozov.gbd.client;

/**
 * Created by km on 19.12.2016.
 */
public interface IRestClient {

    boolean ping();

    boolean synchronizeGoogleBook(String bookId);
}
