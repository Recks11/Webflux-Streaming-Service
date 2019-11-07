package com.rexijie.webflixstreamingservice.config;

import org.reactivestreams.Publisher;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.ResourceRegionEncoder;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.Math.min;

public class ResourceRegionMessageWriter implements HttpMessageWriter<ResourceRegion> {

    private static ResolvableType REGION_TYPE = ResolvableType.forClass(ResourceRegion.class);
    private ResourceRegionEncoder resourceRegionEncoder = new ResourceRegionEncoder();
    private List<MediaType> mediaTypes = MediaType.asMediaTypes(resourceRegionEncoder.getEncodableMimeTypes());

    private static MediaType getResourceMediaType(MediaType mediaType, Resource resource) {
        if (mediaType != null && mediaType.isConcrete() && mediaType != MediaType.APPLICATION_OCTET_STREAM) {
            return mediaType;
        } else {
            return MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM);
        }
    }

    private static Optional<Mono<Void>> zeroCopy(Resource resource, ResourceRegion resourceRegion,
                                                 ReactiveHttpOutputMessage message) {
        Mono<File> internalFileMono = Mono.just(resource)
                .map(resourceData -> {
                    File file;
                    try {
                        file = resourceData.getFile();
                    } catch (Exception e) {
                        throw Exceptions.propagate(e);
                    }
                    return file;
                });

        Mono<Void> voidMono = internalFileMono.flatMap(file -> {
            if (message instanceof ZeroCopyHttpOutputMessage && resource.isFile()) {
                long position = resourceRegion.getPosition();
                long count = resourceRegion.getCount();
                return ((ZeroCopyHttpOutputMessage) message).writeWith(file, position, count);
            }
            return Mono.empty();
        });

        return Optional.of(voidMono);
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

    /**
     * Write an given stream of object to the output message.
     *
     * @param inputStream the objects to write
     * @param elementType the type of objects in the stream which must have been
     *                    previously checked via {@link #canWrite(ResolvableType, MediaType)}
     * @param mediaType   the content type for the write (possibly {@code null} to
     *                    indicate that the default content type of the writer must be used)
     * @param message     the message to write to
     * @param hints       additional information about how to encode and write
     * @return indicates completion or error
     */
    @Override
    public Mono<Void> write(Publisher<? extends ResourceRegion> inputStream,
                            ResolvableType elementType, MediaType mediaType,
                            ReactiveHttpOutputMessage message,
                            Map<String, Object> hints) {
        //@TODO implement this section
        return null;
    }

    /**
     * Server-side only alternative to
     * {@link #write(Publisher, ResolvableType, MediaType, ReactiveHttpOutputMessage, Map)}
     * with additional context available.
     *
     * @param actualType  the actual return type of the method that returned the
     *                    value; for annotated controllers, the {@link MethodParameter} can be
     *                    accessed via {@link ResolvableType#getSource()}.
     * @param elementType the type of Objects in the input stream
     * @param mediaType   the content type to use (possibly {@code null} indicating
     *                    the default content type of the writer should be used)
     * @param request     the current request
     * @param response    the current response
     * @return a {@link Mono} that indicates completion of writing or error
     */
    @Override
    public Mono<Void> write(Publisher<? extends ResourceRegion> inputStream,
                            ResolvableType actualType, ResolvableType elementType,
                            MediaType mediaType, ServerHttpRequest request,
                            ServerHttpResponse response, Map<String, Object> hints) {

        HttpHeaders headers = response.getHeaders();
        headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");

        Mono<ResourceRegion> inputStreamMono = Mono.from(inputStream);
        Mono<Long> contentLengthMono = inputStreamMono.
                map(resourceRegion -> {
                    long contentLength;

                    try {
                        contentLength = resourceRegion.getResource().contentLength();
                    } catch (Exception e) {
                        throw Exceptions.propagate(e);
                    }

                    return contentLength;
                });

        return inputStreamMono.zipWith(contentLengthMono, (resourceRegion, contentLength) -> {
            long startPosition = resourceRegion.getPosition(); //Where zero copy starts
            long endPosition = min(startPosition + resourceRegion.getCount() - 1, contentLength - 1); //where zero copy ends relative to start position
            response.setStatusCode(HttpStatus.PARTIAL_CONTENT);
            MediaType resourceMediaType = getResourceMediaType(mediaType, resourceRegion.getResource());
            headers.setContentType(resourceMediaType);
            headers.add(HttpHeaders.CONTENT_RANGE, "bytes " + startPosition + '-' + endPosition + '/' + contentLength);
            headers.setContentLength(endPosition - startPosition + 1);

            return zeroCopy(resourceRegion.getResource(), resourceRegion, response)
                    .orElseGet(() -> {
                        Mono<ResourceRegion> input = Mono.just(resourceRegion);
                        Flux<DataBuffer> body = this.resourceRegionEncoder.encode(input, response.bufferFactory(), REGION_TYPE, resourceMediaType, Collections.emptyMap());
                        return response.writeWith(body);
                    });
        }).flatMap(voidMono -> voidMono)
                .doOnError(t -> {
                    throw Exceptions.propagate(t);
                });
    }
}
