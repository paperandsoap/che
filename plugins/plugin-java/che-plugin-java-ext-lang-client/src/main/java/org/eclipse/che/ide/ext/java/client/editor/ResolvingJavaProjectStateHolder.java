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
package org.eclipse.che.ide.ext.java.client.editor;

import org.eclipse.che.ide.project.ResolvingProjectStateHolder;

import javax.inject.Singleton;

import static org.eclipse.che.ide.project.ResolvingProjectStateHolder.ResolvingProjectState.NOT_RESOLVED;

@Singleton
public class ResolvingJavaProjectStateHolder implements ResolvingProjectStateHolder {
    private ResolvingProjectState state;

    public ResolvingJavaProjectStateHolder() {
        this.state = NOT_RESOLVED;
    }

    @Override
    public ResolvingProjectState getState() {
        return state;
    }

    @Override
    public void setState(ResolvingProjectState state) {
        this.state = state;
    }
}
