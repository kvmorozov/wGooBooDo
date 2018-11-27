package ru.kmorozov.library.data.loader.processors.gbd;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.logic.context.IBookListProducer;
import ru.kmorozov.gbd.core.logic.library.LibraryFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
public class ServerProducer implements IBookListProducer {

    private Set<String> ids;

    @Value("${library.gbd.ids}")
    public String defaultIds;

    @Autowired
    @Lazy
    private DbContextLoader dbCtx;

    @Override
    public Set<String> getBookIds() {
        String bookId = GBDOptions.getBookId();

        if (!StringUtils.isEmpty(bookId) && LibraryFactory.isValidId(bookId))
            this.ids = new HashSet<>(Collections.singletonList(bookId));

        if (this.ids == null)
            if (StringUtils.isEmpty(this.defaultIds))
                this.ids = this.dbCtx.getBookIdsList();
            else
                this.ids = new HashSet(Arrays.asList(this.defaultIds.split(",")));

        return this.ids;
    }
}
