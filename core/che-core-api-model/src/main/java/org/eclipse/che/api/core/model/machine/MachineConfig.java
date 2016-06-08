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
package org.eclipse.che.api.core.model.machine;

import org.eclipse.che.commons.annotation.Nullable;

import java.util.List;
import java.util.Map;

/**
 * @author gazarenkov
 */
public interface MachineConfig {

    /**
     * Display name.
     */
    String getName();

    /**
     * From where to create this Machine (Recipe/Snapshot).
     */
    MachineSource getSource();

    /**
     * Is workspace bound to machine or not.
     */
    boolean isDev();

    /**
     * Machine type (i.e. "docker").
     */
    String getType();

    /**
     * Machine limits such as RAM size.
     */
    @Nullable
    Limits getLimits();

    /**
     * List of machines from env current machine is depends on.
     * <p/>
     * Machine from depends on list will be available to current machine by host equal to machine's name.
     * Machines from depends on list will be launched before machine that is dependent.
     */
    List<String> getDependsOn();

    /**
     * Get configuration of servers inside of machine.
     *
     * <p>Key is port/transport protocol, e.g. 8080/tcp or 100100/udp
     */
    List<? extends ServerConf> getServers();

    /**
     * Get predefined environment variables of machine
     */
    Map<String, String> getEnvVariables();

    /**
     * Executable used as machine main process
     */
    List<String> getEntrypoint();

    /**
     * Command used as machine main process
     */
    List<String> getCommand();

    /**
     * Links to another machines.
     * Either specify both the machine name and a link alias (MACHINE:ALIAS), or just the machine name
     */
    List<String> getMachineLinks();

    // todo will we allow specifying external port?
    /**
     * Expose ports. Either specify both ports (HOST:CONTAINER), or just the container port (a random host port will be chosen).
     * Example:
     * 3000
     * 3000-3005
     * 8000:8000
     * 9090-9091:8080-8081
     * 49100:22
     */
    List<String> getPorts();

    /**
     * Labels that should be applied to machine
     */
    Map<String, String> getLabels();

    /**
     * Expose ports without publishing them to the host machine - they’ll only be accessible to linked services.
     * Only the internal port can be specified.
     */
    List<String> getExpose();

    /**
     * Container name of machine
     */
    String getContainerName();

    /**
     * Mount all of the volumes from another service or container,
     * optionally specifying read-only access (ro) or read-write (rw).
     * If no access level is specified, then read-write will be used.
     * Example:
     * machine_name
     * machine_name:ro
     * machine_name:rw
     */
    List<String> getVolumesFrom();

    //todo context
}
