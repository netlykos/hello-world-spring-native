package org.netlykos.fortune.codec;

import static java.lang.String.format;
import static org.springframework.http.MediaType.APPLICATION_XHTML_XML;
import static org.springframework.http.MediaType.APPLICATION_XHTML_XML_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static org.springframework.http.MediaType.TEXT_HTML;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.http.MediaType.TEXT_XML;
import static org.springframework.http.MediaType.TEXT_XML_VALUE;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.netlykos.fortune.beans.FortuneCategory;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.Encoder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.util.MimeType;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class FortuneCategoryEncoder implements Encoder<FortuneCategory> {

  private static final Logger LOGGER = LoggerFactory.getLogger(FortuneCategoryEncoder.class);
  private static final String NEW_LINE = System.getProperty("line.separator");
  private static final String XML_PREAMBLE = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

  private List<MimeType> encodableMimeTypes = Arrays.asList(
      APPLICATION_XML, TEXT_XML,
      APPLICATION_XHTML_XML, TEXT_HTML,
      TEXT_PLAIN);

  @Override
  public boolean canEncode(ResolvableType elementType, MimeType mimeType) {
    LOGGER.debug("canEncode invoked with {}, {}", elementType, mimeType);
    if (mimeType == null) {
      return false;
    }
    Class<?> cls = elementType.toClass();
    if (!FortuneCategory.class.isAssignableFrom(cls)) {
      return false;
    }
    for (MimeType candidate : this.encodableMimeTypes) {
      if (candidate.isCompatibleWith(mimeType)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Flux<DataBuffer> encode(Publisher<? extends FortuneCategory> inputStream, DataBufferFactory bufferFactory,
      ResolvableType elementType, MimeType mimeType, Map<String, Object> hints) {
    LOGGER.debug("encode invoked with {}, {}, {}, {}", inputStream, elementType, mimeType, hints);
    notNull(inputStream, "'inputStream' must not be null");
    notNull(bufferFactory, "'bufferFactory' must not be null");
    notNull(elementType, "'elementType' must not be null");
    if (inputStream instanceof Mono) {
      return Mono.from(inputStream)
          .map(value -> encodeValue(value, bufferFactory, elementType, mimeType, hints))
          .flux();
    }
    throw new UnsupportedOperationException(format("Unable to support input stream of type %s", inputStream));
  }

  @Override
  public DataBuffer encodeValue(FortuneCategory value, DataBufferFactory bufferFactory, ResolvableType valueType,
      MimeType mimeType, Map<String, Object> hints) {
    LOGGER.debug("encodeValue invoked with {}, {}, {}, {}", value, valueType, mimeType, hints);
    byte[] content = getContent(value, mimeType.toString()).getBytes();
    DataBuffer buffer = bufferFactory.allocateBuffer(content.length);
    buffer.write(content);
    return buffer;
  }

  @Override
  public List<MimeType> getEncodableMimeTypes() {
    return this.encodableMimeTypes;
  }

  public static String getContent(FortuneCategory fortune, String contentType) {
    return switch (contentType) {
      case APPLICATION_XML_VALUE, TEXT_XML_VALUE -> getXml(fortune);
      case APPLICATION_XHTML_XML_VALUE, TEXT_HTML_VALUE -> getHtml(fortune);
      case TEXT_PLAIN_VALUE -> getText(fortune);
      default ->
        throw new IllegalArgumentException(format("Cannot create content-type [%s] for [%s]", contentType, fortune));
    };
  }

  private static String getText(FortuneCategory fortuneCategory) {
    StringBuilder sb = new StringBuilder()
        .append("category=").append(fortuneCategory.category()).append(NEW_LINE)
        .append("totalRecords=").append(fortuneCategory.totalRecords())
        .append(NEW_LINE);
    return sb.toString();
  }

  private static String getXml(FortuneCategory fortuneCategory) {
    StringBuilder sb = new StringBuilder()
        .append(XML_PREAMBLE)
        .append(format("<fortuneCategory name=\"%s\" totalRecords=\"%d\" />", fortuneCategory.category(), fortuneCategory.totalRecords()));
    return sb.toString();
  }

  private static String getHtml(FortuneCategory f) {
    StringBuilder sb = new StringBuilder()
        .append("<div>")
        .append("<p>")
        .append(format("Fortune category %s has %d records.", f.category(), f.totalRecords()))
        .append("</p>")
        .append("</div>");
    return sb.toString();
  }

  private static void notNull(Object object, String message, Object... messageArgs) {
    if (object == null) {
      throw new IllegalArgumentException(format(message, messageArgs));
    }
  }

}
