/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.Level
 *  org.apache.logging.log4j.Logger
 *  org.apache.logging.log4j.core.Appender
 *  org.apache.logging.log4j.core.LifeCycle$State
 *  org.apache.logging.log4j.core.LoggerContext
 *  org.apache.logging.log4j.core.config.Configuration
 *  org.apache.logging.log4j.core.config.ConfigurationSource
 *  org.apache.logging.log4j.core.config.LoggerConfig
 *  org.apache.logging.log4j.core.config.status.StatusConfiguration
 *  org.apache.logging.log4j.status.StatusLogger
 *  org.apache.logging.log4j.util.LoaderUtil
 */
package org.apache.log4j.xml;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.bridge.AppenderAdapter;
import org.apache.log4j.bridge.AppenderWrapper;
import org.apache.log4j.config.Log4j1Configuration;
import org.apache.log4j.config.PropertySetter;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.rewrite.RewritePolicy;
import org.apache.log4j.spi.AppenderAttachable;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.xml.Log4jEntityResolver;
import org.apache.log4j.xml.UnrecognizedElementHandler;
import org.apache.log4j.xml.XmlConfigurationFactory;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LifeCycle;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.status.StatusConfiguration;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.LoaderUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XmlConfiguration
extends Log4j1Configuration {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static final String CONFIGURATION_TAG = "log4j:configuration";
    private static final String OLD_CONFIGURATION_TAG = "configuration";
    private static final String RENDERER_TAG = "renderer";
    private static final String APPENDER_TAG = "appender";
    public static final String PARAM_TAG = "param";
    public static final String LAYOUT_TAG = "layout";
    private static final String CATEGORY = "category";
    private static final String LOGGER_ELEMENT = "logger";
    private static final String CATEGORY_FACTORY_TAG = "categoryFactory";
    private static final String LOGGER_FACTORY_TAG = "loggerFactory";
    public static final String NAME_ATTR = "name";
    private static final String CLASS_ATTR = "class";
    public static final String VALUE_ATTR = "value";
    private static final String ROOT_TAG = "root";
    private static final String LEVEL_TAG = "level";
    private static final String PRIORITY_TAG = "priority";
    public static final String FILTER_TAG = "filter";
    private static final String ERROR_HANDLER_TAG = "errorHandler";
    public static final String REF_ATTR = "ref";
    private static final String ADDITIVITY_ATTR = "additivity";
    private static final String CONFIG_DEBUG_ATTR = "configDebug";
    private static final String INTERNAL_DEBUG_ATTR = "debug";
    private static final String EMPTY_STR = "";
    private static final Class<?>[] ONE_STRING_PARAM = new Class[]{String.class};
    private static final String dbfKey = "javax.xml.parsers.DocumentBuilderFactory";
    private static final String THROWABLE_RENDERER_TAG = "throwableRenderer";
    public static final long DEFAULT_DELAY = 60000L;
    protected static final String TEST_PREFIX = "log4j-test";
    protected static final String DEFAULT_PREFIX = "log4j";
    private Map<String, Appender> appenderMap = new HashMap<String, Appender>();
    private Properties props = null;

    public XmlConfiguration(LoggerContext loggerContext, ConfigurationSource source, int monitorIntervalSeconds) {
        super(loggerContext, source, monitorIntervalSeconds);
    }

    public void addAppenderIfAbsent(Appender appender) {
        this.appenderMap.putIfAbsent(appender.getName(), appender);
    }

    public void doConfigure() throws FactoryConfigurationError {
        final ConfigurationSource source = this.getConfigurationSource();
        ParseAction action = new ParseAction(){

            @Override
            public Document parse(DocumentBuilder parser) throws SAXException, IOException {
                InputSource inputSource = new InputSource(source.getInputStream());
                inputSource.setSystemId("dummy://log4j.dtd");
                return parser.parse(inputSource);
            }

            public String toString() {
                return XmlConfiguration.this.getConfigurationSource().getLocation();
            }
        };
        this.doConfigure(action);
    }

    private void doConfigure(ParseAction action) throws FactoryConfigurationError {
        DocumentBuilderFactory dbf;
        try {
            LOGGER.debug("System property is : {}", (Object)OptionConverter.getSystemProperty(dbfKey, null));
            dbf = DocumentBuilderFactory.newInstance();
            LOGGER.debug("Standard DocumentBuilderFactory search succeded.");
            LOGGER.debug("DocumentBuilderFactory is: " + dbf.getClass().getName());
        }
        catch (FactoryConfigurationError fce) {
            Exception e = fce.getException();
            LOGGER.debug("Could not instantiate a DocumentBuilderFactory.", (Throwable)e);
            throw fce;
        }
        try {
            dbf.setValidating(true);
            DocumentBuilder docBuilder = dbf.newDocumentBuilder();
            docBuilder.setErrorHandler(new SAXErrorHandler());
            docBuilder.setEntityResolver(new Log4jEntityResolver());
            Document doc = action.parse(docBuilder);
            this.parse(doc.getDocumentElement());
        }
        catch (Exception e) {
            if (e instanceof InterruptedException || e instanceof InterruptedIOException) {
                Thread.currentThread().interrupt();
            }
            LOGGER.error("Could not parse " + action.toString() + ".", (Throwable)e);
        }
    }

    @Override
    public Configuration reconfigure() {
        try {
            ConfigurationSource source = this.getConfigurationSource().resetInputStream();
            if (source == null) {
                return null;
            }
            XmlConfigurationFactory factory = new XmlConfigurationFactory();
            XmlConfiguration config = (XmlConfiguration)factory.getConfiguration(this.getLoggerContext(), source);
            return config == null || config.getState() != LifeCycle.State.INITIALIZING ? null : config;
        }
        catch (IOException ex) {
            LOGGER.error("Cannot locate file {}: {}", (Object)this.getConfigurationSource(), (Object)ex);
            return null;
        }
    }

    private void parseUnrecognizedElement(Object instance, Element element, Properties props) throws Exception {
        boolean recognized = false;
        if (instance instanceof UnrecognizedElementHandler) {
            recognized = ((UnrecognizedElementHandler)instance).parseUnrecognizedElement(element, props);
        }
        if (!recognized) {
            LOGGER.warn("Unrecognized element {}", (Object)element.getNodeName());
        }
    }

    private void quietParseUnrecognizedElement(Object instance, Element element, Properties props) {
        try {
            this.parseUnrecognizedElement(instance, element, props);
        }
        catch (Exception ex) {
            if (ex instanceof InterruptedException || ex instanceof InterruptedIOException) {
                Thread.currentThread().interrupt();
            }
            LOGGER.error("Error in extension content: ", (Throwable)ex);
        }
    }

    public String subst(String value, Properties props) {
        try {
            return OptionConverter.substVars(value, props);
        }
        catch (IllegalArgumentException e) {
            LOGGER.warn("Could not perform variable substitution.", (Throwable)e);
            return value;
        }
    }

    public void setParameter(Element elem, PropertySetter propSetter, Properties props) {
        String name = this.subst(elem.getAttribute(NAME_ATTR), props);
        String value = elem.getAttribute(VALUE_ATTR);
        value = this.subst(OptionConverter.convertSpecialChars(value), props);
        propSetter.setProperty(name, value);
    }

    public Object parseElement(Element element, Properties props, Class expectedClass) throws Exception {
        String clazz = this.subst(element.getAttribute(CLASS_ATTR), props);
        Object instance = OptionConverter.instantiateByClassName(clazz, expectedClass, null);
        if (instance != null) {
            PropertySetter propSetter = new PropertySetter(instance);
            NodeList children = element.getChildNodes();
            int length = children.getLength();
            for (int loop = 0; loop < length; ++loop) {
                Node currentNode = children.item(loop);
                if (currentNode.getNodeType() != 1) continue;
                Element currentElement = (Element)currentNode;
                String tagName = currentElement.getTagName();
                if (tagName.equals(PARAM_TAG)) {
                    this.setParameter(currentElement, propSetter, props);
                    continue;
                }
                this.parseUnrecognizedElement(instance, currentElement, props);
            }
            return instance;
        }
        return null;
    }

    private Appender findAppenderByName(Document doc, String appenderName) {
        Appender appender = this.appenderMap.get(appenderName);
        if (appender != null) {
            return appender;
        }
        Element element = null;
        NodeList list = doc.getElementsByTagName(APPENDER_TAG);
        for (int t = 0; t < list.getLength(); ++t) {
            Node node = list.item(t);
            NamedNodeMap map = node.getAttributes();
            Node attrNode = map.getNamedItem(NAME_ATTR);
            if (!appenderName.equals(attrNode.getNodeValue())) continue;
            element = (Element)node;
            break;
        }
        if (element == null) {
            LOGGER.error("No appender named [{}] could be found.", (Object)appenderName);
            return null;
        }
        appender = this.parseAppender(element);
        if (appender != null) {
            this.appenderMap.put(appenderName, appender);
        }
        return appender;
    }

    public Appender findAppenderByReference(Element appenderRef) {
        String appenderName = this.subst(appenderRef.getAttribute(REF_ATTR));
        Document doc = appenderRef.getOwnerDocument();
        return this.findAppenderByName(doc, appenderName);
    }

    public Appender parseAppender(Element appenderElement) {
        String className = this.subst(appenderElement.getAttribute(CLASS_ATTR));
        LOGGER.debug("Class name: [" + className + ']');
        Appender appender = this.manager.parseAppender(className, appenderElement, this);
        if (appender == null) {
            appender = this.buildAppender(className, appenderElement);
        }
        return appender;
    }

    private Appender buildAppender(String className, Element appenderElement) {
        try {
            Appender appender = (Appender)LoaderUtil.newInstanceOf((String)className);
            PropertySetter propSetter = new PropertySetter(appender);
            appender.setName(this.subst(appenderElement.getAttribute(NAME_ATTR)));
            XmlConfiguration.forEachElement(appenderElement.getChildNodes(), currentElement -> {
                switch (currentElement.getTagName()) {
                    case "param": {
                        this.setParameter((Element)currentElement, propSetter);
                        break;
                    }
                    case "layout": {
                        appender.setLayout(this.parseLayout((Element)currentElement));
                        break;
                    }
                    case "filter": {
                        Filter filter = this.parseFilters((Element)currentElement);
                        if (filter == null) break;
                        LOGGER.debug("Adding filter of type [{}] to appender named [{}]", filter.getClass(), (Object)appender.getName());
                        appender.addFilter(filter);
                        break;
                    }
                    case "errorHandler": {
                        this.parseErrorHandler((Element)currentElement, appender);
                        break;
                    }
                    case "appender-ref": {
                        String refName = this.subst(currentElement.getAttribute(REF_ATTR));
                        if (appender instanceof AppenderAttachable) {
                            AppenderAttachable aa = (AppenderAttachable)((Object)appender);
                            Appender child = this.findAppenderByReference((Element)currentElement);
                            LOGGER.debug("Attaching appender named [{}] to appender named [{}].", (Object)refName, (Object)appender.getName());
                            aa.addAppender(child);
                            break;
                        }
                        LOGGER.error("Requesting attachment of appender named [{}] to appender named [{}]which does not implement org.apache.log4j.spi.AppenderAttachable.", (Object)refName, (Object)appender.getName());
                        break;
                    }
                    default: {
                        try {
                            this.parseUnrecognizedElement(appender, (Element)currentElement, this.props);
                            break;
                        }
                        catch (Exception ex) {
                            throw new ConsumerException(ex);
                        }
                    }
                }
            });
            propSetter.activate();
            return appender;
        }
        catch (ConsumerException ex) {
            Throwable t = ex.getCause();
            if (t instanceof InterruptedException || t instanceof InterruptedIOException) {
                Thread.currentThread().interrupt();
            }
            LOGGER.error("Could not create an Appender. Reported error follows.", t);
        }
        catch (Exception oops) {
            if (oops instanceof InterruptedException || oops instanceof InterruptedIOException) {
                Thread.currentThread().interrupt();
            }
            LOGGER.error("Could not create an Appender. Reported error follows.", (Throwable)oops);
        }
        return null;
    }

    public RewritePolicy parseRewritePolicy(Element rewritePolicyElement) {
        String className = this.subst(rewritePolicyElement.getAttribute(CLASS_ATTR));
        LOGGER.debug("Class name: [" + className + ']');
        RewritePolicy policy = this.manager.parseRewritePolicy(className, rewritePolicyElement, this);
        if (policy == null) {
            policy = this.buildRewritePolicy(className, rewritePolicyElement);
        }
        return policy;
    }

    private RewritePolicy buildRewritePolicy(String className, Element element) {
        try {
            RewritePolicy policy = (RewritePolicy)LoaderUtil.newInstanceOf((String)className);
            PropertySetter propSetter = new PropertySetter(policy);
            XmlConfiguration.forEachElement(element.getChildNodes(), currentElement -> {
                if (currentElement.getTagName().equalsIgnoreCase(PARAM_TAG)) {
                    this.setParameter((Element)currentElement, propSetter);
                }
            });
            propSetter.activate();
            return policy;
        }
        catch (ConsumerException ex) {
            Throwable t = ex.getCause();
            if (t instanceof InterruptedException || t instanceof InterruptedIOException) {
                Thread.currentThread().interrupt();
            }
            LOGGER.error("Could not create an RewritePolicy. Reported error follows.", t);
        }
        catch (Exception oops) {
            if (oops instanceof InterruptedException || oops instanceof InterruptedIOException) {
                Thread.currentThread().interrupt();
            }
            LOGGER.error("Could not create an RewritePolicy. Reported error follows.", (Throwable)oops);
        }
        return null;
    }

    private void parseErrorHandler(Element element, Appender appender) {
        org.apache.log4j.spi.ErrorHandler eh = (org.apache.log4j.spi.ErrorHandler)OptionConverter.instantiateByClassName(this.subst(element.getAttribute(CLASS_ATTR)), org.apache.log4j.spi.ErrorHandler.class, null);
        if (eh != null) {
            eh.setAppender(appender);
            PropertySetter propSetter = new PropertySetter(eh);
            XmlConfiguration.forEachElement(element.getChildNodes(), currentElement -> {
                String tagName = currentElement.getTagName();
                if (tagName.equals(PARAM_TAG)) {
                    this.setParameter((Element)currentElement, propSetter);
                }
            });
            propSetter.activate();
            appender.setErrorHandler(eh);
        }
    }

    public Filter parseFilters(Element filterElement) {
        String className = this.subst(filterElement.getAttribute(CLASS_ATTR));
        LOGGER.debug("Class name: [" + className + ']');
        Filter filter = this.manager.parseFilter(className, filterElement, this);
        if (filter == null) {
            PropertySetter propSetter = new PropertySetter(filter);
            XmlConfiguration.forEachElement(filterElement.getChildNodes(), currentElement -> {
                String tagName = currentElement.getTagName();
                if (tagName.equals(PARAM_TAG)) {
                    this.setParameter((Element)currentElement, propSetter);
                } else {
                    this.quietParseUnrecognizedElement(filter, (Element)currentElement, this.props);
                }
            });
            propSetter.activate();
        }
        return filter;
    }

    private void parseCategory(Element loggerElement) {
        String catName = this.subst(loggerElement.getAttribute(NAME_ATTR));
        boolean additivity = OptionConverter.toBoolean(this.subst(loggerElement.getAttribute(ADDITIVITY_ATTR)), true);
        LoggerConfig loggerConfig = this.getLogger(catName);
        if (loggerConfig == null) {
            loggerConfig = new LoggerConfig(catName, org.apache.logging.log4j.Level.ERROR, additivity);
            this.addLogger(catName, loggerConfig);
        } else {
            loggerConfig.setAdditive(additivity);
        }
        this.parseChildrenOfLoggerElement(loggerElement, loggerConfig, false);
    }

    private void parseRoot(Element rootElement) {
        LoggerConfig root = this.getRootLogger();
        this.parseChildrenOfLoggerElement(rootElement, root, true);
    }

    private void parseChildrenOfLoggerElement(Element catElement, LoggerConfig loggerConfig, boolean isRoot) {
        PropertySetter propSetter = new PropertySetter(loggerConfig);
        loggerConfig.getAppenderRefs().clear();
        XmlConfiguration.forEachElement(catElement.getChildNodes(), currentElement -> {
            switch (currentElement.getTagName()) {
                case "appender-ref": {
                    Appender appender = this.findAppenderByReference((Element)currentElement);
                    String refName = this.subst(currentElement.getAttribute(REF_ATTR));
                    if (appender != null) {
                        LOGGER.debug("Adding appender named [{}] to loggerConfig [{}].", (Object)refName, (Object)loggerConfig.getName());
                        loggerConfig.addAppender(this.getAppender(refName), null, null);
                        break;
                    }
                    LOGGER.debug("Appender named [{}] not found.", (Object)refName);
                    break;
                }
                case "level": 
                case "priority": {
                    this.parseLevel((Element)currentElement, loggerConfig, isRoot);
                    break;
                }
                case "param": {
                    this.setParameter((Element)currentElement, propSetter);
                    break;
                }
                default: {
                    this.quietParseUnrecognizedElement(loggerConfig, (Element)currentElement, this.props);
                }
            }
        });
        propSetter.activate();
    }

    public Layout parseLayout(Element layoutElement) {
        String className = this.subst(layoutElement.getAttribute(CLASS_ATTR));
        LOGGER.debug("Parsing layout of class: \"{}\"", (Object)className);
        Layout layout = this.manager.parseLayout(className, layoutElement, this);
        if (layout == null) {
            layout = this.buildLayout(className, layoutElement);
        }
        return layout;
    }

    private Layout buildLayout(String className, Element layout_element) {
        try {
            Layout layout = (Layout)LoaderUtil.newInstanceOf((String)className);
            PropertySetter propSetter = new PropertySetter(layout);
            XmlConfiguration.forEachElement(layout_element.getChildNodes(), currentElement -> {
                String tagName = currentElement.getTagName();
                if (tagName.equals(PARAM_TAG)) {
                    this.setParameter((Element)currentElement, propSetter);
                } else {
                    try {
                        this.parseUnrecognizedElement(layout, (Element)currentElement, this.props);
                    }
                    catch (Exception ex) {
                        throw new ConsumerException(ex);
                    }
                }
            });
            propSetter.activate();
            return layout;
        }
        catch (ConsumerException ce) {
            Throwable cause = ce.getCause();
            if (cause instanceof InterruptedException || cause instanceof InterruptedIOException) {
                Thread.currentThread().interrupt();
            }
            LOGGER.error("Could not create the Layout. Reported error follows.", cause);
        }
        catch (Exception oops) {
            if (oops instanceof InterruptedException || oops instanceof InterruptedIOException) {
                Thread.currentThread().interrupt();
            }
            LOGGER.error("Could not create the Layout. Reported error follows.", (Throwable)oops);
        }
        return null;
    }

    private void parseLevel(Element element, LoggerConfig logger, boolean isRoot) {
        String catName = logger.getName();
        if (isRoot) {
            catName = ROOT_TAG;
        }
        String priStr = this.subst(element.getAttribute(VALUE_ATTR));
        LOGGER.debug("Level value for {} is [{}].", (Object)catName, (Object)priStr);
        if ("inherited".equalsIgnoreCase(priStr) || "null".equalsIgnoreCase(priStr)) {
            if (isRoot) {
                LOGGER.error("Root level cannot be inherited. Ignoring directive.");
            } else {
                logger.setLevel(null);
            }
        } else {
            String className = this.subst(element.getAttribute(CLASS_ATTR));
            if (EMPTY_STR.equals(className)) {
                logger.setLevel(OptionConverter.convertLevel(priStr, org.apache.logging.log4j.Level.DEBUG));
            } else {
                LOGGER.debug("Desired Level sub-class: [{}]", (Object)className);
                try {
                    Class clazz = LoaderUtil.loadClass((String)className);
                    Method toLevelMethod = clazz.getMethod("toLevel", ONE_STRING_PARAM);
                    Level pri = (Level)toLevelMethod.invoke(null, priStr);
                    logger.setLevel(OptionConverter.convertLevel(pri));
                }
                catch (Exception e) {
                    if (e instanceof InterruptedException || e instanceof InterruptedIOException) {
                        Thread.currentThread().interrupt();
                    }
                    LOGGER.error("Could not create level [" + priStr + "]. Reported error follows.", (Throwable)e);
                    return;
                }
            }
        }
        LOGGER.debug("{} level set to {}", (Object)catName, (Object)logger.getLevel());
    }

    private void setParameter(Element elem, PropertySetter propSetter) {
        String name = this.subst(elem.getAttribute(NAME_ATTR));
        String value = elem.getAttribute(VALUE_ATTR);
        value = this.subst(OptionConverter.convertSpecialChars(value));
        propSetter.setProperty(name, value);
    }

    private void parse(Element element) {
        String rootElementName = element.getTagName();
        if (!rootElementName.equals(CONFIGURATION_TAG)) {
            if (rootElementName.equals(OLD_CONFIGURATION_TAG)) {
                LOGGER.warn("The <configuration> element has been deprecated.");
                LOGGER.warn("Use the <log4j:configuration> element instead.");
            } else {
                LOGGER.error("DOM element is - not a <log4j:configuration> element.");
                return;
            }
        }
        String debugAttrib = this.subst(element.getAttribute(INTERNAL_DEBUG_ATTR));
        LOGGER.debug("debug attribute= \"" + debugAttrib + "\".");
        String status = "error";
        if (!debugAttrib.equals(EMPTY_STR) && !debugAttrib.equals("null")) {
            status = OptionConverter.toBoolean(debugAttrib, true) ? INTERNAL_DEBUG_ATTR : "error";
        } else {
            LOGGER.debug("Ignoring debug attribute.");
        }
        String confDebug = this.subst(element.getAttribute(CONFIG_DEBUG_ATTR));
        if (!confDebug.equals(EMPTY_STR) && !confDebug.equals("null")) {
            LOGGER.warn("The \"configDebug\" attribute is deprecated.");
            LOGGER.warn("Use the \"debug\" attribute instead.");
            status = OptionConverter.toBoolean(confDebug, true) ? INTERNAL_DEBUG_ATTR : "error";
        }
        StatusConfiguration statusConfig = new StatusConfiguration().withStatus(status);
        statusConfig.initialize();
        XmlConfiguration.forEachElement(element.getChildNodes(), currentElement -> {
            switch (currentElement.getTagName()) {
                case "category": 
                case "logger": {
                    this.parseCategory((Element)currentElement);
                    break;
                }
                case "root": {
                    this.parseRoot((Element)currentElement);
                    break;
                }
                case "renderer": {
                    LOGGER.warn("Renderers are not supported by Log4j 2 and will be ignored.");
                    break;
                }
                case "throwableRenderer": {
                    LOGGER.warn("Throwable Renderers are not supported by Log4j 2 and will be ignored.");
                    break;
                }
                case "categoryFactory": 
                case "loggerFactory": {
                    LOGGER.warn("Log4j 1 Logger factories are not supported by Log4j 2 and will be ignored.");
                    break;
                }
                case "appender": {
                    Appender appender = this.parseAppender((Element)currentElement);
                    this.appenderMap.put(appender.getName(), appender);
                    if (appender instanceof AppenderWrapper) {
                        this.addAppender(((AppenderWrapper)appender).getAppender());
                        break;
                    }
                    this.addAppender((org.apache.logging.log4j.core.Appender)new AppenderAdapter(appender).getAdapter());
                    break;
                }
                default: {
                    this.quietParseUnrecognizedElement(null, (Element)currentElement, this.props);
                }
            }
        });
    }

    private String subst(String value) {
        return this.getStrSubstitutor().replace(value);
    }

    public static void forEachElement(NodeList list, Consumer<Element> consumer) {
        int length = list.getLength();
        for (int loop = 0; loop < length; ++loop) {
            Node currentNode = list.item(loop);
            if (currentNode.getNodeType() != 1) continue;
            Element currentElement = (Element)currentNode;
            consumer.accept(currentElement);
        }
    }

    private static class ConsumerException
    extends RuntimeException {
        ConsumerException(Exception ex) {
            super(ex);
        }
    }

    private static class SAXErrorHandler
    implements ErrorHandler {
        private static final Logger LOGGER = StatusLogger.getLogger();

        private SAXErrorHandler() {
        }

        @Override
        public void error(SAXParseException ex) {
            SAXErrorHandler.emitMessage("Continuable parsing error ", ex);
        }

        @Override
        public void fatalError(SAXParseException ex) {
            SAXErrorHandler.emitMessage("Fatal parsing error ", ex);
        }

        @Override
        public void warning(SAXParseException ex) {
            SAXErrorHandler.emitMessage("Parsing warning ", ex);
        }

        private static void emitMessage(String msg, SAXParseException ex) {
            LOGGER.warn("{} {} and column {}", (Object)msg, (Object)ex.getLineNumber(), (Object)ex.getColumnNumber());
            LOGGER.warn(ex.getMessage(), (Throwable)ex.getException());
        }
    }

    private static interface ParseAction {
        public Document parse(DocumentBuilder var1) throws SAXException, IOException;
    }
}

