package org.netlykos.fortune;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.*;

import java.util.List;

import org.netlykos.fortune.beans.Fortune;
import org.netlykos.fortune.beans.FortuneCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FortuneController {

  @Autowired
  FortuneManager fortuneManager;

  // @formatter:off
  @GetMapping(
      produces = { TEXT_PLAIN_VALUE, APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE },
      path = { "/fortune", "/fortune/{category}", "/fortune/{category}/{cookie:[\\d]+}" }
  )
  public Fortune fortune(
      @PathVariable(required = false) String category,
      @PathVariable(required = false) Integer cookie
  ) {
  // @formatter:on
    return getCookie(category, cookie);
  }

  @GetMapping(produces = { TEXT_PLAIN_VALUE, APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE }, path = "/categories")
  public List<FortuneCategory> categories() {
    return fortuneManager.getFortuneCategories();
  }

  private Fortune getCookie(String category, Integer cookie) {
    if (category != null) {
      if (cookie != null) {
        return fortuneManager.getFortune(category, cookie);
      }
      return fortuneManager.getRandomFortune(category);
    }
    return fortuneManager.getRandomFortune();
  }

}
