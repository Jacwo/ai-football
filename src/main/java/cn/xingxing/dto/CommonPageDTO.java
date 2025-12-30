package cn.xingxing.dto;

import lombok.Data;


/**
 * 公共分页参数DTO
 *
 * @author lilei
 */
@Data
public class CommonPageDTO {

    /**
     * 分页页码
     */

    private Integer pageNo;


    private Integer pageSize;

}
