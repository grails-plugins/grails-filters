package org.grails.plugins.web.filters

import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author Graeme Rocher
 * @since 1.0
 */
class CompositeInterceptorSpec extends Specification {

    void "test composite interceptor post handle"() {
        setup:
        def t1
        def t2
        def i1 = [postHandle: { HttpServletRequest request, HttpServletResponse response, Object o, ModelAndView mv -> t1 = 'foo'; true }] as HandlerInterceptor
        def i2 = [postHandle: { HttpServletRequest request, HttpServletResponse response, Object o, ModelAndView mv -> t2 = 'bar'; true }] as HandlerInterceptor

        def ci = new CompositeInterceptor()
        ci.handlers = [i1, i2]

        when:
        ci.postHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), "boo", null)

        then:
        'foo' == t1
        'bar' == t2
    }

    void testCompositeInterceptorAfterCompletion() {
        def t1
        def t2
        def i1 = [afterCompletion: { HttpServletRequest request, HttpServletResponse response, Object o, Exception e -> t1 = 'foo'; true }] as HandlerInterceptor
        def i2 = [afterCompletion: { HttpServletRequest request, HttpServletResponse response, Object o, Exception e -> t2 = 'bar'; true }] as HandlerInterceptor

        def ci = new CompositeInterceptor()
        ci.handlers = [i1, i2]

        ci.afterCompletion(new MockHttpServletRequest(), new MockHttpServletResponse(), "boo", null)
        assertEquals 'foo', t1
        assertEquals 'bar', t2
    }

    void testCompositeInterceptorPreHandle() {
        def t1
        def t2
        def i1 = [preHandle: { HttpServletRequest request, HttpServletResponse response, Object o -> t1 = 'foo'; true }] as HandlerInterceptor
        def i2 = [preHandle: { HttpServletRequest request, HttpServletResponse response, Object o -> t2 = 'bar'; true }] as HandlerInterceptor

        def ci = new CompositeInterceptor()
        ci.handlers = [i1, i2]

        ci.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), "boo")
        assertEquals 'foo', t1
        assertEquals 'bar', t2
    }
}
