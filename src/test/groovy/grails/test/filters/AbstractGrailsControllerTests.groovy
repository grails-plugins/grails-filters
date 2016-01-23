package grails.test.filters

import grails.core.DefaultGrailsApplication
import grails.core.GrailsApplication
import grails.util.GrailsWebMockUtil
import grails.util.Holders
import grails.util.Metadata
import grails.web.databinding.DataBindingUtils
import grails.web.databinding.GrailsWebDataBinder
import org.grails.compiler.injection.GrailsAwareClassLoader
import org.grails.databinding.converters.DateConversionHelper
import org.grails.datastore.gorm.config.GrailsDomainClassMappingContext
import org.grails.plugins.DefaultGrailsPlugin
import org.grails.plugins.MockGrailsPluginManager
import org.grails.plugins.testing.GrailsMockHttpServletRequest
import org.grails.plugins.testing.GrailsMockHttpServletResponse
import org.grails.support.MockApplicationContext
import org.grails.web.converters.configuration.ConvertersConfigurationHolder
import org.grails.web.servlet.context.support.WebRuntimeSpringConfiguration
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.grails.web.util.GrailsApplicationAttributes
import org.springframework.context.ApplicationContext
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.request.RequestContextHolder

abstract class AbstractGrailsControllerTests extends GroovyTestCase {

	def servletContext
	GrailsWebRequest webRequest
	MockHttpServletRequest request
	org.springframework.mock.web.MockHttpServletResponse response
	GroovyClassLoader gcl = new GrailsAwareClassLoader(getClass().classLoader)
	GrailsApplication ga
	def mockManager
	MockApplicationContext ctx
	ApplicationContext appCtx

	protected void onSetUp() {}

	protected void setUp() {
		super.setUp()

		ExpandoMetaClass.enableGlobally()

		GroovySystem.metaClassRegistry.metaClassCreationHandle = new ExpandoMetaClassCreationHandle()

		ctx = new MockApplicationContext()
		onSetUp()
		ga = new DefaultGrailsApplication(gcl.getLoadedClasses().findAll { clazz -> !Closure.isAssignableFrom(clazz) } as Class[], gcl)

		def binder = new GrailsWebDataBinder(ga)
		binder.registerConverter new DateConversionHelper()

		ctx.registerMockBean(DataBindingUtils.DATA_BINDER_BEAN_NAME, binder)

		ga.metadata[Metadata.APPLICATION_NAME] = getClass().name
		mockManager = new MockGrailsPluginManager(ga)
		ctx.registerMockBean("manager", mockManager)
		def dependantPluginClasses = [
				"org.grails.plugins.CoreGrailsPlugin",
				"org.grails.plugins.web.mapping.UrlMappingsGrailsPlugin",
				"org.grails.plugins.web.filters.FiltersGrailsPlugin"
		].collect { className ->
			gcl.loadClass(className)
		}
		def dependentPlugins = dependantPluginClasses.collect { new DefaultGrailsPlugin(it, ga)}

		dependentPlugins.each { mockManager.registerMockPlugin(it); it.manager = mockManager }

		ga.initialise()

		ga.setApplicationContext(ctx)

		ctx.registerMockBean(GrailsApplication.APPLICATION_ID, ga)

		def springConfig = new WebRuntimeSpringConfiguration(ctx)
		servletContext = ctx.getServletContext()

		springConfig.servletContext = servletContext

		dependentPlugins*.doWithRuntimeConfiguration(springConfig)

		ctx.registerMockBean("grailsDomainClassMappingContext", new GrailsDomainClassMappingContext(ga))
		ga.mainContext = springConfig.getUnrefreshedApplicationContext()
		appCtx = springConfig.getApplicationContext()

		dependentPlugins*.doWithApplicationContext(appCtx)
		servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, appCtx)
		servletContext.setAttribute(GrailsApplicationAttributes.APPLICATION_CONTEXT, appCtx)

		request = new GrailsMockHttpServletRequest(characterEncoding: "utf-8")
		response = new GrailsMockHttpServletResponse()
		webRequest = GrailsWebMockUtil.bindMockWebRequest(appCtx, request, response)
	}

	protected void tearDown() {
		ga.mainContext.close()
		RequestContextHolder.resetRequestAttributes()
		ExpandoMetaClass.disableGlobally()

		Holders.config = null
		Holders.grailsApplication = null
		Holders.setPluginManager(null)

		ConvertersConfigurationHolder.getInstance().clear()

		super.tearDown()
	}
}
