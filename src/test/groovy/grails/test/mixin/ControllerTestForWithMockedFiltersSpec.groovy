package grails.test.mixin

import grails.artefact.Artefact
import grails.web.Controller
import spock.lang.Specification

@TestFor(FirstController)
@Mock(RedirectingFilters)
class ControllerTestForWithMockedFiltersSpec extends Specification {

    void "test redirecting Filter"() {
        when:
        // GRAILS-7657
        withFilters(controller: 'first', action: 'list') {
            controller.list()
        }

        then:
        response.redirectedUrl == '/second'
    }
}

@Controller
class FirstController {
    def list = {}
}

@Artefact("Filters")
class RedirectingFilters {
    def filters = {
        all(controller: 'first', action: 'list') {
            before = {
                redirect(controller: 'second')
                return false
            }
        }
    }
}