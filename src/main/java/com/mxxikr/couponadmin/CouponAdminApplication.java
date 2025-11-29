package com.mxxikr.couponadmin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 쿠폰 관리 애플리케이션 메인 클래스
 */
@EnableJpaAuditing
@SpringBootApplication
public class CouponAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(CouponAdminApplication.class, args);
    }

}
