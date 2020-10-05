package com.shepherd.redbookuserservice.config;

/**
 * @author fjZheng
 * @version 1.0
 * @date 2020/7/21 10:34
 */

import com.shepherd.redbookuserservice.connection.InfluxDbConnection;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
//@ConditionalOnClass(InfluxDB.class)
public class InfluxDbConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "spring.influx")
    @ConditionalOnMissingBean(name = "influxDbProperties")
    InfluxDbProperties influxDbProperties() {
        return new InfluxDbProperties();
    }

    @Bean
    @ConditionalOnMissingBean(name = "influxDbConnection")
    public InfluxDbConnection influxDbConnection(InfluxDbProperties influxDbProperties) {
        BatchOptions batchOptions = BatchOptions.DEFAULTS;
        batchOptions = batchOptions.actions(influxDbProperties.getActions());
        batchOptions = batchOptions.flushDuration(influxDbProperties.getFlushDuration());
        batchOptions = batchOptions.jitterDuration(influxDbProperties.getJitterDuration());
        batchOptions = batchOptions.bufferLimit(influxDbProperties.getBufferLimit());

        InfluxDbConnection influxDbConnection = new InfluxDbConnection(
                influxDbProperties.getUrl(), influxDbProperties.getDatabase(), influxDbProperties.getRetentionPolicy(),
                influxDbProperties.getRetentionPolicyTime(), batchOptions);
        influxDbConnection.createRetentionPolicy();

        System.out.println("init influxDb >>>>>>" + influxDbProperties);
        return influxDbConnection;
    }
}


