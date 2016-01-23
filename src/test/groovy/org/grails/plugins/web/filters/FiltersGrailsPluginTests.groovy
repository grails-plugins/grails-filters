package org.grails.plugins.web.filters

import grails.plugins.GrailsPlugin
import grails.test.filters.AbstractFilterTests

/**
 * @author Graeme Rocher
 * @since 1.0
 */
class FiltersGrailsPluginTests extends AbstractFilterTests {

    protected void onSetUp() {
        gcl.parseClass """
class Filters {
    def filters = {
        all(controller:"*", action:"*") {
            before = {

            }
            after = {

            }
            afterView = {

            }
        }
    }
}"""
    }

    void testSpringConfig() {
        assertTrue appCtx.containsBean("filterInterceptor")
        assertTrue appCtx.containsBean("Filters")
        assertTrue appCtx.containsBean("FiltersClass")
    }

    void testOnChange() {
        def newFilter = gcl.parseClass('''
class Filters {
    def filters = {
        all(controller:"author", action:"list") {
            before = {
                println "different"
            }
            after = {

            }
            afterView = {

            }
        }
    }
}
        ''')

        mockManager.getGrailsPlugin("filters").notifyOfEvent(GrailsPlugin.EVENT_ON_CHANGE, newFilter)

        assertTrue appCtx.containsBean("filterInterceptor")
        assertTrue appCtx.containsBean("Filters")
        assertTrue appCtx.containsBean("FiltersClass")

        def configs = appCtx.getBean("FiltersClass").getConfigs(appCtx.getBean("Filters"))

        assertEquals "author", configs[0].scope.controller
        assertEquals "list", configs[0].scope.action
    }
}
