package grails.test.mixin

import grails.artefact.Artefact
import grails.persistence.Entity
import org.junit.Test
import spock.lang.Specification

@TestFor(SecureUserController)
@Mock([SecurityFilters, User])
class ControllerAndFilterMixinInteractionSpec extends Specification {

    void "verify that controller and filters share the same web request"() {
        setup:
        controller.params.username = "Unknown"
        controller.params.password = "Bad"

        when:
        withFilters(action: "index") {
            controller.index()
        }

        then:
        flash.message == "Sorry, Unknown"
        "/user/login" == response.redirectedUrl
    }
}

@Artefact('Controller')
class SecureUserController {
    def index() {}
}

class SecurityFilters {

    def filters = {
        all(controller: '*', action: '*') {
            before = {
                if (!session.user) {
                    flash.message = "Sorry, Unknown"
                    redirect controller: "user", action: "login"
                }
            }
            after = {

            }
            afterView = {

            }
        }
    }
}

@Entity
class User {
    String username
    String password
}
