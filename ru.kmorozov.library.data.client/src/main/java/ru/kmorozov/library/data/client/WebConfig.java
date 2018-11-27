package ru.kmorozov.library.data.client;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by sbt-morozov-kv on 19.06.2017.
 */
@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setDefaultTimeout(Long.MAX_VALUE);
        super.configureAsyncSupport(configurer);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (!registry.hasMappingForPattern("/icons/**")) {
            registry.addResourceHandler("/icons/**").addResourceLocations(
                    "classpath:/static/icons/");
        }

        if (!registry.hasMappingForPattern("/css/**")) {
            registry.addResourceHandler("/css/**").addResourceLocations(
                    "classpath:/static/css/");
        }

        if (!registry.hasMappingForPattern("/**"))
            registry.addResourceHandler("/**").addResourceLocations("file:/E:/tmp/");
    }
}