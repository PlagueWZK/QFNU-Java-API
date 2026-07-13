package io.github.plaguewzk.qfnujavaapi.core;

import io.github.plaguewzk.qfnujavaapi.QFNUClient;
import io.github.plaguewzk.qfnujavaapi.TestConfig;
import io.github.plaguewzk.qfnujavaapi.parser.HtmlParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Created on 2026/7/13 17:07
 *
 * @author PlagueWZK
 */

@DisplayName("添加自定义组件")
public class AppendCustomComponentTest {
    @Test
    public void testCustomComponent() {
        QFNUClient client = QFNUClient.builder().account(TestConfig.getAccount(), TestConfig.getPassword()).install(
                (parsers, services) -> {
                    parsers.registerParser(MyCustomParser.class, resolver -> new MyCustomParser());
                    services.registerService(
                            MyCustomService.class,
                            resolver -> new MyCustomService(resolver.parser(MyCustomParser.class))
                    );
                }).build();

        MyCustomService service = client.service(MyCustomService.class);
        Assertions.assertEquals("Test : hello world!", service.testService("Test"));
    }
}

class MyCustomParser implements HtmlParser<String> {
    @Override
    public String parser(String html) {
        return html + " : hello world!";
    }
}

class MyCustomService {
    private final HtmlParser<String> parser;

    public MyCustomService(HtmlParser<String> parser) {
        this.parser = parser;
    }

    public String testService(String html) {
        return parser.parser(html);
    }
}
