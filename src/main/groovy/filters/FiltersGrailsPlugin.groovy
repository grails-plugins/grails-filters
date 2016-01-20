package filters

import grails.plugins.*

class FiltersGrailsPlugin extends Plugin {

    def grailsVersion = "3.1.0.RC1 > *"

    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    def title = "Filters"
    def author = "Jeff Brown"
    def authorEmail = "brownj@ociweb.com"
    def description = '''\
An optional plugin which supports filters support which prior to Grails 3.1 was included in Grails core.
'''
    def profiles = ['web']

    def documentation = "http://grails.org/plugin/filters"

    def license = "APACHE"

    def organization = [ name: "OCI", url: "http://www.ociweb.com/" ]

    def issueManagement = [ system: "GitHub", url: "https://github.com/grails-plugins/filters/issues" ]

    def scm = [ url: "https://github.com/grails-plugins/filters" ]

    Closure doWithSpring() { {->
            // TODO Implement runtime spring config (optional)
        }
    }

    void doWithDynamicMethods() {
        // TODO Implement registering dynamic methods to classes (optional)
    }

    void doWithApplicationContext() {
        // TODO Implement post initialization spring config (optional)
    }

    void onChange(Map<String, Object> event) {
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    void onConfigChange(Map<String, Object> event) {
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    void onShutdown(Map<String, Object> event) {
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
