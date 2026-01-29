package com.hmdp;

import com.hmdp.constant.UserBackMessage;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class hmdpApplicationTest {

    @Resource
    private UserBackMessage userBackMessage;

    /**
     * 发送验证码
     * @param
     * @param
     * @return
     */
    @Test
    public void sendCode() {
        // 打印整个对象，看看是不是真的绑定上了

    }
}
