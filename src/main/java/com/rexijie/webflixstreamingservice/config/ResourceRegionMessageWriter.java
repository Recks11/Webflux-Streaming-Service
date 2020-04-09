package com.rexijie.webflixstreamingservice.config;

import org.reactivestreams.Publisher;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.ResourceRegionEncoder;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.Math.min;

/**
 * {@code HttpMessageWriter} that can write a {@link ResourceRegion}.
 *
 * This class was directly converted from the given {@link org.springframework.http.codec.ResourceHttpMessageWriter}
 * and adapted to write multiple resource regions in a single header
 */
public class ResourceRegionMessageWriter implements HttpMessageWriter<ResourceRegion> {

    private static ResolvableType REGION_TYPE = ResolvableType.forClass(ResourceRegion.class);

    private ResourceRegionEncoder resourceRegionEncoder;

    private List<MediaType> mediaTypes;

    public ResourceRegionMessageWriter() {
        this.resourceRegionEncoder = new ResourceRegionEncoder();
        this.mediaTypes = MediaType.asMediaTypes(resourceRegionEncoder.getEncodableMimeTypes());
    }

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
            } catch (IOException e) {
                // Ignored
            }
        }
        return Optional.empty();
    }

    private static long lengthOf(ResourceRegion resourceRegion) {
        if (resourceRegion.getResource().getClass() != InputStreamResource.class) {
            try {
                return resourceRegion.getResource().contentLength();
            } catch (Exception e) {
                // Ignore Exception
            }
        }
        return -1;
    }

    @Override
    public boolean canWrite(ResolvableType elementType, MediaType mediaType) {
        return resourceRegionEncoder.canEncode(elementType, mediaType);
    }

    @Override
    public List<MediaType> getWritableMediaTypes() {
        return this.mediaTypes;
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

        return Mono.from(inputStream)
                .flatMap(resource ->
                        writeResource(resource, elementType, mediaType, message, hints));
    }

    private Mono<Void> writeResource(ResourceRegion resourceRegion,
                                     ResolvableType elementType, MediaType mediaType,
                                     ReactiveHttpOutputMessage message,
                                     Map<String, Object> hints) {
        HttpHeaders headers = message.getHeaders();
        MediaType resourceType = getResourceMediaType(mediaType, resourceRegion.getResource());
        headers.setContentType(resourceType);

        if (headers.getContentLength() < 0) {
            long length = lengthOf(resourceRegion);
            if (length != -1)
                headers.setContentLength(length);
        }

        return zeroCopy(resourceRegion.getResource(), resourceRegion, message)
                .orElseGet(() -> {
                    Mono<ResourceRegion> input = Mono.just(resourceRegion);
                    DataBufferFactory bufferFactory = message.bufferFactory();
                    Flux<DataBuffer> body =
                            this.resourceRegionEncoder.encode(input, bufferFactory, elementType, resourceType, hints);
                    return message.writeWith(body);
                });
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

        return inputStreamMono.flatMap((resourceRegion) -> {
            long contentLength = lengthOf(resourceRegion);
            long startPosition = resourceRegion.getPosition(); //Where zero copy starts
            long endPosition = min(startPosition + resourceRegion.getCount() - 1, contentLength - 1); //where zero copy ends relative to start position

            MediaType resourceMediaType = getResourceMediaType(mediaType, resourceRegion.getResource());


            headers.setContentType(resourceMediaType);
            headers.add(HttpHeaders.CONTENT_RANGE,
                    "bytes " + startPosition + '-' + endPosition + '/' + contentLength);
            headers.setContentLength(endPosition - startPosition + 1);

            response.setStatusCode(HttpStatus.PARTIAL_CONTENT);
            return writeEncodedRegion(resourceRegion, response, resourceMediaType, hints);
        });
    }

    private Mono<Void> writeEncodedRegion(ResourceRegion region, ReactiveHttpOutputMessage message,
                                          MediaType mediaType, Map<String, Object> hints) {
        return zeroCopy(region.getResource(), region, message)
                .orElseGet(() -> {
                    Mono<ResourceRegion> input = Mono.just(region);
                    Flux<DataBuffer> body = this.resourceRegionEncoder
                            .encode(input, message.bufferFactory(), REGION_TYPE, mediaType, hints);
                    return message.writeWith(body);
                });
    }
}
