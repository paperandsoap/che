/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.user.server.model.impl;

import org.eclipse.che.api.core.model.user.Profile;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Data object for the {@link Profile}.
 *
 * @author Yevhenii Voevodin
 */
@Entity(name = "Profile")
@Table
public class ProfileImpl implements Profile {

    @Id
    private String userId;

    @OneToOne
    @PrimaryKeyJoinColumn
    public UserImpl user;

    public UserImpl getUser() {
        return user;
    }

    @Transient
    private String email;
    /*

             <element-collection name="attributes" target-class="java.lang.String">
                <map-key-class class="java.lang.String"/>
                <map-key-column name="KEY" nullable="false"/>
                <column name="VALUE" />r
                <collection-table name="PROFILE_ATTRIBUTES" >
                    <join-column name="PROFILE_ID" />
                </collection-table>
            </element-collection>

     */
    @Transient
    private Map<String, String> attributes;

    public ProfileImpl() {}

    public ProfileImpl(String userId, String email) {
        this.userId = userId;
        this.email = email;
    }

    public ProfileImpl(String userId, String email, Map<String, String> attributes) {
        this.userId = userId;
        this.email = email;
        if (attributes != null) {
            this.attributes = new HashMap<>(attributes);
        }
    }

    public ProfileImpl(Profile profile) {
        this(profile.getUserId(), profile.getEmail(), profile.getAttributes());
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public Map<String, String> getAttributes() {
        if (attributes == null) {
            this.attributes = new HashMap<>();
        }
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ProfileImpl)) {
            return false;
        }
        final ProfileImpl that = (ProfileImpl)obj;
        return Objects.equals(userId, that.userId)
               && Objects.equals(email, that.email)
               && getAttributes().equals(that.getAttributes());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(userId);
        hash = 31 * hash + Objects.hashCode(email);
        hash = 31 * hash + getAttributes().hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return "ProfileImpl{" +
               "userId='" + userId + '\'' +
               ", email='" + email + '\'' +
               ", attributes=" + attributes +
               '}';
    }
}
