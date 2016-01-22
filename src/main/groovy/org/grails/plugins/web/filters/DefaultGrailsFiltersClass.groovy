/*
 * Copyright 2004-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.grails.plugins.web.filters

import org.grails.core.AbstractInjectableGrailsClass
import org.grails.plugins.web.filters.FilterConfig
import org.grails.plugins.web.filters.FilterLoader
import org.grails.plugins.web.filters.GrailsFiltersClass

/**
 * Loads filter definitions into a set of FilterConfig instances.
 *
 * @author mike
 * @author Graeme Rocher
 */
class DefaultGrailsFiltersClass extends AbstractInjectableGrailsClass implements GrailsFiltersClass {
    static FILTERS = "Filters"

    DefaultGrailsFiltersClass(Class aClass) {
        super(aClass, FILTERS)
    }

    List<FilterConfig> getConfigs(Object filters) {

        if (!filters) return []

        def loader = new FilterLoader(filters)
        def filtersClosure = filters.filters
        filtersClosure.delegate = loader
        filtersClosure.call()

        return loader.filters
    }

    @Override
    MetaClass getMetaClass() {
        GroovySystem.metaClassRegistry.getMetaClass org.grails.plugins.web.filters.DefaultGrailsFiltersClass
    }
}

