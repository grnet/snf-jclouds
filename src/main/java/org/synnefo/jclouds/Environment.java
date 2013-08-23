package org.synnefo.jclouds;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.sshj.config.SshjSshClientModule;

/**
 * @author Christos KK Loverdos <loverdos@gmail.com>
 */
public final class Environment {
    public static final String SNF_END_POINT = "SNF_END_POINT";
    public static final String SNF_USER_UUID = "SNF_USER_UUID";
    public static final String SNF_USER_TOKEN = "SNF_USER_TOKEN";

    public static final String SnfEndpoint = getEnvOrProp(SNF_END_POINT);
    public static final String SnfUserUUID = getEnvOrProp(SNF_USER_UUID);
    public static final String SnfUserToken = getEnvOrProp(SNF_USER_TOKEN);

    public static final String SNF_SERVER_ID = "SNF_SERVER_ID";
    public static final String SNF_SERVER_ZONE = "SNF_SERVER_ZONE";
    public static final String SNF_SERVER_USERNAME = "SNF_SERVER_USERNAME";
    public static final String SNF_SERVER_PASSWORD = "SNF_SERVER_PASSWORD";

    // The server id without a zone.
    // Note that some jClouds APIs assume a format "zone/id".
    public static final String SnfServerID = getEnvOrProp(SNF_SERVER_ID);
    public static final String SnfServerZone = getDefaultEnvOrProp(SNF_SERVER_ZONE, "default");
    public static final String SnfServerUsername = getEnvOrProp(SNF_SERVER_USERNAME);
    public static final String SnfServerPassword = getEnvOrProp(SNF_SERVER_PASSWORD);

    static {
        // These are absolutely essential for all tests.
        checkAndComplain(SNF_END_POINT, SnfEndpoint);
        checkAndComplain(SNF_USER_UUID, SnfUserUUID);
        checkAndComplain(SNF_USER_TOKEN, SnfUserToken);
    }

    public static final String API_VERSION = "API_VERSION";
    public static final String JCLOUDS_PROVIDER = "JCLOUDS_PROVIDER";

    public static final String JCloudsProvider = getDefaultEnvOrProp(JCLOUDS_PROVIDER, "openstack-nova");
    public static final String APIVersion = getDefaultEnvOrProp(API_VERSION, "v2.0");



    public static final Iterable<Module> Modules = ImmutableSet.<Module> of(
        new SLF4JLoggingModule(),
        new SshjSshClientModule()
    );

    public static void checkAndComplain(String name, String value) {
        if(value == null) {
            System.err.println("Please set " + name);
        }
    }

    public static String getEnvOrProp(String name) {
        final String env = System.getenv(name);

        return env != null ? env : System.getProperty(name);
    }

    public static String getDefaultEnvOrProp(String name, String def) {
        String value = getEnvOrProp(name);
        return value != null ? value : def;
    }

    public static <T> boolean isPresentSafe(Optional<T> optional) {
        return optional != null && optional.isPresent();
    }
}
