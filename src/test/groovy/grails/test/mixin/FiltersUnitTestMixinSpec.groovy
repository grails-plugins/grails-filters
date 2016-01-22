package grails.test.mixin

import grails.artefact.Artefact
import grails.test.mixin.web.FiltersUnitTestMixin
import grails.test.runtime.FreshRuntime
import org.springframework.beans.factory.config.MethodInvokingFactoryBean
import spock.lang.Specification

@TestMixin(FiltersUnitTestMixin)
@FreshRuntime
class FiltersUnitTestMixinSpec extends Specification {

    AuthorController controller
    AutowiredService autowiredService

    void setUp() {
        controller = mockController(AuthorController)
    }

    void cleanup() {
        runtime.publishEvent("resetGrailsApplication")
    }

    void "test filter invocation explicit controller and action"() {
        setup:
        mockFilters(SimpleFilters)

        when:
        withFilters(controller: "author", action: "list") {
            controller.list()
        }

        then:
        request.filterBefore == 'one'
        request.filterAfter == 'two [authors:[bob, fred]]'
        request.filterView == 'done'
    }

    void "test filter invocation implicit controller and action"() {
        setup:
        mockFilters(SimpleFilters)

        when:
        withFilters(action: "list") {
            controller.list()
        }

        then:
        request.filterBefore == 'one'
        request.filterAfter == 'two [authors:[bob, fred]]'
        request.filterView == 'done'
    }

    void "test cancelling filter invocation"() {
        setup:
        mockFilters(CancellingFilters)

        when:
        withFilters(action: "list") {
            controller.list()
        }

        then:
        request.filterBefore == "one"
        !request.filterAfter
        !request.filterView
        response.redirectedUrl == '/book/list'

    }

    void "test exception throwing filter"() {
        setup:
        mockFilters(ExceptionThrowingFilters)

        when:
        withFilters(action: "list") {
            controller.list()
        }

        then:
        thrown(Exception)
        !request.filterBefore
        !request.filterAfter
        !request.filterView
        request.exception
    }

    void "test filter is auto wired"() {
        setup:
        defineBeans {
            autowiredService(MethodInvokingFactoryBean) {
                targetObject = this
                targetMethod = 'setupService'
            }
        }
        mockFilters(AutowiredFilters)

        when:
        withFilters(action: "list") {
            controller.list()
        }

        then:
        1 == autowiredService.sessionSetupCounter
    }

    void "test filter is autowired with beans defined after mocking"() {
        setup:
        mockFilters(AutowiredFilters)
        defineBeans {
            autowiredService(MethodInvokingFactoryBean) {
                targetObject = this
                targetMethod = 'setupService'
            }
        }

        when:
        withFilters(action: "list") {
            controller.list()
        }

        then:
        1 == autowiredService.sessionSetupCounter
    }

    AutowiredService setupService() {
        this.autowiredService = new AutowiredService()
    }
}

@Artefact("Controller")
class AuthorController {
    def list = { [authors: ['bob', 'fred']] }
}

class SimpleFilters {
    def filters = {
        all(controller: "author", action: "list") {
            before = {
                request.filterBefore = "one"
            }
            after = { model ->
                request.filterAfter = "two ${model}"
            }
            afterView = {
                request.filterView = "done"
            }
        }
    }
}

class CancellingFilters {
    def filters = {
        all(controller: "author", action: "list") {
            before = {
                request.filterBefore = "one"
                redirect(controller: "book", action: "list")
                return false
            }
            after = { model ->
                request.filterAfter = "two ${model}"
            }
            afterView = {
                request.filterView = "done"
            }
        }
    }
}

class ExceptionThrowingFilters {
    def filters = {
        all(controller: "author", action: "list") {
            before = {
                throw new Exception("bad")
            }
            after = { model ->
                request.filterAfter = "two ${model}"
            }
            afterView = { e ->
                request.exception = e
            }
        }
    }
}

class AutowiredFilters {

    def autowiredService

    def filters = {
        all(controller: "author", action: "list") {
            before = {
                autowiredService.setupSession()
            }
        }
    }
}

class AutowiredService {
    int sessionSetupCounter = 0

    void setupSession() {
        sessionSetupCounter++
    }
}
