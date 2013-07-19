/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.core.progmodel.facets.object.membergroups;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import com.google.common.io.InputSupplier;
import com.google.common.io.Resources;

import org.apache.isis.applib.annotation.MemberGroupLayout;
import org.apache.isis.applib.annotation.MemberGroupLayout.ColumnSpans;
import org.apache.isis.applib.annotation.MemberGroups;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.object.membergroups.MemberGroupLayoutFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociationFilters;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting.Visitor;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailures;

public class MemberGroupLayoutFacetFactory extends FacetFactoryAbstract implements MetaModelValidatorRefiner {

    public MemberGroupLayoutFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        FacetUtil.addFacet(create(processClassContext));
    }

    private MemberGroupLayoutFacet create(final ProcessClassContext processClassContext) {
        final FacetHolder holder = processClassContext.getFacetHolder();
        
        final Class<?> cls = processClassContext.getCls();

        // try to find property file (different suffices supported)
        final Properties properties = propertiesFor(cls, ".memberGroupLayout", ".memberLayout", ".layout");
        if(properties != null) {
            return new MemberGroupLayoutFacetProperties(properties, holder);
        }
        
        final MemberGroupLayout mglAnnot = Annotations.getAnnotation(cls, MemberGroupLayout.class);
        if (mglAnnot != null) {
            return new MemberGroupLayoutFacetAnnotation(mglAnnot, holder);
        }
        final MemberGroups mgAnnot = Annotations.getAnnotation(cls, MemberGroups.class);
        if (mgAnnot != null) {
            return new MemberGroupsFacetAnnotation(mgAnnot, processClassContext.getFacetHolder());
        }
        return new MemberGroupLayoutFacetFallback(holder); 
    }

    static Properties propertiesFor(final Class<?> cls, final String... suffices) {
        for (String suffix : suffices) {
            final Properties properties = propertiesFor(cls, suffix);
            if(properties != null) {
                return properties;
            }
        }
        return null;
    }
    static Properties propertiesFor(final Class<?> cls, final String suffix) {
        try {
            final URL url = Resources.getResource(cls, cls.getSimpleName()+suffix);
            final InputSupplier<InputStream> inputSupplier = Resources.newInputStreamSupplier(url);
            final Properties properties = new Properties();
            properties.load(inputSupplier.getInput());
            return properties;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void refineMetaModelValidator(MetaModelValidatorComposite metaModelValidator, IsisConfiguration configuration) {
        metaModelValidator.add(new MetaModelValidatorVisiting(newValidatorVisitor()));
    }

    private Visitor newValidatorVisitor() {
        return new MetaModelValidatorVisiting.Visitor() {

            @Override
            public boolean visit(ObjectSpecification objectSpec, ValidationFailures validationFailures) {
                MemberGroupLayoutFacet facet = objectSpec.getFacet(MemberGroupLayoutFacet.class);
                ColumnSpans columnSpans = facet.getColumnSpans();
                final List<String> middle = facet.getMiddle();
                final List<String> right = facet.getRight();
                List<ObjectAssociation> objectCollections = objectSpec.getAssociations(ObjectAssociationFilters.staticallyVisible(Where.OBJECT_FORMS));

                if(columnSpans.getMiddle() == 0 && !middle.isEmpty()) {
                    validationFailures.add("%s MemberGroupLayout: middle (property) column is 0 for ColumnSpans (%s), but groups have been listed (%s).  NB: ColumnSpans may have been defaulted if could not be parsed.", objectSpec.getIdentifier().getClassName(), columnSpans.name(), middle);
                }
                if(columnSpans.getMiddle() > 0 && middle.isEmpty()) {
                    validationFailures.add("%s MemberGroupLayout: middle (property) column is non-zero for ColumnSpans (%s), but no groups have been listed", objectSpec.getIdentifier().getClassName(), columnSpans.name());
                }
                
                if(columnSpans.getRight() == 0 && !right.isEmpty()) {
                    validationFailures.add("%s MemberGroupLayout: right (property) column is 0 for ColumnSpans (%s), but groups have been listed (%s).  NB: ColumnSpans may have been defaulted if could not be parsed.", objectSpec.getIdentifier().getClassName(), columnSpans.name(), right);
                }
                if(columnSpans.getRight() > 0 && right.isEmpty()) {
                    validationFailures.add("%s MemberGroupLayout: right (property) column is non-zero for ColumnSpans (%s), but no groups have been listed", objectSpec.getIdentifier().getClassName(), columnSpans.name());
                }
                
                if(columnSpans.getCollections() == 0 && !objectCollections.isEmpty()) {
                    validationFailures.add("%s MemberGroupLayout: collections column is 0 for ColumnSpans (%s), but there are (up to) %d visible collections visible", objectSpec.getIdentifier().getClassName(), columnSpans.name(), objectCollections.size());
                }
                return true;
            }
        };
    }

}