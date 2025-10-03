package vn.vinaacademy.chat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import vn.vinaacademy.security.grpc.UserGrpcClient;

@Slf4j
@EnableDiscoveryClient
@SpringBootApplication
public class ChatServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(ChatServiceApplication.class, args);
  }

  @Bean
  public CommandLineRunner runner(UserGrpcClient client) {
    return args -> {
      var response = client.getUserById("4a97640d-61a5-40dc-a19d-a27ef1e5a921");
      log.info("User from gRPC: {}", response);
    };
  }
}
