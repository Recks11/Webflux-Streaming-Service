package com.rexijie.webflixstreamingservice.config;

import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.ResourceRegionEncoder;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.Math.min;

public class ResourceRegionMessageWriter implements HttpMessageWriter<ResourceRegion> {

    private ResourceRegionEncoder resourceRegionEncoder = new ResourceRegionEncoder();
    private List<MediaType> mediaTypes = MediaType.asMediaTypes(resourceRegionEncoder.getEncodableMimeTypes());

    private static ResolvableType  REGION_TYPE = ResolvableType.forClass(ResourceRegion.class);

    private static MediaType getResourceMediaType(MediaType mediaType, Resource resource) {
        if (mediaType != null && mediaType.isConcrete() && mediaType != MediaType.APPLICATION_OCTET_STREAM) {
            return mediaType;
        } else {
            return MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM);
        }
    }

    private static Optional<Mono<Void>> zeroCopy(Resource resource, ResourceRegion resourceRegion,
                                                 ReactiveHttpOutputMessage message) {
        if (message instanceof ZeroCopyHttpOutputMessage && resource.isFile()) {
            try {
                File file = resource.getFile();
                long position = resourceRegion.getPosition();
                long count = resourceRegion.getCount();
                return Optional.of(((ZeroCopyHttpOutputMessage) message).writeWith(file, position, count));
            } catch (Exception e) {
                //Do Nothing
            }
        }
        return Optional.empty();
    }

    private Mono<Void> writeSingleRegion(ResourceRegion region,
                                                ReactiveHttpOutputMessage message) {
        return zeroCopy(region.getResource(), region, message)
                .orElseGet(() -> {
                    Mono<ResourceRegion> input = Mono.just(region);
                    MediaType messageMediaType = message.getHeaders().getContentType();
                    Flux<DataBuffer> body = this.resourceRegionEncoder.encode(input, message.bufferFactory(), REGION_TYPE, messageMediaType, Collections.emptyMap());
                    return message.writeWith(body);
                });
    }

    @Override
    public List<MediaType> getWritableMediaTypes() {
        return this.mediaTypes;
    }

    @Override
    public boolean canWrite(ResolvableType elementType, MediaType mediaType) {
        return resourceRegionEncoder.canEncode(elementType, mediaType);
    }

    @Override
    public Mono<Void> write(Publisher<? extends ResourceRegion> inputStream,
                            ResolvableType elementType, MediaType mediaType,
                            ReactiveHttpOutputMessage message,
                            Map<String, Object> hints) {
        return null;
    }

    @Override
    public Mono<Void> write(Publisher<? extends ResourceRegion> inputStream,
                            ResolvableType actualType, ResolvableType elementType,
                            MediaType mediaType, ServerHttpRequest request,
                            ServerHttpResponse response, Map<String, Object> hints) {
        HttpHeaders headers = response.getHeaders();
        headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");

        return Mono.from(inputStream)
                .flatMap(resourceRegion -> {
                    long contentLength = 0;
                    try {
                        contentLength = resourceRegion.getResource().contentLength();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    long start = resourceRegion.getPosition();
                    long end = min(start + resourceRegion.getCount() - 1, contentLength - 1);
                    response.setStatusCode(HttpStatus.PARTIAL_CONTENT);
                    MediaType resourceMediaType = getResourceMediaType(mediaType, resourceRegion.getResource());
                    headers.setContentType(resourceMediaType);
                    headers.add("Content-Range", "bytes " + start + "-" + end + "/" + contentLength);
                    headers.setContentLength(end - start + 1);

                    return zeroCopy(resourceRegion.getResource(), resourceRegion, response)
                            .orElseGet(() -> {
                                Mono<ResourceRegion> input = Mono.just(resourceRegion);
                                Flux<DataBuffer> body = this.resourceRegionEncoder.encode(input, response.bufferFactory(), REGION_TYPE, resourceMediaType, Collections.emptyMap());
                                return response.writeWith(body);
                            });

                }).doOnError(Throwable::printStackTrace);
    }
}
