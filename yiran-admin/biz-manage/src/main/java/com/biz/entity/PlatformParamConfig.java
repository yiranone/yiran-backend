package com.biz.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import one.yiran.db.common.domain.TimedBasedEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "PLATFORM_PARAM_CONFIG",
        indexes = {@Index(name = "idx_cc_query_key",  columnList="channelId, configKey", unique = true)})
public class PlatformParamConfig extends TimedBasedEntity {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long channelId;

    @Column(length = 10)
    private String configGroup;

    @Column(length = 32)
    private String valueType;

    @Column(length = 32)
    private String configKey;

    @Column(length = 32)
    private String configName;

    @Column(length = 2048)
    private String configValue;

    @Column(length = 1024)
    private String description;

}
