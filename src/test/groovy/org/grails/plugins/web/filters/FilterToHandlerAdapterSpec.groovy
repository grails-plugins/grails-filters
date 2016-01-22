package org.grails.plugins.web.filters

import grails.core.DefaultGrailsApplication
import grails.web.CamelCaseUrlConverter
import grails.web.UrlConverter
import org.grails.core.DefaultGrailsControllerClass
import org.springframework.context.support.GenericApplicationContext
import spock.lang.Specification

/**
 * @author Graeme Rocher
 * @since 1.0
 */
class FilterToHandlerAdapterSpec extends Specification {

    void "test URI Mapping"() {
        setup:
        def filterAdapter = new FilterToHandlerAdapter()
        filterAdapter.filterConfig = new FilterConfig()
        filterAdapter.filterConfig.scope.uri = "/restricted/**"

        when:
        filterAdapter.afterPropertiesSet()

        then:
        filterAdapter.accept("Ignore", "index", "/restricted/1", null, null)
        filterAdapter.accept("Ignore", "index", "/restricted/1/2", null, null)
        filterAdapter.accept("Ignore", "index", "/restricted;", null, null)
        !filterAdapter.accept("Ignore", "index", "/foo/1/2", null, null)
    }

    void "test URI Mapping2"() {
        setup:
        def filterAdapter = new FilterToHandlerAdapter()
        filterAdapter.filterConfig = new FilterConfig()
        filterAdapter.filterConfig.scope.controller = "trol"
        filterAdapter.filterConfig.scope.find = true

        when:
        filterAdapter.afterPropertiesSet()

        then:
        filterAdapter.accept("Controller", "index", "/restricted/1", null, null)
        filterAdapter.accept("Controller", "index", "/restricted/1/2", null, null)
        filterAdapter.accept("Controller", "index", "/foo/1/2", null, null)
        !filterAdapter.accept("Contoller", "index", "/foo/1/2", null, null)
    }

    void "test URI Mapping 3"() {
        setup:
        def filterAdapter = new FilterToHandlerAdapter()
        filterAdapter.filterConfig = new FilterConfig()
        filterAdapter.filterConfig.scope.controller = ".*trol.*"
        filterAdapter.filterConfig.scope.action = "index"
        filterAdapter.filterConfig.scope.invert = true
        filterAdapter.filterConfig.scope.find = false
        filterAdapter.filterConfig.scope.regex = true

        when:
        filterAdapter.afterPropertiesSet()

        then:
        !filterAdapter.accept("Controller", "index", "/restricted/1", null, null)
        !filterAdapter.accept("Controller", "index", "/restricted/1/2", null, null)
        !filterAdapter.accept("Controller", "index", "/foo/1/2", null, null)
        filterAdapter.accept("Contoller", "index", "/foo/1/2", null, null)
    }

    void "test default action with controller match and action wild card"() {
        setup:
        def filterAdapter = new FilterToHandlerAdapter()
        filterAdapter.filterConfig = new FilterConfig()
        filterAdapter.filterConfig.scope.controller = "demo"
        filterAdapter.filterConfig.scope.action = "*"

        when:
        filterAdapter.afterPropertiesSet()

        then:
        filterAdapter.accept("demo", null, "/ignored", null, null)
    }

    void "test default action with controller mismatch and action wild card"() {
        setup:
        def filterAdapter = new FilterToHandlerAdapter()
        filterAdapter.filterConfig = new FilterConfig()
        filterAdapter.filterConfig.scope.controller = "demo"
        filterAdapter.filterConfig.scope.action = "*"

        when:
        filterAdapter.afterPropertiesSet()

        then:
        filterAdapter.accept("auth", null, "/ignored", null, null)
    }

    void "test default action with controller match and action mismatch"() {
        setup:
        def filterAdapter = new FilterToHandlerAdapter()
        filterAdapter.filterConfig = new FilterConfig()
        filterAdapter.filterConfig.scope.controller = "demo"
        filterAdapter.filterConfig.scope.action = "foo"

        when:
        filterAdapter.afterPropertiesSet()

        then:
        filterAdapter.accept("demo", null, "/ignored", null, null)
    }

    void "test default action with controller match and action match"() {
        setup:
        def application = new DefaultGrailsApplication([DemoController] as Class[], getClass().classLoader)
        def mainContext = new GenericApplicationContext()
        mainContext.defaultListableBeanFactory.registerSingleton UrlConverter.BEAN_NAME, new CamelCaseUrlConverter()
        application.mainContext = mainContext
        application.initialise()
        def filterAdapter = new FilterToHandlerAdapter(grailsApplication: application)
        filterAdapter.filterConfig = new FilterConfig()
        filterAdapter.filterConfig.scope.controller = "demo"
        filterAdapter.filterConfig.scope.action = "index"
        def controllerClass = application?.getArtefactByLogicalPropertyName(DefaultGrailsControllerClass.CONTROLLER, "demo")

        when:
        filterAdapter.afterPropertiesSet()

        then:
        filterAdapter.accept("demo", null, "/ignored", null, controllerClass)
    }

    void "test default action with controller match and no action specified in config"() {
        setup:
        def filterAdapter = new FilterToHandlerAdapter()
        filterAdapter.filterConfig = new FilterConfig()
        filterAdapter.filterConfig.scope.controller = "demo"

        when:
        filterAdapter.afterPropertiesSet()

        then:
        filterAdapter.accept("demo", null, "/ignored", null, null)
    }

    void "test app root with wild carded controller and action"() {
        setup:
        def filterAdapter = new FilterToHandlerAdapter()
        filterAdapter.filterConfig = new FilterConfig()
        filterAdapter.filterConfig.scope.controller = "*"
        filterAdapter.filterConfig.scope.action = "*"

        when:
        filterAdapter.afterPropertiesSet()

        then:
        filterAdapter.accept(null, null, '/', null, null)
    }

    void "test app root with wild carded controller and action regex"() {
        setup:
        def filterAdapter = new FilterToHandlerAdapter()
        filterAdapter.filterConfig = new FilterConfig()
        filterAdapter.filterConfig.scope.controller = ".*"
        filterAdapter.filterConfig.scope.action = ".*"
        filterAdapter.filterConfig.scope.regex = true

        when:
        filterAdapter.afterPropertiesSet()

        then:
        filterAdapter.accept(null, null, '/', null, null)
    }

    void "test app root with wild carded controller and no action"() {
        setup:
        def filterAdapter = new FilterToHandlerAdapter()
        filterAdapter.filterConfig = new FilterConfig()
        filterAdapter.filterConfig.scope.controller = "*"

        when:
        filterAdapter.afterPropertiesSet()

        then:
        filterAdapter.accept(null, null, '/', null, null)
    }

    void "test app root with wild carded controller and specificAction"() {
        setup:
        def filterAdapter = new FilterToHandlerAdapter()
        filterAdapter.filterConfig = new FilterConfig()
        filterAdapter.filterConfig.scope.controller = "*"
        filterAdapter.filterConfig.scope.action = "something"

        when:
        filterAdapter.afterPropertiesSet()

        then:
        filterAdapter.accept(null, null, '/', null, null)
    }

    void "test app root with specific controller and wild carded action"() {
        setup:
        def filterAdapter = new FilterToHandlerAdapter()
        filterAdapter.filterConfig = new FilterConfig()
        filterAdapter.filterConfig.scope.controller = "something"
        filterAdapter.filterConfig.scope.action = "*"

        when:
        filterAdapter.afterPropertiesSet()

        then:
        filterAdapter.accept(null, null, '/', null, null)
    }

    void "test namespace mismatch controller mismatch and action wildcard"() {
        setup:
        def filterAdapter = new FilterToHandlerAdapter()
        filterAdapter.filterConfig = new FilterConfig()
        filterAdapter.filterConfig.scope.controller = "demo"
        filterAdapter.filterConfig.scope.action = "*"
        filterAdapter.filterConfig.scope.namespace = "demo"

        when:
        filterAdapter.afterPropertiesSet()

        then:
        filterAdapter.accept("demo", null, "/ignored", "namespace", null)
    }
}

class DemoController {
    def index = {}
}
