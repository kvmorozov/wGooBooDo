package ru.kmorozov.library.data.client

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter

/**
 * Created by sbt-morozov-kv on 19.06.2017.
 */
@Configuration
class WebConfig : WebMvcConfigurerAdapter() {

    override fun configureAsyncSupport(configurer: AsyncSupportConfigurer?) {
        configurer!!.setDefaultTimeout(java.lang.Long.MAX_VALUE)
        super.configureAsyncSupport(configurer)
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry?) {
        if (!registry!!.hasMappingForPattern("/icons/**")) {
            registry.addResourceHandler("/icons/**").addResourceLocations(
                    "classpath:/static/icons/")
        }

        if (!registry.hasMappingForPattern("/css/**")) {
            registry.addResourceHandler("/css/**").addResourceLocations(
                    "classpath:/static/css/")
        }

        if (!registry.hasMappingForPattern("/**"))
            registry.addResourceHandler("/**").addResourceLocations("file:/E:/tmp/")
    }
}