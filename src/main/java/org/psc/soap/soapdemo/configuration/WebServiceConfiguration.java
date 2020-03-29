package org.psc.soap.soapdemo.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.wss4j.common.ext.WSPasswordCallback;
import org.apache.wss4j.dom.handler.WSHandlerConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor;
import org.springframework.ws.soap.security.wss4j2.callback.SimplePasswordValidationCallbackHandler;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@EnableWs
@Configuration
public class WebServiceConfiguration extends WsConfigurerAdapter {
    public static final String TARGET_NAMESPACE = "http://schema.soapdemo.soap.psc.org";

    @Value("${service-path:/service/soap}")
    private String servicePath;

    @Override
    public void addInterceptors(List<EndpointInterceptor> interceptors) {
        interceptors.add(wss4jSecurityInterceptor());
        interceptors.add(endpointInterceptor());
        log.info("n");
    }

    @Bean
    public EndpointInterceptor endpointInterceptor() {

        return new EndpointInterceptor() {
            @Override
            public boolean handleRequest(MessageContext messageContext, Object endpoint) throws Exception {
                //                messageContext.getRequest().getPayloadResult();
                //                messageContext.getProperty("Username");
                SaajSoapMessage soapMessage = (SaajSoapMessage) messageContext.getRequest();
                //                log.info("debug");
                return true;
            }

            @Override
            public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
                return true;
            }

            @Override
            public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
                return true;
            }

            @Override
            public void afterCompletion(MessageContext messageContext, Object endpoint, Exception ex) throws Exception {

            }
        };

    }


    @Bean
    public Wss4jSecurityInterceptor wss4jSecurityInterceptor() {
        Wss4jSecurityInterceptor wss4jSecurityInterceptor = new Wss4jSecurityInterceptor();
        wss4jSecurityInterceptor.setValidationActions(WSHandlerConstants.USERNAME_TOKEN);
        Map<String, String> users = new ConcurrentHashMap<String, String>() {{
            put("test", "password123");
            put("defaultUser", "password123");
            put("test2", "password123");
        }};
        SimplePasswordValidationCallbackHandler simplePasswordValidationCallbackHandler =
                new SimplePasswordValidationCallbackHandler();
        simplePasswordValidationCallbackHandler.setUsersMap(users);
        // for testing
        //wss4jSecurityInterceptor.setValidationCallbackHandler(new UnsafeUsernameTokenCallbackHandler());
        wss4jSecurityInterceptor.setValidationCallbackHandlers(
                new CallbackHandler[]{simplePasswordValidationCallbackHandler, new UsernameLoggingCallbackHandler()});
        return wss4jSecurityInterceptor;
    }

    @Bean
    public ServletRegistrationBean messageDispatcherServlet(ApplicationContext applicationContext) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(applicationContext);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, servicePath.concat("/*"));
    }

    @Bean(name = "worker")
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema workerSchema) {
        DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
        wsdl11Definition.setPortTypeName("WorkerPort");
        wsdl11Definition.setLocationUri(servicePath);
        wsdl11Definition.setTargetNamespace(TARGET_NAMESPACE);
        wsdl11Definition.setSchema(workerSchema);
        return wsdl11Definition;
    }

    @Bean
    public XsdSchema workerSchema() {
        return new SimpleXsdSchema(new ClassPathResource("xsd/worker.xsd"));
    }

    private static class UnsafeUsernameTokenCallbackHandler implements CallbackHandler {

        @Override
        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            for (Callback callback : callbacks) {
                if (callback instanceof WSPasswordCallback) {
                    WSPasswordCallback wsPasswordCallback = (WSPasswordCallback) callback;
                    wsPasswordCallback.setPassword("");
                }
            }

        }
    }

    private static class UsernameLoggingCallbackHandler implements CallbackHandler {

        @Override
        public void handle(Callback[] callbacks) {
            for (Callback callback : callbacks) {
                if (callback instanceof WSPasswordCallback) {
                    WSPasswordCallback wsPasswordCallback = (WSPasswordCallback) callback;
                    log.info("username: {}", wsPasswordCallback.getIdentifier());
                }
            }

        }
    }
}
