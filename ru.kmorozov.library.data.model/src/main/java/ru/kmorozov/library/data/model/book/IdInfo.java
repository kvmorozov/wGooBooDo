package ru.kmorozov.library.data.model.book;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class IdInfo {

    public enum IdType {
        JSTOR,
        DOI,
        ISBN
    }

    public IdInfo() {

    }

    public IdInfo(String someId, IdType idType) {
        this.someId = someId;
        this.idType = idType;
    }

    private String someId;

    private IdType idType;

    public String getSomeId() {
        return this.someId;
    }

    public void setSomeId(String someId) {
        this.someId = someId;
    }

    public IdType getIdType() {
        return this.idType;
    }

    public void setIdType(IdType idType) {
        this.idType = idType;
    }
}
