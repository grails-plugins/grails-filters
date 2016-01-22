package grails.test.runtime

import grails.test.mixin.UseTestPlugin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import grails.test.mixin.services.ServiceUnitTestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.test.mixin.support.MixinInstance
import grails.test.mixin.support.TestMixinRuntimeSupport
import grails.test.mixin.web.ControllerUnitTestMixin
import grails.test.mixin.web.FiltersUnitTestMixin
import grails.test.mixin.web.GroovyPageUnitTestMixin
import grails.test.mixin.webflow.WebFlowUnitTestMixin
import spock.lang.Specification
import spock.lang.Unroll

class TestRuntimeFactorySpec extends Specification {
    @Unroll
    def "should instantiate correct plugins and features for #testClass.simpleName"() {
        when:
        def testRuntime = TestRuntimeFactory.getRuntimeForTestClass(testClass)

        then:
        testRuntime.features == features as Set
        testRuntime.plugins.collect {
            it.class.simpleName
        } as Set == plugins as Set

        where:
        testClass                             | features                                                                   | plugins
        SampleFiltersTestClass                | ['coreBeans', 'controller', 'filters', 'grailsApplication']                | ['CoreBeansTestPlugin', 'ControllerTestPlugin', 'GrailsApplicationTestPlugin', 'MetaClassCleanerTestPlugin', 'FiltersTestPlugin']
        SampleDomainAndFiltersTestClass       | ['coreBeans', 'domainClass', 'controller', 'filters', 'grailsApplication'] | ['CoreBeansTestPlugin', 'DomainClassTestPlugin', 'GrailsApplicationTestPlugin', 'MetaClassCleanerTestPlugin', 'ControllerTestPlugin', 'FiltersTestPlugin']
        SampleDomainWithFiltersUsageTestClass | ['coreBeans', 'domainClass', 'filters', 'grailsApplication']               | ['CoreBeansTestPlugin', 'DomainClassTestPlugin', 'GrailsApplicationTestPlugin', 'MetaClassCleanerTestPlugin', 'ControllerTestPlugin', 'FiltersTestPlugin']
    }

    @Unroll
    def "should throw exception when feature is missing for #testClass.simpleName"() {
        when:
        def testRuntime = TestRuntimeFactory.getRuntimeForTestClass(testClass)

        then:
        TestRuntimeFactoryException e = thrown()
        e.message == "No plugin available for feature $missingFeature"

        where:
        testClass                                               | missingFeature
        SampleDomainAndFiltersWithExcludedDomainPluginTestClass | 'domainClass'
    }
}


class SampleFiltersTestClass {
    @MixinInstance
    private static FiltersUnitTestMixin mixinInstance = new FiltersUnitTestMixin()
}

class SampleDomainAndFiltersTestClass {
    @MixinInstance
    private static DomainClassUnitTestMixin domainClassMixinInstance = new DomainClassUnitTestMixin()
    @MixinInstance
    private static FiltersUnitTestMixin filtersMixinInstance = new FiltersUnitTestMixin()
}

@UseTestPlugin(FiltersTestPlugin)
class SampleDomainWithFiltersUsageTestClass {
    @MixinInstance
    private static DomainClassUnitTestMixin domainClassMixinInstance = new DomainClassUnitTestMixin()
}

@UseTestPlugin(value = [DomainClassTestPlugin], exclude = true)
class SampleDomainAndFiltersWithExcludedDomainPluginTestClass {
    @MixinInstance
    private static DomainClassUnitTestMixin domainClassMixinInstance = new DomainClassUnitTestMixin()
    @MixinInstance
    private static FiltersUnitTestMixin filtersMixinInstance = new FiltersUnitTestMixin()
}