package org.netlykos.fortune;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.netlykos.fortune.beans.Fortune;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = FortuneManager.class)
class FortuneManagerTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(FortuneManagerTest.class);

  @Autowired
  FortuneManager fortuneManager;

  @BeforeEach
  void init() {
    assertNotNull(fortuneManager);
  }

  @Test
  void testGetRandomFortune() {
    Fortune fortune = fortuneManager.getRandomFortune();
    LOGGER.debug("{}", fortune);
    assertNotNull(fortune);
  }

  @Test
  void testGetFortuneSuccess() {
    String category = "art";
    int cookie = 3;
    List<String> expect = Arrays.asList("A celebrity is a person who is known for his well-knownness.");
    Fortune actual = fortuneManager.getFortune(category, cookie);
    LOGGER.debug("{}", actual);
    assertEquals(expect.size(), actual.lines().size());
    assertEquals(expect.get(0), actual.lines().get(0));
  }

  @Test
  void testGetFortuneSuccessEdge() {
    String category = "art";
    int cookie = 465;
    List<String> expect = Arrays.asList(
        "\"Hiro has two loves, baseball and porn, but due to an elbow injury he",
        "gives up baseball....\"",
        "  -- AniDB description of _H2_, with selective quoting applied.",
        "     http://anidb.info/perl-bin/animedb.pl?show=anime&aid=352");
    Fortune actual = fortuneManager.getFortune(category, cookie);
    LOGGER.debug("{}", actual);
    assertEquals(expect.size(), actual.lines().size());
    for (int i = 0; i < expect.size(); i++) {
      assertEquals(expect.get(i), actual.lines().get(i));
    }
  }

  @Test
  void testGetFortuneFailureBadCategory() {
    String category = "not_a_valid_category";
    String expected = String.format("Category %s is not setup.", category);
    IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> {
      fortuneManager.getFortune(category, 1);
    });
    assertEquals(expected, actual.getMessage());
  }

  @Test
  void testGetFortuneFailureNegativeCookie() {
    String category = "art";
    int cookie = -1;
    String expected = "Cookie number should be positive.";
    IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> {
      fortuneManager.getFortune(category, cookie);
    });
    assertEquals(expected, actual.getMessage());
  }

  @Test
  void testGetFortuneFailureOverflowCookie() {
    String category = "art";
    int range = 465, cookie = range + 1;
    String expected = String.format("Category %s only contains %d cookie(s).", category, range);
    IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> {
      fortuneManager.getFortune(category, cookie);
    });
    assertEquals(expected, actual.getMessage());
  }

}