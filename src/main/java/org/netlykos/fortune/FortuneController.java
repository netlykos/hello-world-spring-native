package org.netlykos.fortune;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_NDJSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XHTML_XML_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.http.MediaType.TEXT_XML_VALUE;

import java.util.List;
import java.util.stream.Collectors;

import org.netlykos.fortune.beans.Fortune;
import org.netlykos.fortune.beans.FortuneCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FortuneController {

  private static final String TAB = "\t";
  private static final String EXPANDED_TAB = "    ";

  @Autowired
  FortuneManager fortuneManager;

  // @formatter:off
  @GetMapping(
      path = { "/fortune", "/fortune/{category}", "/fortune/{category}/{cookie:[\\d]+}" },
      produces = {
         APPLICATION_XML_VALUE, TEXT_XML_VALUE,
         APPLICATION_JSON_VALUE, APPLICATION_NDJSON_VALUE,
         APPLICATION_XHTML_XML_VALUE, TEXT_HTML_VALUE,
         TEXT_PLAIN_VALUE
      })
  public Fortune fortune(
      @PathVariable(required = false) String category,
      @PathVariable(required = false) Integer cookie
  ) {
  // @formatter:on
    Fortune fortune = getCookie(category, cookie);
    // need to replace "\t" <tabs> with space else the client gets "\t" in their response (in json)
    List<String> lines = fortune.lines();
    List<String> newLines = lines.stream().map(s -> s.replace(TAB, EXPANDED_TAB)).collect(Collectors.toList());
    return new Fortune(fortune.category(), fortune.number(), newLines);
  }

  // @formatter:off
  @GetMapping(path = "/categories",
    produces = {
         APPLICATION_XML_VALUE, TEXT_XML_VALUE,
         APPLICATION_JSON_VALUE, APPLICATION_NDJSON_VALUE,
         APPLICATION_XHTML_XML_VALUE, TEXT_HTML_VALUE,
         TEXT_PLAIN_VALUE
    })
  // @formatter:on
  public List<FortuneCategory> categories() {
    return fortuneManager.getFortuneCategories();
  }

  private Fortune getCookie(String category, Integer cookie) {
    if (category != null) {
      if (cookie != null) {
        return fortuneManager.getFortune(category, cookie);
      }
      // we got a category, but no cookie number - select a random cookie from the category
      return fortuneManager.getRandomFortune(category);
    }
    return fortuneManager.getRandomFortune();
  }

}
