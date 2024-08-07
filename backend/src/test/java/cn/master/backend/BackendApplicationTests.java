package cn.master.backend;

import cn.master.backend.security.JwtGenerator;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class BackendApplicationTests {
    @Resource
    PasswordEncoder passwordEncoder;

    @Test
    void contextLoads() {
        String admin = passwordEncoder.encode("admin");
        System.out.println(admin);
    }

}
