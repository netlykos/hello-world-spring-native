package org.netlykos.fortune.converter;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.*;
import static org.springframework.http.MediaType.TEXT_PLAIN;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.netlykos.fortune.beans.Fortune;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import reactor.core.publisher.Mono;

public class FortuneHttpMessageConverter implements HttpMessageConverter<Fortune>, HttpMessageWriter<Fortune> {

  private static final Logger LOGGER = LoggerFactory.getLogger(FortuneHttpMessageConverter.class);
  private static final String NEW_LINE = System.getProperty("line.separator");

  private List<MediaType> supportedMediaTypes = Arrays.asList(APPLICATION_XML, TEXT_PLAIN);

  @Override
  public boolean canRead(Class<?> clazz, MediaType mediaType) {
    return false;
  }

  @Override
  public boolean canWrite(ResolvableType elementType, MediaType mediaType) {
    return canWrite(elementType.resolve(), mediaType);
  }

  @Override
  public boolean canWrite(Class<?> clazz, MediaType mediaType) {
    return Fortune.class.equals(clazz) && supportedMediaTypes.contains(mediaType);
  }

  @Override
  public List<MediaType> getWritableMediaTypes() {
    return getSupportedMediaTypes();
  }

  @Override
  public List<MediaType> getSupportedMediaTypes() {
    return supportedMediaTypes;
  }

  @Override
  public Fortune read(Class<? extends Fortune> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
    HttpHeaders headers = inputMessage.getHeaders();
    List<String> contentTypes = headers.get(CONTENT_TYPE);
    throw new HttpMessageNotReadableException(format("Cannot parse to Fortune.class based on content types [%s]", contentTypes), inputMessage);
  }

  @Override
  public Mono<Void> write(Publisher<? extends Fortune> inputStream, ResolvableType elementType, MediaType mediaType, ReactiveHttpOutputMessage message,
      Map<String, Object> hints) {
    return null;
  }

  @Override
  public void write(Fortune fortune, MediaType contentType, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
    OutputStream outputStream = outputMessage.getBody();
    LOGGER.debug("Processing request for content type {}", contentType);
    String content = getContent(fortune, contentType.toString());
    outputStream.write(content.getBytes());
  }

  private static String getContent(Fortune fortune, String contentType) {
    return switch (contentType) {
    case APPLICATION_XML_VALUE -> getXml(fortune);
    case TEXT_PLAIN_VALUE -> getText(fortune);
    default -> throw new HttpMessageNotWritableException(format("Cannot create content-type [%s] for [%s]", contentType, fortune));
    };
  }

  private static String getText(Fortune fortune) {
    StringBuilder sb = new StringBuilder().append("category=").append(fortune.category()).append(NEW_LINE).append("number=").append(fortune.number())
        .append(NEW_LINE);
    sb.append(fortune.lines().stream().collect(Collectors.joining(NEW_LINE)));
    return sb.toString();
  }

  private static String getXml(Fortune f) {
    String line = f.lines().stream().map(l -> format("<line>%s</line>", escapeXmlCharacters(l))).collect(joining());
    StringBuilder sb = new StringBuilder().append(format("<Fortune category=\"%s\" number=\"%d\">", f.category(), f.number())).append("<lines>").append(line)
        .append("</lines>").append("</Fortune>");
    return sb.toString();
  }

  /**
   * Escape the following special XML characters < &lt; > &gt; & &amp;
   * 
   * @param line
   * @return
   */
  private static String escapeXmlCharacters(String line) {
    return line.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }

}
