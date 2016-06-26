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
package org.eclipse.che.ide.project;

public interface ResolvingProjectStateHolder {
    /** Describes state of Resolving Project process. */
    public enum ResolvingProjectState {
        NOT_RESOLVED, IN_PROGRESS, RESOLVED
    }

    ResolvingProjectState getState();
    void setState(ResolvingProjectState state);

    /** Listener that will be called when resolving project state has been changed. */
    public interface ResolvingProjectStateListener {
        void onResolvingProjectStateChanged(ResolvingProjectState state);
    }
}
