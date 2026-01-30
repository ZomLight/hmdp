package com.hmdp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
@Data
public class UserInfoDTO {
    private String nickName;
    private String icon;

    private String city;
    private String introduce;
    private Integer fans;
    private Integer followee;
    // 0 man 1 woman
    private Boolean gender;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;
    private Integer credits;
    private Boolean level;
}
