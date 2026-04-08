package org.zzt.note.app;

import org.zzt.note.commons.security.OptionsAwareRequireTokenInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 *
 * @author zhouzhitong
 * @since 2026/3/26
 */
@SpringBootApplication(scanBasePackages = {"org.zzt.note"})
@EnableJpaRepositories(basePackages = "org.zzt.note")
@EntityScan(basePackages = {"org.zzt.note"})
@Import(OptionsAwareRequireTokenInterceptor.class)
public class NoteApplication {

    public static void main(String[] args) {
        SpringApplication.run(NoteApplication.class, args);
    }

}
