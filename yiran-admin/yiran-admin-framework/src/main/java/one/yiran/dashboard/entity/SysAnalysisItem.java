package one.yiran.dashboard.entity;

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
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "SYS_ANALYSIS_ITEM",indexes = {
        @Index(name = "idx_ty_date",columnList = "type,belongDate"),
        @Index(name = "idx_chid_type",columnList = "channelId,type")
})
public class SysAnalysisItem extends TimedBasedEntity {

    public static final String TYPE_MEMBER = "MEMBER";


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "TYPE",length = 32,nullable = false)
    private String type;//

    @Column(length = 32,nullable = true)
    private String subType;

    @Column
    private Long channelId;

    @Column(length = 32,nullable = false)
    private String keyName;

    @Column(length = 32,nullable = false)
    private String value;

    @Column
    private LocalDate belongDate;
}
