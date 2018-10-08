package th.bkk.pongchet;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(path="/rest")
@Slf4j
public class LineRest {
	
	@GetMapping("/Greeting")
	public String greet() {
		log.info("LineRest.greet() ");
		return "Hello World";
	}
}
