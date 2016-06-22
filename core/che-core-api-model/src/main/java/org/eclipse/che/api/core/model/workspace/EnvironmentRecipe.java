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
package org.eclipse.che.api.core.model.workspace;

/**
 * @author Alexander Garagatyi
 */
public interface EnvironmentRecipe {

    /**
     * Type of the environment, e.g. opencompose
     */
    String getType();

    /**
     * Content type of the environment recipe
     */
    String getContentType();

    /**
     * Content of an environment recipe.
     * Content and location fields are mutually exclusive
     */
    String getContent();

    /**
     * Location of an environment recipe.
     * Content and location fields are mutually exclusive
     */
    String getLocation();
}