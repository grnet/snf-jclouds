package org.synnefo.jclouds;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;

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

    static {
        checkAndComplain(SNF_END_POINT, SnfEndpoint);
        checkAndComplain(SNF_USER_UUID, SnfUserUUID);
        checkAndComplain(SNF_USER_TOKEN, SnfUserToken);
    }

    public static final String API_VERSION = "API_VERSION";
    public static final String JCLOUDS_PROVIDER = "JCLOUDS_PROVIDER";

    public static final String JCloudsProvider = getDefaultEnvOrProp(JCLOUDS_PROVIDER, "openstack-nova");
    public static final String APIVersion = getDefaultEnvOrProp(API_VERSION, "v2.0");



    public static final Iterable<Module> Modules = ImmutableSet.<Module> of(
        new SLF4JLoggingModule()/*,
        new SshjSshClientModule()*/
    );

//    public static final ComputeServiceContext Context = ContextBuilder.newBuilder(JCloudsProvider).
//        endpoint(SnfEndpoint).
//        credentials(SnfUserUUID, SnfUserToken).
//        modules(Modules).
//        apiVersion(APIVersion).
//        buildView(ComputeServiceContext.class);

    private static void checkAndComplain(String name, String value) {
        if(value == null) {
            System.err.println("Please set " + name);
        }
    }

    public static String getEnvOrProp(String name) {
        final String env = System.getenv(name);
        if(env != null) {
            return env;
        }

        final String prop = System.getProperty(name);

        return prop;
    }

    public static String getDefaultEnvOrProp(String name, String def) {
        String value = getEnvOrProp(name);
        if(value != null) { return value; }
        return def;
    }

    public static <T> boolean isPresentSafe(Optional<T> optional) {
        return optional != null && optional.isPresent();
    }
}
