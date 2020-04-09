package com.rexijie.webflixstreamingservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.ViewResolverRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.thymeleaf.spring5.ISpringWebFluxTemplateEngine;
import org.thymeleaf.spring5.view.reactive.ThymeleafReactiveViewResolver;

@Configuration
@EnableWebFlux
public class WebFluxConfig implements WebFluxConfigurer {

    private final ISpringWebFluxTemplateEngine templateEngine;

    public WebFluxConfig(ISpringWebFluxTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Bean
    public ThymeleafReactiveViewResolver thymeleafChunkAndDataDrivenViewResolver() {
        final ThymeleafReactiveViewResolver viewResolver = new ThymeleafReactiveViewResolver();
        viewResolver.setTemplateEngine(templateEngine);
        viewResolver.setOrder(1);
        viewResolver.setResponseMaxChunkSizeBytes(8192);
        return viewResolver;
    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        registry.viewResolver(this.thymeleafChunkAndDataDrivenViewResolver());
    }

    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        configurer.customCodecs().register(new ResourceRegionMessageWriter()); // for resource region
    }
}
