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
package org.apache.isis.applib.fixturescripts;

import java.util.List;

import com.google.common.collect.Lists;

import org.apache.isis.applib.annotation.Named;

@Named("Composite Script")
public abstract class CompositeFixtureScript extends FixtureScript {

    private static Discoverability defaultDiscoverability() {
        return Discoverability.DISCOVERABLE;
    }

    public CompositeFixtureScript(final String localName) {
        this(localName, localName, defaultDiscoverability());
    }
    public CompositeFixtureScript(String friendlyName, final String localName) {
        this(friendlyName, localName, defaultDiscoverability());
    }
    public CompositeFixtureScript(final FixtureScript parent, String friendlyName, final String localName) {
        this(parent, friendlyName, localName, defaultDiscoverability());
    }
    
    public CompositeFixtureScript(final String localName, Discoverability discoverability) {
        super(localName, localName, discoverability);
    }
    public CompositeFixtureScript(String friendlyName, final String localName, Discoverability discoverability) {
        super(friendlyName, localName, discoverability);
    }
    public CompositeFixtureScript(final FixtureScript parent, String friendlyName, final String localName, Discoverability discoverability) {
        super(parent, friendlyName, localName, discoverability);
    }
    
    // //////////////////////////////////////

    private final List<FixtureScript> children = Lists.newArrayList();
    /**
     * Adds a child {@link FixtureScript fixture script} (simply using its default name).
     */
    protected final void add(FixtureScript fixtureScript) {
        add(null, fixtureScript);
    }
    /**
     * Adds a child {@link FixtureScript fixture script}, overriding its default name with one more
     * meaningful in the context of this fixture.
     */
    protected final void add(String localNameOverride, FixtureScript fixtureScript) {
        fixtureScript.setParentPath(pathWith(""));
        if(localNameOverride != null) {
            fixtureScript.setLocalName(localNameOverride);
        }
        getContainer().injectServicesInto(fixtureScript);
        children.add(fixtureScript);
    }

    // //////////////////////////////////////

    /**
     * Mandatory hook method.
     */
    protected abstract void addChildren();

    protected void doRun(FixtureResultList fixtureResults) {
        addChildren();
        for (final FixtureScript child : children) {
            child.doRun(fixtureResults);
        }
    }

}