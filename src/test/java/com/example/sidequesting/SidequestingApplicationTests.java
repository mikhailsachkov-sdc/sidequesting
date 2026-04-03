package com.example.sidequesting;

import com.example.sidequesting.telegrambot.config.BotInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = "telegram.token=test-token")
class SidequestingApplicationTests {

  @MockitoBean BotInitializer botInitializer;

  @Test
  void contextLoads() {}
}
