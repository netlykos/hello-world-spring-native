package org.netlykos.fortune.converter;

import static java.lang.String.format;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_XML;
import static org.springframework.http.MediaType.TEXT_PLAIN;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import org.netlykos.fortune.beans.Fortune;
import org.netlykos.fortune.codec.FortuneEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

public class FortuneHttpMessageConverter implements HttpMessageConverter<Fortune> {

  private static final Logger LOGGER = LoggerFactory.getLogger(FortuneHttpMessageConverter.class);

  private List<MediaType> supportedMediaTypes = Arrays.asList(APPLICATION_XML, TEXT_PLAIN);

  @Override
  public boolean canRead(Class<?> clazz, MediaType mediaType) {
    LOGGER.info("canRead invoked with class {} and media type {}", clazz, mediaType);
    return false;
  }

  @Override
  public boolean canWrite(Class<?> clazz, MediaType mediaType) {
    LOGGER.info("canWrite invoked with class {} and media type {}", clazz, mediaType);
    return Fortune.class.equals(clazz) && supportedMediaTypes.contains(mediaType);
  }

  @Override
  public List<MediaType> getSupportedMediaTypes() {
    return supportedMediaTypes;
  }

  @Override
  public Fortune read(Class<? extends Fortune> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
    HttpHeaders headers = inputMessage.getHeaders();
    LOGGER.info("Read invoked with class {} and HttpHeader {}", clazz, headers);
    List<String> contentTypes = headers.get(CONTENT_TYPE);
    throw new HttpMessageNotReadableException(format("Cannot parse to Fortune.class based on content types [%s]", contentTypes), inputMessage);
  }

  @Override
  public void write(Fortune fortune, MediaType contentType, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
    OutputStream outputStream = outputMessage.getBody();
    LOGGER.trace("Processing request for content type {} with {}", contentType, fortune);
    String content = FortuneEncoder.getContent(fortune, contentType.toString());
    outputStream.write(content.getBytes());
  }

}
