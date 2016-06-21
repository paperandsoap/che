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
package org.eclipse.che.api.user.server.jpa;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.spi.ProfileDao;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

@Singleton
public class JpaProfileDao implements ProfileDao {

    @Inject
    private EntityManagerFactory factory;

    @Override
    public void create(ProfileImpl profile) throws ServerException, ConflictException {
        requireNonNull(profile, "Required non-null profile");
        final EntityManager manager = factory.createEntityManager();
        try {
            manager.getTransaction().begin();
            manager.persist(profile);
            manager.getTransaction().commit();
        } catch (RuntimeException x) {
            throw new ServerException(x.getLocalizedMessage(), x);
        } finally {
            if (manager.getTransaction().isActive()) {
                manager.getTransaction().rollback();
            }
            manager.close();
        }
    }

    @Override
    public void update(ProfileImpl profile) throws NotFoundException, ServerException {

    }

    @Override
    public void remove(String id) throws ServerException {

    }

    @Override
    public ProfileImpl getById(String userId) throws NotFoundException, ServerException {
        requireNonNull(userId, "Required non-null id");
        final EntityManager manager = factory.createEntityManager();
        try {
            final ProfileImpl profile = manager.find(ProfileImpl.class, userId);
            if (profile == null) {
                throw new NotFoundException(format("Couldn't find profile for user with id '%s'", userId));
            }
            return profile;
        } finally {
            manager.close();
        }
    }
}
