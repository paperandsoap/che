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
package org.eclipse.che.plugin.docker.client.json.network;

import java.util.Map;

/**
 * author Alexander Garagatyi
 */
public class NewNetwork {
    private String              name;
    private boolean             checkDuplicate;
    private String              driver;
    private boolean             internal;
    private Ipam                iPAM;
    private boolean             enableIPv6;
    private Map<String, String> options;
    private Map<String, String> labels;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NewNetwork withName(String name) {
        this.name = name;
        return this;
    }

    public boolean isCheckDuplicate() {
        return checkDuplicate;
    }

    public void setCheckDuplicate(boolean checkDuplicate) {
        this.checkDuplicate = checkDuplicate;
    }

    public NewNetwork withCheckDuplicate(boolean checkDuplicate) {
        this.checkDuplicate = checkDuplicate;
        return this;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public NewNetwork withDriver(String driver) {
        this.driver = driver;
        return this;
    }

    public boolean isInternal() {
        return internal;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    public NewNetwork withInternal(boolean internal) {
        this.internal = internal;
        return this;
    }

    public Ipam getIPAM() {
        return iPAM;
    }

    public void setIPAM(Ipam iPAM) {
        this.iPAM = iPAM;
    }

    public NewNetwork withIPAM(Ipam iPAM) {
        this.iPAM = iPAM;
        return this;
    }

    public boolean isEnableIPv6() {
        return enableIPv6;
    }

    public void setEnableIPv6(boolean enableIPv6) {
        this.enableIPv6 = enableIPv6;
    }

    public NewNetwork withEnableIPv6(boolean enableIPv6) {
        this.enableIPv6 = enableIPv6;
        return this;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public void setOptions(Map<String, String> options) {
        this.options = options;
    }

    public NewNetwork withOptions(Map<String, String> options) {
        this.options = options;
        return this;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public NewNetwork withLabels(Map<String, String> labels) {
        this.labels = labels;
        return this;
    }

    @Override
    public String toString() {
        return "NewNetwork{" +
               "name='" + name + '\'' +
               ", checkDuplicate=" + checkDuplicate +
               ", driver='" + driver + '\'' +
               ", internal=" + internal +
               ", iPAM=" + iPAM +
               ", enableIPv6=" + enableIPv6 +
               ", options=" + options +
               ", labels=" + labels +
               '}';
    }
}
