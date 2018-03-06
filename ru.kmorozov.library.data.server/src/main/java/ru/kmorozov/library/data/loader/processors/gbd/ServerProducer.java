package ru.kmorozov.library.data.loader.processors.gbd;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.kmorozov.gbd.core.logic.context.IBookListProducer;

import java.util.Arrays;
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
        if (ids == null)
            if (StringUtils.isEmpty(defaultIds))
                ids = dbCtx.getBookIdsList();
            else
                ids = new HashSet(Arrays.asList(defaultIds.split(",")));

        return ids;
    }
}
