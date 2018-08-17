/*
 * Copyright (C) 2012-2018 52°North Initiative for Geospatial Open Source
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
package org.n52.sta.edm.provider.entities;

import static org.n52.sta.edm.provider.SensorThingsEdmConstants.NAMESPACE;
import static org.n52.sta.edm.provider.entities.ObservationEntityProvider.ET_OBSERVATION_FQN;
import static org.n52.sta.edm.provider.entities.ObservationEntityProvider.ET_OBSERVATION_NAME;
import static org.n52.sta.edm.provider.entities.ObservationEntityProvider.ES_OBSERVATIONS_NAME;
import static org.n52.sta.edm.provider.entities.ObservedPropertyEntityProvider.ET_OBSERVED_PROPERTY_FQN;
import static org.n52.sta.edm.provider.entities.ObservedPropertyEntityProvider.ET_OBSERVED_PROPERTY_NAME;
import static org.n52.sta.edm.provider.entities.ObservedPropertyEntityProvider.ES_OBSERVED_PROPERTIES_NAME;
import static org.n52.sta.edm.provider.entities.SensorEntityProvider.ET_SENSOR_FQN;
import static org.n52.sta.edm.provider.entities.SensorEntityProvider.ET_SENSOR_NAME;
import static org.n52.sta.edm.provider.entities.SensorEntityProvider.ES_SENSORS_NAME;
import static org.n52.sta.edm.provider.entities.ThingEntityProvider.ES_THINGS_NAME;
import static org.n52.sta.edm.provider.entities.ThingEntityProvider.ET_THING_FQN;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationPropertyBinding;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.springframework.stereotype.Component;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
public class DatastreamEntityProvider extends AbstractSensorThingsEntityProvider {

    // Entity Type Name
    public static final String ET_DATASTREAM_NAME = "Datastream";
    public static final FullQualifiedName ET_DATASTREAM_FQN = new FullQualifiedName(NAMESPACE, ET_DATASTREAM_NAME);

    // Entity Set Name
    public static final String ES_DATASTREAMS_NAME = "Datastreams";
    
    // Entity Navigation Property Names
    private static final String NAV_LINK_NAME_THINGS = ES_THINGS_NAME + NAVIGATION_LINK_ANNOTATION;
    private static final String NAV_LINK_NAME_SENSORS = ET_SENSOR_NAME + NAVIGATION_LINK_ANNOTATION;
    private static final String NAV_LINK_NAME_OBSERVED_PROPERTY = ET_OBSERVED_PROPERTY_NAME + NAVIGATION_LINK_ANNOTATION;
    private static final String NAV_LINK_NAME_OBSERVATION = ET_OBSERVATION_NAME + NAVIGATION_LINK_ANNOTATION;

    @Override
    protected CsdlEntityType createEntityType() {
    	//create EntityType properties
        CsdlProperty id = new CsdlProperty().setName(ID_ANNOTATION).setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
        CsdlProperty name = new CsdlProperty().setName(PROP_NAME).setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
        CsdlProperty description = new CsdlProperty().setName(PROP_DESCRIPTION).setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
        
        //TODO: Change to correct Type (JSON instead of String)
        CsdlProperty unitOfMeasurement = new CsdlProperty().setName(PROP_UOM).setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
        CsdlProperty observationType = new CsdlProperty().setName(PROP_OBSERVATION_TYPE).setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
        CsdlProperty observedArea = new CsdlProperty().setName(PROP_OBSERVED_AREA).setType(EdmPrimitiveTypeKind.Geometry.getFullQualifiedName());
        CsdlProperty phenomenonTime = new CsdlProperty().setName(PROP_PHENOMENON_TIME).setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
        CsdlProperty resultTime = new CsdlProperty().setName(PROP_RESULT_TIME).setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
        
        CsdlProperty selfLink = new CsdlProperty().setName(SELF_LINK_ANNOTATION).setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
        CsdlProperty navLinkThings = new CsdlProperty().setName(NAV_LINK_NAME_THINGS).setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
        CsdlProperty navLinkSensor = new CsdlProperty().setName(NAV_LINK_NAME_SENSORS).setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
        CsdlProperty navLinkObservedProperty = new CsdlProperty().setName(NAV_LINK_NAME_OBSERVED_PROPERTY).setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
        CsdlProperty navLinkObservation = new CsdlProperty().setName(NAV_LINK_NAME_OBSERVATION).setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
        
        
        // navigation property: Many optional to one mandatory
        CsdlNavigationProperty navPropThings = new CsdlNavigationProperty()
                .setName(ES_THINGS_NAME)
                .setType(ET_THING_FQN)
                .setCollection(true)
                .setPartner(ET_DATASTREAM_NAME);
        
        // navigation property: Many optional to one mandatory        
        CsdlNavigationProperty navPropSensor = new CsdlNavigationProperty()
                .setName(ET_SENSOR_NAME)
                .setType(ET_SENSOR_FQN)
                .setCollection(true)
                .setPartner(ES_DATASTREAMS_NAME);
        
        // navigation property: Many optional to one mandatory
        CsdlNavigationProperty navPropObservedProperty = new CsdlNavigationProperty()
                .setName(ET_OBSERVED_PROPERTY_NAME)
                .setType(ET_OBSERVED_PROPERTY_FQN)
                .setCollection(true)
                .setPartner(ES_DATASTREAMS_NAME);
        
        // navigation property: One mandatory to many optional
        CsdlNavigationProperty navPropObservation = new CsdlNavigationProperty()
                .setName(ET_OBSERVATION_NAME)
                .setType(ET_OBSERVATION_FQN)
                .setCollection(true)
                .setPartner(ET_DATASTREAM_NAME);
        
        List<CsdlNavigationProperty> navPropList = new ArrayList<CsdlNavigationProperty>();
        navPropList.addAll(Arrays.asList(navPropThings, navPropSensor, navPropObservedProperty, navPropObservation));
        
        // create CsdlPropertyRef for Key element
        CsdlPropertyRef propertyRef = new CsdlPropertyRef();
        propertyRef.setName(ID_ANNOTATION);

        // configure EntityType
        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName(ET_DATASTREAM_NAME);
        entityType.setProperties(Arrays.asList(
        		id,
        		name,
        		description,
        		unitOfMeasurement,
        		observationType,
        		observedArea,
        		phenomenonTime,
        		resultTime,
        		selfLink,
        		navLinkThings,
        		navLinkSensor,
        		navLinkObservedProperty,
        		navLinkObservation));
        entityType.setKey(Collections.singletonList(propertyRef));
        entityType.setNavigationProperties(navPropList);

        return entityType;
     }

    @Override
    protected CsdlEntitySet createEntitySet() {
        CsdlEntitySet entitySet = new CsdlEntitySet();
        entitySet.setName(ES_DATASTREAMS_NAME);
        entitySet.setType(ET_DATASTREAM_FQN);

        CsdlNavigationPropertyBinding navPropThingBinding = new CsdlNavigationPropertyBinding();
        navPropThingBinding.setPath(ES_THINGS_NAME); // the path from entity type to navigation property
        navPropThingBinding.setTarget(ES_THINGS_NAME); //target entitySet, where the nav prop points to

        CsdlNavigationPropertyBinding navPropSensorBinding = new CsdlNavigationPropertyBinding();
        navPropSensorBinding.setPath(ET_SENSOR_NAME);
        navPropSensorBinding.setTarget(ES_SENSORS_NAME);
        
        CsdlNavigationPropertyBinding navPropObservedPropertyBinding = new CsdlNavigationPropertyBinding();
        navPropObservedPropertyBinding.setPath(ET_OBSERVED_PROPERTY_NAME);
        navPropObservedPropertyBinding.setTarget(ES_OBSERVED_PROPERTIES_NAME);
        
        CsdlNavigationPropertyBinding navPropObservationBinding = new CsdlNavigationPropertyBinding();
        navPropObservationBinding.setPath(ET_OBSERVATION_NAME);
        navPropObservationBinding.setTarget(ES_OBSERVATIONS_NAME);

        List<CsdlNavigationPropertyBinding> navPropBindingList = new ArrayList<CsdlNavigationPropertyBinding>();
        navPropBindingList.addAll(Arrays.asList(
        		navPropThingBinding,
        		navPropSensorBinding,
        		navPropObservedPropertyBinding,
        		navPropObservationBinding)
		);
        
        entitySet.setNavigationPropertyBindings(navPropBindingList);
        return entitySet;	
    }

    @Override
    public FullQualifiedName getFullQualifiedTypeName() {
        return ET_DATASTREAM_FQN;
    }
}
