package cn.coding.nettyandwebsocket;

import cn.coding.nettyandwebsocket.server.WebSocketServerListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class NettyAndWebSocketApplication implements WebMvcConfigurer {

	public static void main(String[] args) {

		SpringApplication.run(NettyAndWebSocketApplication.class, args);

	}
	@Bean
	public WebSocketServerListener init() {
		return new WebSocketServerListener();
	}
}
