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
package org.eclipse.che.ide.ext.java.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Event that describes the state of Resolving Project process.
 * For example, 'Resolving Project process' for a Maven project means reimporting maven model.
 *
 * @author Roman Nikitenko
 */
public class ResolvingProjectEvent extends GwtEvent<ResolvingProjectEvent.ResolvingProjectEventHandler> {
    public static final Type<ResolvingProjectEventHandler> TYPE = new Type<>();

    private final ResolvingProjectState resolvingProjectState;

    public interface ResolvingProjectEventHandler extends EventHandler {
        /** Called when Resolving project process is starting. */
        void onResolvingProjectStarting();

        /** Called when Resolving project process has finished. */
        void onResolvingProjectFinished();
    }

    /** Describes state of Resolving Project process. */
    public enum ResolvingProjectState {
        NOT_RESOLVED, IN_PROGRESS, RESOLVED
    }

    /**
     * Create new {@link ResolvingProjectEvent}.
     *
     * @param state
     *         the state of Resolving Project process
     */
    protected ResolvingProjectEvent(ResolvingProjectState state) {
        this.resolvingProjectState = state;
    }

    /**
     * Creates a Resolving Project Starting event.
     */
    public static ResolvingProjectEvent createResolvingProjectStartingEvent() {
        return new ResolvingProjectEvent(ResolvingProjectState.IN_PROGRESS);
    }

    /**
     * Creates a Resolving Project Finished event.
     */
    public static ResolvingProjectEvent createResolvingProjectFinishedEvent() {
        return new ResolvingProjectEvent(ResolvingProjectState.RESOLVED);
    }

    @Override
    public Type<ResolvingProjectEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ResolvingProjectEventHandler handler) {
        switch (resolvingProjectState) {
            case IN_PROGRESS:
                handler.onResolvingProjectStarting();
                break;
            case RESOLVED:
                handler.onResolvingProjectFinished();
                break;
            default:
                break;
        }
    }
}
