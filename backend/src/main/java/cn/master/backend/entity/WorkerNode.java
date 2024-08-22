package cn.master.backend.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serial;

/**
 * DB WorkerID Assigner for UID Generator 实体类。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DB WorkerID Assigner for UID Generator")
@Table("worker_node")
public class WorkerNode implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * auto increment id
     */
    @Id(keyType = KeyType.Auto)
    @Schema(description = "auto increment id")
    private Long id;

    /**
     * host name
     */
    @Schema(description = "host name")
    private String hostName;

    /**
     * port
     */
    @Schema(description = "port")
    private String port;

    /**
     * node type: ACTUAL or CONTAINER
     */
    @Schema(description = "node type: ACTUAL or CONTAINER")
    private Integer type;

    /**
     * launch date
     */
    @Schema(description = "launch date")
    private Long launchDate;

    /**
     * modified time
     */
    @Schema(description = "modified time")
    private Long modified;

    /**
     * created time
     */
    @Schema(description = "created time")
    private Long created;

}
