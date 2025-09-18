package vn.vinaacademy.chat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import vn.vinaacademy.security.grpc.JwtGrpcClient;

@Slf4j
@SpringBootApplication
public class ChatServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(ChatServiceApplication.class, args);
  }

  @Bean
  public CommandLineRunner runner(JwtGrpcClient client) {
    return args -> client.validateToken("test-token");
  }
}
