package com.isssr.ticketing_system.rest.acl;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.wordnik.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class ACLDTO {

    @JsonProperty(value = "sid")
    @NotNull
    @ApiModelProperty(required = true)
    private SIDDTO siddto;

    @JsonProperty(value = "domain_object_type")
    @NotNull
    @NotEmpty
    @ApiModelProperty(required = true)
    private String domainObjectType;

    @JsonProperty(value = "domain_object_id")
    @NotNull
    @ApiModelProperty(required = true)
    private Long domainObjectId;

    @JsonProperty(value = "perms")
    @NotNull
    @Size(min = 4, max = 4)
    @ApiModelProperty(required = true)
    private List<PermissionDTO> perms;

}
