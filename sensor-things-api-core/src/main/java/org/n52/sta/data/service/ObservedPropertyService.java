/*
 * Copyright (C) 2018-2019 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.sta.data.service;

import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.series.db.beans.sta.ObservablePropertyEntity;
import org.n52.sta.data.query.DatastreamQuerySpecifications;
import org.n52.sta.data.query.ObservedPropertyQuerySpecifications;
import org.n52.sta.data.repositories.DatastreamRepository;
import org.n52.sta.data.repositories.PhenomenonRepository;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.n52.sta.edm.provider.entities.DatastreamEntityProvider;
import org.n52.sta.exception.STACRUDException;
import org.n52.sta.mapping.ObservedPropertyMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
@DependsOn({"springApplicationContext"})
public class ObservedPropertyService extends AbstractSensorThingsEntityService<PhenomenonRepository, PhenomenonEntity> {

    private static final Logger logger = LoggerFactory.getLogger(ObservedPropertyService.class);

    private static final DatastreamQuerySpecifications dQS = new DatastreamQuerySpecifications();
    private static final ObservedPropertyQuerySpecifications oQS = new ObservedPropertyQuerySpecifications();

    private final DatastreamRepository datastreamRepository;

    private ObservedPropertyMapper mapper;

    @Autowired
    public ObservedPropertyService(PhenomenonRepository repository,
                                   ObservedPropertyMapper mapper,
                                   DatastreamRepository datastreamRepository) {
        super(repository, PhenomenonEntity.class);
        this.mapper = mapper;
        this.datastreamRepository = datastreamRepository;
    }

    @Override
    public EntityTypes getType() {
        return EntityTypes.ObservedProperty;
    }

    /**
     * Overrides the default method as different field is used to store identifier
     *
     * @param relatedId   ID of the related Entity
     * @param relatedType EntityType of the related Entity
     * @return identifier of the entity. can be null if entity is nout found
     */
    @Override
    public String getEntityIdByRelatedEntity(String relatedId, String relatedType) {
        return getRepository().getStaIdentifier(this.byRelatedEntityFilter(relatedId, relatedType, null));
    }

    @Override
    public boolean existsEntityByRelatedEntity(String relatedId, String relatedType, String ownId) {
        switch (relatedType) {
            case IOT_DATASTREAM: {
                return getRepository().findOne(byRelatedEntityFilter(relatedId, relatedType, ownId)).isPresent();
            }
            default:
                return false;
        }
    }

    @Override
    public Specification<PhenomenonEntity> byRelatedEntityFilter(String relatedId,
                                                                 String relatedType,
                                                                 String ownId) {
        switch (relatedType) {
            case IOT_DATASTREAM: {
                return (root, query, builder) -> {
                    Subquery<PhenomenonEntity> sq = query.subquery(PhenomenonEntity.class);
                    Root<DatastreamEntity> datastream = sq.from(DatastreamEntity.class);
                    Join<DatastreamEntity, PhenomenonEntity> join =
                            datastream.join(DatastreamEntity.PROPERTY_OBSERVABLE_PROPERTY);
                    sq.select(join)
                            .where(builder.equal(datastream.get(DescribableEntity.PROPERTY_IDENTIFIER), relatedId));
                    if (ownId != null) {
                        return builder.and(builder.in(root).value(sq), builder.equal(root.get(STAIDENTIFIER), ownId));
                    }
                    return builder.in(root).value(sq);
                };
            }
            default:
                return null;
        }
    }

    @Override
    public String checkPropertyName(String property) {
        switch (property) {
            case "definition":
                return DataEntity.PROPERTY_IDENTIFIER;
            case "identifier":
                return STAIDENTIFIER;
            default:
                return super.checkPropertyName(property);
        }
    }

    @Override
    public PhenomenonEntity create(PhenomenonEntity observableProperty) throws STACRUDException {
        if (observableProperty.getStaIdentifier() != null && !observableProperty.isSetName()) {
            return getRepository().findByStaIdentifier(observableProperty.getStaIdentifier()).get();
        }
        if (observableProperty.getStaIdentifier() == null) {
            if (getRepository().existsByName(observableProperty.getName())) {
                Optional<PhenomenonEntity> optional
                        = getRepository().findOne(oQS.withName(observableProperty.getName()));
                return optional.isPresent() ? optional.get() : null;
            } else {
                // Autogenerate Identifier
                observableProperty.setStaIdentifier(UUID.randomUUID().toString());
            }
        } else if (getRepository().existsByStaIdentifier(observableProperty.getStaIdentifier())) {
            throw new STACRUDException("Identifier already exists!", HttpStatus.BAD_REQUEST);
        }
        return getRepository().save(getAsPhenomenonEntity(observableProperty));
    }

    @Override
    public PhenomenonEntity update(PhenomenonEntity entity, HttpMethod method) throws STACRUDException {
        checkUpdate(entity);
        if (HttpMethod.PATCH.equals(method)) {
            Optional<PhenomenonEntity> existing = getRepository().findByStaIdentifier(entity.getStaIdentifier());
            if (existing.isPresent()) {
                PhenomenonEntity merged = mapper.merge(existing.get(), entity);
                return getRepository().save(getAsPhenomenonEntity(merged));
            }
            throw new STACRUDException("Unable to update. Entity not found.", HttpStatus.NOT_FOUND);
        } else if (HttpMethod.PUT.equals(method)) {
            throw new STACRUDException("Http PUT is not yet supported!", HttpStatus.NOT_IMPLEMENTED);
        }
        throw new STACRUDException("Invalid http method for updating entity!", HttpStatus.BAD_REQUEST);
    }

    @Override
    protected PhenomenonEntity update(PhenomenonEntity entity) {
        return getRepository().save(getAsPhenomenonEntity(entity));
    }

    private void checkUpdate(PhenomenonEntity entity) throws STACRUDException {
        if (entity instanceof ObservablePropertyEntity) {
            ObservablePropertyEntity observableProperty = (ObservablePropertyEntity) entity;
            if (observableProperty.hasDatastreams()) {
                for (DatastreamEntity datastream : observableProperty.getDatastreams()) {
                    checkInlineDatastream(datastream);
                }
            }
        }
    }

    @Override
    public void delete(String id) throws STACRUDException {
        if (getRepository().existsByStaIdentifier(id)) {
            // delete datastreams
            datastreamRepository.findAll(dQS.withObservedPropertyIdentifier(id)).forEach(d -> {
                try {
                    getDatastreamService().delete(d.getIdentifier());
                } catch (STACRUDException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            });
            getRepository().deleteByStaIdentifier(id);
        } else {
            throw new STACRUDException(
                    "Unable to delete. Entity not found.", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    protected void delete(PhenomenonEntity entity) {
        getRepository().deleteByStaIdentifier(entity.getStaIdentifier());
    }

    @Override
    protected PhenomenonEntity createOrUpdate(PhenomenonEntity entity) throws STACRUDException {
        if (entity.getStaIdentifier() != null && getRepository().existsByStaIdentifier(entity.getStaIdentifier())) {
            return update(entity, HttpMethod.PATCH);
        }
        return create(entity);
    }

    private PhenomenonEntity getAsPhenomenonEntity(PhenomenonEntity observableProperty) {
        return observableProperty instanceof ObservablePropertyEntity
                ? ((ObservablePropertyEntity) observableProperty).asPhenomenonEntity()
                : observableProperty;
    }

    @SuppressWarnings("unchecked")
    private AbstractSensorThingsEntityService<?, DatastreamEntity> getDatastreamService() {
        return (AbstractSensorThingsEntityService<?, DatastreamEntity>) getEntityService(
                EntityTypes.Datastream);
    }

    /* (non-Javadoc)
     * @see org.n52.sta.mapping.AbstractMapper#getRelatedCollections(java.lang.Object)
     */
    @Override
    public Map<String, Set<String>> getRelatedCollections(Object rawObject) {
        Map<String, Set<String>> collections = new HashMap<>();
        PhenomenonEntity entity = (PhenomenonEntity) rawObject;

        List<DatastreamEntity> observations = datastreamRepository
                .findAll(dQS.withObservedPropertyIdentifier(entity.getStaIdentifier()));
        collections.put(
                DatastreamEntityProvider.ET_DATASTREAM_NAME,
                observations
                        .stream()
                        .map(DatastreamEntity::getIdentifier)
                        .collect(Collectors.toSet()));
        return collections;
    }
}
