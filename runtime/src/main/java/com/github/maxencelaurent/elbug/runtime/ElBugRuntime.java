package com.github.maxencelaurent.elbug.runtime;

import fish.payara.micro.BootstrapException;
import fish.payara.micro.PayaraMicro;
import fish.payara.micro.PayaraMicroRuntime;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.PropertyResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.h2.Driver;

/**
 * @author maxence
 */
public class ElBugRuntime {

    private static final Logger logger = LoggerFactory.getLogger(ElBugRuntime.class);

    private static final Map<String, String> env;

    public static final String ELBUG_DB_NAME_KEY = "elBug.db.name";
    public static final String ELBUG_DB_HOST_KEY = "elBug.db.host";
    public static final String ELBUG_HTTP_THREADS_KEY = "elBug.http.threads";
    public static final String ELBUG_HTTP_POPULATORS_KEY = "elBug.nb_populators";
    public static final String CACHE_COORDINATION_PROTOCOL = "eclipselink.cache.coordination.protocol";
    public static final String CACHE_COORDINATION_CHANNEL = "eclipselink.cache.coordination.channel";

    public static final String PROPERTIES_PATH = "./src/main/resources/elBug-override.properties";

    private PayaraMicroRuntime payara;
    private String appName;
    private String baseUrl;
    private File domainConfig;

    private static boolean init = false;

    static {
        Driver driver;
        env = new HashMap<>();

        env.put(ELBUG_DB_NAME_KEY, "elBug_dev");
        env.put(ELBUG_DB_HOST_KEY, "localhost");
        env.put(ELBUG_HTTP_THREADS_KEY, "5");
        env.put(ELBUG_HTTP_POPULATORS_KEY, "3");
        env.put(CACHE_COORDINATION_PROTOCOL, "fish.payara.persistence.eclipselink.cache.coordination.HazelcastPublishingTransportManager");

        String clusterName;
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            clusterName = "Hz" + localHost.getHostName() + "Cluster";
        } catch (UnknownHostException ex) {
            clusterName = "HzLocalCluster";
        }

        env.put(CACHE_COORDINATION_CHANNEL, clusterName);
    }

    public ElBugRuntime() {
        // ensure a default constructor exists
    }

    public PayaraMicroRuntime getPayara() {
        return payara;
    }

    public void setPayara(PayaraMicroRuntime payara) {
        this.payara = payara;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public File getDomainConfig() {
        return domainConfig;
    }

    public void setDomainConfig(File domainConfig) {
        this.domainConfig = domainConfig;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String toString() {
        return "ElBugRuntime UP: " + baseUrl;
    }

    /**
     * ENv := Default static env + elBug.properties file + extraEnv
     *
     * @param extraEnv
     *
     * @throws IOException
     */
    public static final void initEnv(Map<String, String> extraEnv) throws IOException {

        try (InputStream is = Files.newInputStream(Path.of(PROPERTIES_PATH))) {
            PropertyResourceBundle properties = new PropertyResourceBundle(is);
            for (String k : properties.keySet()) {
                env.put(k, properties.getString(k));
            }
        }

        if (extraEnv != null) {
            env.putAll(extraEnv);
        }
        env.forEach(System::setProperty);
    }

    public static final ElBugRuntime boot(Boolean resetDb) throws BootstrapException, IOException {
        if (!init) {
            initEnv(null);
        }

        ElBugRuntime wr = new ElBugRuntime();

        String root = "../app/";

        File domainConfig = new File("./src/main/resources/domain.xml");
        File tmpDomainConfig = File.createTempFile("domain", ".xml");

        Files.copy(domainConfig.toPath(), tmpDomainConfig.toPath(), StandardCopyOption.REPLACE_EXISTING);

        String warPath = root + "target/ElBug";

        File theWar = new File(warPath);

        PayaraMicroRuntime bootstrap = PayaraMicro.getInstance()
            .setAlternateDomainXML(tmpDomainConfig)
            .addDeploymentFile(theWar)
            .setHttpAutoBind(true)
            .setSslAutoBind(true)
            .bootStrap();

        String appName = bootstrap.getDeployedApplicationNames().iterator().next();
        Integer httpPort = bootstrap.getLocalDescriptor().getHttpPorts().get(0);
        String appUrl = bootstrap.getLocalDescriptor().getApplicationURLS().get(0).toString();

        logger.error("AppName: {}", appName);
        logger.error("Port: {}", httpPort);
        logger.error("URL: {}", appUrl);

        wr.setAppName(appName);
        wr.setBaseUrl("http://localhost:" + httpPort + "/" + appName);
        wr.setDomainConfig(tmpDomainConfig);
        wr.setPayara(bootstrap);
        return wr;
    }

    public static void main(String... args) throws BootstrapException, IOException {
        ElBugRuntime payara1 = ElBugRuntime.boot(false);

        Runtime.getRuntime()
            .addShutdownHook(new Thread() {
                @Override
                public void run() {
                    logger.info("Shutdown Hook");
                    try {
                        if (payara1 != null) {
                            payara1.getPayara().shutdown();
                        }

                        //if (payara2 != null) {
                        //payara2.getPayara().shutdown();
                        //}
                    } catch (BootstrapException ex) {
                        logger.info("Shutdown failed with {}", ex);
                    }
                }
            });

        logger.info("Running");
    }
}
