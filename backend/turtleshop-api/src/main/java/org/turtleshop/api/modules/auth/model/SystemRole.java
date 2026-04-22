package org.turtleshop.api.modules.auth.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SystemRole {
    private Integer roleId;
    private String name;
    private String description;
}