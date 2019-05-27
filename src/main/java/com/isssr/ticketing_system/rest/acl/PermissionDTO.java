package com.isssr.ticketing_system.rest.acl;

import com.wordnik.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class PermissionDTO {

    @NotNull
    @NotEmpty
    @ApiModelProperty(required = true)
    private Character permission;

    @NotNull
    @ApiModelProperty(required = true)
    private boolean grant;


    public PermissionDTO(@NotNull @NotEmpty Character permission, @NotNull boolean grant) {
        this.permission = permission;
        this.grant = grant;
    }

    public PermissionDTO(int permission, @NotNull boolean grant) {

        switch (permission) {
            case 1:
                this.permission = 'R';
                break;
            case 2:
                this.permission = 'W';
                break;
            case 4:
                this.permission = 'C';
                break;
            case 8:
                this.permission = 'D';
                break;
        }
        this.grant = grant;
    }

    public static List<PermissionDTO> getBlank() {

        List<PermissionDTO> list = new ArrayList<>();

        list.add(new PermissionDTO('R', false));
        list.add(new PermissionDTO('W', false));
        list.add(new PermissionDTO('C', false));
        list.add(new PermissionDTO('D', false));

        return list;

    }

    public static void setPermissionOnList(List<PermissionDTO> list, Character c, boolean grant) {

        for (PermissionDTO p : list) {
            if (p.getPermission().equals(c)) {
                p.setGrant(grant);
            }
        }

    }
}
