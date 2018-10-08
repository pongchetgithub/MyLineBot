package th.bkk.pongchet;

import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@LineMessageHandler
@Slf4j
public class MyLineBotApplication  implements InitializingBean{
	
	@Value("${line.bot.channel-token}") private String token;
	@Value("${line.bot.channel-secret}") private String secret;

	@Override
	public void afterPropertiesSet() throws Exception {
		log.info("token: "+token);
		log.info("secret: "+secret);
	}
	
	public static void main(String[] args) {
		SpringApplication.run(MyLineBotApplication.class, args);
	}
	
    @EventMapping
    public Message handleTextMessage(MessageEvent<TextMessageContent> e) {
        log.info("event: " + e);
        TextMessageContent message = e.getMessage();
        return new TextMessage(message.getText());
    }
}
