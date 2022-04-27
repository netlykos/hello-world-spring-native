package org.netlykos.fortune.codec;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.springframework.http.MediaType.APPLICATION_XML;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.netlykos.fortune.beans.Fortune;
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

public class FortuneEncoder implements Encoder<Fortune> {

  private static final Logger LOGGER = LoggerFactory.getLogger(FortuneEncoder.class);
  private static final String NEW_LINE = System.getProperty("line.separator");

  private List<MimeType> encodableMimeTypes = Arrays.asList(APPLICATION_XML, TEXT_PLAIN);

  @Override
  public boolean canEncode(ResolvableType elementType, MimeType mimeType) {
    LOGGER.debug("canEncode invoked with {}, {}", elementType, mimeType);
    if (mimeType == null) {
      return false;
    }
    Class<?> cls = elementType.toClass();
    if (!Fortune.class.isAssignableFrom(cls)) {
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
  public Flux<DataBuffer> encode(Publisher<? extends Fortune> inputStream, DataBufferFactory bufferFactory,
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
  public DataBuffer encodeValue(Fortune value, DataBufferFactory bufferFactory, ResolvableType valueType,
      MimeType mimeType, Map<String, Object> hints) {
    byte[] content = getContent(value, mimeType.toString()).getBytes();
    DataBuffer buffer = bufferFactory.allocateBuffer(content.length);
    buffer.write(content);
    return buffer;
  }

  @Override
  public List<MimeType> getEncodableMimeTypes() {
    return this.encodableMimeTypes;
  }

  public static String getContent(Fortune fortune, String contentType) {
    return switch (contentType) {
      case APPLICATION_XML_VALUE -> getXml(fortune);
      case TEXT_PLAIN_VALUE -> getText(fortune);
      default ->
        throw new IllegalArgumentException(format("Cannot create content-type [%s] for [%s]", contentType, fortune));
    };
  }

  private static String getText(Fortune fortune) {
    StringBuilder sb = new StringBuilder().append("category=").append(fortune.category()).append(NEW_LINE)
        .append("number=").append(fortune.number())
        .append(NEW_LINE);
    sb.append(fortune.lines().stream().collect(Collectors.joining(NEW_LINE)));
    return sb.toString();
  }

  private static String getXml(Fortune f) {
    String line = f.lines().stream()
        .map(l -> format("<line>%s</line>", escapeXmlCharacters(l))).collect(joining());
    StringBuilder sb = new StringBuilder()
        .append(format("<fortune category=\"%s\" number=\"%d\">", f.category(), f.number())).append("<lines>")
        .append(line)
        .append("</lines>").append("</fortune>");
    return sb.toString();
  }

  /**
   * Escape the following special XML characters < &lt; > &gt; & &amp;
   * 
   * @param line
   * @return
   */
  private static String escapeXmlCharacters(String line) {
    return line.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;");
  }

  private static void notNull(Object object, String message, Object... messageArgs) {
    if (object == null) {
      throw new IllegalArgumentException(format(message, messageArgs));
    }
  }

}
