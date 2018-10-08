package th.bkk.pongchet;

import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@LineMessageHandler
@SpringBootApplication
public class MyLineBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyLineBotApplication.class, args);
	}
    @EventMapping
    public Message handleTextMessage(MessageEvent<TextMessageContent> e) {
        System.out.println("event: " + e);
        TextMessageContent message = e.getMessage();
        return new TextMessage(message.getText());
    }
}
