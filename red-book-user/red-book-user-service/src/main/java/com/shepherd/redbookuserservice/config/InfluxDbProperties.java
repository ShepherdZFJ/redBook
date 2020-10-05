package com.shepherd.redbookuserservice.config;

import lombok.Data;

/**
 * @author fjZheng
 * @version 1.0
 * @date 2020/7/21 10:31
 */
@Data
public class InfluxDbProperties {
    private String url;
    //    private String userName;
//    private String password;
    private String database;
    private String retentionPolicy = "autogen";
    private String retentionPolicyTime = "30d";
    private int actions = 2000;
    private int flushDuration = 1000;
    private int jitterDuration = 0;
    private int bufferLimit = 10000;
}
