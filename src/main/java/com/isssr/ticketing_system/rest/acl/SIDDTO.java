package com.isssr.ticketing_system.rest.acl;

import com.wordnik.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class SIDDTO {

    @NotNull
    @ApiModelProperty(required = true)
    private Long id;

    @NotNull
    @ApiModelProperty(required = true)
    private Integer principal;

    private String sidName;

    public SIDDTO(Long id, Integer principal) {
        this.id = id;
        this.principal = principal;
        this.sidName = "";
    }

    public SIDDTO(@NotNull Long id, @NotNull Integer principal, String sidName) {
        this.id = id;
        this.principal = principal;
        this.sidName = sidName;
    }
}
