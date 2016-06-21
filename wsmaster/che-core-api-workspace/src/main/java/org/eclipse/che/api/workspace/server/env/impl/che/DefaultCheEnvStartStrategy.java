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
package org.eclipse.che.api.workspace.server.env.impl.che;

import org.eclipse.che.api.core.model.machine.MachineConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * author Alexander Garagatyi
 */
public class DefaultCheEnvStartStrategy implements CheEnvStartStrategy {
    @Override
    public List<MachineConfig> order(List<MachineConfig> configs) throws IllegalArgumentException {
        configs = new ArrayList<>(configs);

        // move start of dependent machines after machines they depends on
        Map<String, Integer> weights = weightMachines(configs);

        configs = sortByWeight(configs, weights);

        return configs;
    }

    private Map<String, Integer> weightMachines(List<MachineConfig> configs) throws IllegalArgumentException {
        HashMap<String, Integer> weights = new HashMap<>();
        Set<String> machinesLeft = configs.stream()
                                          .map(MachineConfig::getName)
                                          .collect(Collectors.toSet());

        // should not happen if config was validated before usage in engine
        if (machinesLeft.size() != configs.size()) {
            throw new IllegalArgumentException("Configs contains machines with duplicate name");
        }

        // create dependency graph
        Map<String, List<String>> dependencies = new HashMap<>(configs.size());
        for (MachineConfig config : configs) {
            ArrayList<String> machineDependencies = new ArrayList<>(config.getDependsOn().size() + config.getMachineLinks().size());

            machineDependencies.addAll(config.getDependsOn());

            for (String link : config.getMachineLinks()) {
                machineDependencies.add(getServiceFromMachineLink(link));
            }
            dependencies.put(config.getName(), machineDependencies);
        }

        boolean weightEvaluatedInCycleRun = true;
        while (weights.size() != dependencies.size() && weightEvaluatedInCycleRun) {
            weightEvaluatedInCycleRun = false;
            for (String service : dependencies.keySet()) {
                // todo if type is not `docker` put machine in the end of start queue
                // process not yet processed machines only
                if (machinesLeft.contains(service)) {
                    if (dependencies.get(service).size() == 0) {
                        // no links - smallest weight 0
                        weights.put(service, 0);
                        machinesLeft.remove(service);
                        weightEvaluatedInCycleRun = true;
                    } else {
                        // machine has depends on entry - check if it has not weighted connection
                        Optional<String> nonWeightedLink = dependencies.get(service)
                                                                       .stream()
                                                                       .filter(machinesLeft::contains)
                                                                       .findAny();
                        if (!nonWeightedLink.isPresent()) {
                            // all connections are weighted - lets evaluate current machine
                            Optional<String> maxWeight = dependencies.get(service)
                                                                     .stream()
                                                                     .max((o1, o2) -> weights.get(o1).compareTo(weights.get(o2)));
                            // optional can't empty because size of the list is checked above
                            //noinspection OptionalGetWithoutIsPresent
                            weights.put(service, weights.get(maxWeight.get()) + 1);
                            machinesLeft.remove(service);
                            weightEvaluatedInCycleRun = true;
                        }
                    }
                }
            }
        }

        if (weights.size() != configs.size()) {
            throw new IllegalArgumentException("Launch order of machines " + machinesLeft + " can't be evaluated");
        }

        return weights;
    }

    private String getServiceFromMachineLink(String link) {
        String service = link;
        if (link != null) {
            String[] split = service.split(":");
            if (split.length != 1 && split.length != 2) {
                throw new IllegalArgumentException(format("Service link %s is invalid", link));
            }
            service = split[0];
        }
        return service;
    }

    private List<MachineConfig> sortByWeight(List<MachineConfig> configs, Map<String, Integer> weights) {
        configs.sort((o1, o2) -> weights.get(o1.getName()).compareTo(weights.get(o2.getName())));
        return configs;
    }
}
