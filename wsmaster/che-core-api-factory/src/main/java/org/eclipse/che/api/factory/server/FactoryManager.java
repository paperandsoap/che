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
package org.eclipse.che.api.factory.server;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.factory.server.model.impl.FactoryImpl;
import org.eclipse.che.api.factory.server.snippet.SnippetGenerator;
import org.eclipse.che.api.factory.server.spi.FactoryDao;
import org.eclipse.che.api.factory.shared.model.Factory;
import org.eclipse.che.commons.lang.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.eclipse.che.api.factory.shared.Constants.HTML_SNIPPET_TYPE;
import static org.eclipse.che.api.factory.shared.Constants.IFRAME_SNIPPET_TYPE;
import static org.eclipse.che.api.factory.shared.Constants.MARKDOWN_SNIPPET_TYPE;
import static org.eclipse.che.api.factory.shared.Constants.URL_SNIPPET_TYPE;

/**
 * @author Anton Korneta
 */
@Singleton
public class FactoryManager {

    private static final Logger LOG = LoggerFactory.getLogger(FactoryManager.class);

    @Inject
    private FactoryDao factoryDao;

    /**
     * Save factory to storage and return stored data.
     *
     * @param factory
     *         instance of factory which would be stored
     * @return decorated the factory instance of which has been stored
     * @throws ServerException
     *         when any server errors occurs
     * @throws ConflictException
     *         when stored the factory is already exist
     */
    public Factory createFactory(Factory factory) throws ConflictException,
                                                         ServerException {
        requireNonNull(factory);
        return createFactory(factory, null);
    }

    /**
     * Save factory to storage and return stored data. Field 'factory' should contains factory information.
     * Fields with images should be named 'image'. Acceptable image size 100x100 pixels.
     *
     * @param factory
     *         http request form data
     * @param images
     *         http request form data
     * @return stored data
     * @throws ConflictException
     *         when an error occurred during saving the factory
     * @throws ServerException
     *         when any server errors occurs
     */
    public Factory createFactory(Factory factory, Set<FactoryImage> images) throws ConflictException,
                                                                                   ServerException {
        requireNonNull(factory);
        return factoryDao.create(new FactoryImpl(factory, images));
    }


    /**
     * Updates specified factory with a new factory content.
     *
     * @param update
     *         the new data for the factory
     * @return updated factory with links
     * @throws NotFoundException
     *         when the factory with specified id doesn't not found
     * @throws ServerException
     *         when any server error occurs
     * @throws ConflictException
     *         when not rewritable factory information is present in the new factory
     */
    public Factory updateFactory(Factory update) throws ConflictException,
                                                         NotFoundException,
                                                         ServerException {
        requireNonNull(update);
        return updateFactory(update, null);
    }

    /**
     * Updates specified factory with a new factory content.
     *
     * @param update
     *         the new data for the factory
     * @param images
     *         the new data for the factory
     * @return updated factory with links
     * @throws NotFoundException
     *         when the factory with specified id doesn't not found
     * @throws ServerException
     *         when any server error occurs
     * @throws ConflictException
     *         when not rewritable factory information is present in the new factory
     */
    public Factory updateFactory(Factory update, Set<FactoryImage> images) throws ConflictException,
                                                                                   NotFoundException,
                                                                                   ServerException {
        requireNonNull(update);
        return factoryDao.update(new FactoryImpl(update, images));
    }

    /**
     * Removes factory information from storage by its id.
     *
     * @param id
     *         factory identifier
     * @throws NotFoundException
     *         when the factory with specified id doesn't not found
     * @throws ServerException
     *         when any server errors occurs
     */
    public void removeFactory(String id) throws NotFoundException,
                                                ServerException {
        requireNonNull(id);
        factoryDao.remove(id);
    }

    /**
     * Gets factory by identifier.
     *
     * @param id
     *         factory identifier
     * @return the factory instance if it's found by id
     * @throws NullPointerException
     *         when {@code id} is null
     * @throws NotFoundException
     *         when the factory with specified id doesn't not found
     * @throws ServerException
     *         when any server errors occurs
     */
    public Factory getById(String id) throws NotFoundException,
                                             ServerException {
        requireNonNull(id);
        return factoryDao.getById(id);
    }

    public Set<FactoryImage> getFactoryImages(String factoryId) throws NotFoundException,
                                                                       ServerException {
        requireNonNull(factoryId);
        final Set<FactoryImage> images = factoryDao.getById(factoryId).getImages();
        if (images.isEmpty()) {
            throw new NotFoundException("Default image for factory " + factoryId + " is not found.");
        }
        return images;
    }

    /**
     * Get image information by its id from specified factory.
     *
     * @param factoryId
     *         factory identifier
     * @param imageName
     *         image name
     * @return image information if ids are correct. If imageId is not set, random image of the factory will be returned,
     * if factory has no images, exception will be thrown
     * @throws NotFoundException
     *         when the factory with specified id doesn't not found
     * @throws NotFoundException
     *         when image id is not specified and there is no default image for the specified factory
     * @throws NotFoundException
     *         when image with specified id doesn't exist
     */
    public Set<FactoryImage> getFactoryImages(String factoryId, String imageName) throws NotFoundException,
                                                                                         ServerException {
        requireNonNull(factoryId);
        requireNonNull(imageName);
        final Set<FactoryImage> images = getFactoryImages(factoryId).stream()
                                                                    .filter(img -> imageName.equals(img.getName()))
                                                                    .collect(Collectors.toSet());

        if(images.isEmpty()) {
            throw new NotFoundException("Image with name " + imageName + " is not found.");
        }
        return images;
    }

    /**
     * Get list of factories which conform specified attributes.
     *
     * @param maxItems
     *         max number of items in response
     * @param skipCount
     *         skip items. Must be equals or greater then {@code 0}
     * @param attributes
     *         skip items. Must be equals or greater then {@code 0}
     * @return stored data, if specified attributes is correct
     * @throws ServerException
     *         when any server errors occurs
     */
    public List<? extends Factory> getByAttribute(int maxItems,
                                                  int skipCount,
                                                  List<Pair<String, String>> attributes) throws ServerException {
        return factoryDao.getByAttribute(maxItems, skipCount, attributes);
    }

    /**
     * Gets factory snippet by factory id and snippet type.
     * If snippet type is not set, "url" type will be used as default.
     *
     * @param factoryId
     *         id of factory
     * @param snippetType
     *         type of snippet
     * @param uriInfo
     *         url context
     * @return snippet content.
     * @throws NotFoundException
     *         when factory with specified id doesn't not found - with response code 400
     * @throws ServerException
     *         when any server error occurs during snippet creation
     */
    final String getFactorySnippet(String factoryId,
                                   String snippetType,
                                   UriInfo uriInfo) throws NotFoundException,
                                                           ServerException {
        requireNonNull(factoryId);
        final URI baseUri = uriInfo.getBaseUri();
        final String baseUrl = UriBuilder.fromUri(baseUri)
                                         .replacePath("")
                                         .build()
                                         .toString();
        switch (snippetType) {
            case URL_SNIPPET_TYPE:
                return UriBuilder.fromUri(baseUri)
                                 .replacePath("factory")
                                 .queryParam("id", factoryId)
                                 .build()
                                 .toString();
            case HTML_SNIPPET_TYPE:
                return SnippetGenerator.generateHtmlSnippet(baseUrl, factoryId);
            case IFRAME_SNIPPET_TYPE:
                return SnippetGenerator.generateiFrameSnippet(baseUrl, factoryId);
            case MARKDOWN_SNIPPET_TYPE:
                final Set<FactoryImage> images = getFactoryImages(factoryId);
                final String imageId = (images.size() > 0) ? images.iterator().next().getName()
                                                           : null;
                try {
                    return SnippetGenerator.generateMarkdownSnippet(baseUrl, getById(factoryId), imageId);
                } catch (IllegalArgumentException e) {
                    throw new ServerException(e.getLocalizedMessage());
                }
            default:
                // when the specified type is not supported
                return null;
        }
    }
}
