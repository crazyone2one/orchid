package cn.master.backend.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author Created by 11's papa on 08/16/2024
 **/
@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class SelectOption {

    private String text;
    private String value;
}
