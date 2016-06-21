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
package org.eclipse.che.api.workspace.shared.dto;

import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.dto.shared.DTO;

import java.util.Map;

/**
 * @author Alexander Garagatyi
 */
@DTO
public interface EnvironmentDto extends Environment {

    @Override
    EnvironmentRecipeDto getRecipe();

    void setRecipe(EnvironmentRecipeDto recipe);

    EnvironmentDto withRecipe(EnvironmentRecipeDto recipe);

    @Override
    Map<String, MachineExtensionDto> getMachines();

    void setMachines(Map<String, MachineExtensionDto> machines);

    EnvironmentDto withMachines(Map<String, MachineExtensionDto> machines);
}
