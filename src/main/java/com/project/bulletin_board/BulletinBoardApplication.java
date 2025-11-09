package com.project.bulletin_board;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@EntityScan("com.project.bulletin_board.entity") // 👈 3. 엔티티 위치 알려주기
@EnableJpaRepositories("com.project.bulletin_board.repository")
@SpringBootApplication
public class BulletinBoardApplication {

	public static void main(String[] args) {
		SpringApplication.run(BulletinBoardApplication.class, args);
	}
}
