package com.isssr.ticketing_system.acl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.model.Sid;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "acl_sid")
public class Authority {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // possible values
    // 1 for existing user
    // 0 for roles
    @Column(name = "principal")
    private Integer principal;

    // username OR userType name
    @Column(name = "sid")
    @Enumerated(EnumType.STRING)
    @Getter(AccessLevel.NONE)
    @JsonProperty(value = "sid")
    private AuthorityName sid;

    public Authority(AuthorityName sid) {
        this.sid = sid;
    }

    @JsonIgnore
    public String getAuthorityString() {
        return this.sid.toString();
    }

    public Sid convertToSid() {
        return new GrantedAuthoritySid(this.getAuthorityString());
    }

    public AuthorityName getAuthorityName() {
        return this.sid;
    }
}
