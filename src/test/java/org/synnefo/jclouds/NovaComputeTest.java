package org.synnefo.jclouds;

import com.google.common.collect.FluentIterable;
import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.NovaAsyncApi;
import org.jclouds.openstack.nova.v2_0.domain.Flavor;
import org.jclouds.openstack.nova.v2_0.domain.Image;
import org.jclouds.openstack.nova.v2_0.domain.Server;
import org.jclouds.openstack.nova.v2_0.domain.ServerCreated;
import org.jclouds.openstack.nova.v2_0.features.FlavorApi;
import org.jclouds.openstack.nova.v2_0.features.ImageApi;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;
import org.jclouds.openstack.nova.v2_0.options.CreateServerOptions;
import org.jclouds.openstack.v2_0.domain.Link;
import org.jclouds.openstack.v2_0.domain.Resource;
import org.jclouds.rest.RestContext;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.util.Set;

/**
 * Uses the jClouds OpenStack driver via Nova-specific APIs.
 *
 * @author Christos KK Loverdos <loverdos@gmail.com>
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NovaComputeTest {
    private NovaApi novaApi;
    private FlavorApi flavorApi;
    private ImageApi imageApi;
    private ServerApi serverApi;
    private NovaServerApiHelpers novaServerApiHelpers;

    @Before
    public void setUp() throws Exception {
        ComputeServiceContext context = ContextBuilder.newBuilder(Environment.JCloudsProvider).
            endpoint(Environment.SnfEndpoint).
            credentials(Environment.SnfUserUUID, Environment.SnfUserToken).
            modules(Environment.Modules).
            apiVersion(Environment.APIVersion).
            buildView(ComputeServiceContext.class);

        ComputeService compute = context.getComputeService();
        RestContext<NovaApi, NovaAsyncApi> nova = context.unwrap();
        this.novaApi = nova.getApi();


        // This is jClouds 1.6.0
//        this.novaApi = ContextBuilder.newBuilder(Environment.JCloudsProvider).
//            endpoint(Environment.SnfEndpoint).
//            credentials(Environment.SnfUserUUID, Environment.SnfUserToken).
//            modules(Environment.Modules).
//            apiVersion(Environment.APIVersion).
//            buildApi(NovaApi.class);

        final Set<String> zones = novaApi.getConfiguredZones();
        final String zone = zones.iterator().next();

        Assert.assertEquals("default", zone);

        this.flavorApi = novaApi.getFlavorApiForZone(zone);
        System.out.println("flavorApi = " + flavorApi);
        this.imageApi = novaApi.getImageApiForZone(zone);
        System.out.println("imageApi = " + imageApi);
        this.serverApi = novaApi.getServerApiForZone(zone);
        System.out.println("serverApi = " + serverApi);
        
        this.novaServerApiHelpers = new NovaServerApiHelpers(serverApi);
    }

    @After
    public void tearDown() throws Exception {
        // This is jClouds 1.6.0 API
//        this.novaApi.close();
    }

    @Test
    public void test_001_flavors() {
        final FluentIterable<? extends Resource> flavors = flavorApi.list().concat();
        for(Resource flavor : flavors) {
            System.out.println("flavor = " + flavor);
        }
    }

    @Test
    public void test_002_flavors_in_detail() {
        final FluentIterable<? extends Flavor> flavors = flavorApi.listInDetail().concat();
        for(Flavor flavor : flavors) {
            System.out.println("flavor = " + flavor);
        }
    }

    @Test
    public void test_003_images() {
        final FluentIterable<? extends Resource> images = imageApi.list().concat();
        for(Resource image : images) {
            System.out.println("image = " + image);
        }
    }

    @Test
    public void test_004_images_in_detail() {
        final FluentIterable<? extends Image> images = imageApi.listInDetail().concat();
        for(Image image : images) {
            System.out.println("image = " + image);
        }
    }

    @Test
    public void test_005_servers() {
        final FluentIterable<? extends Resource> servers = serverApi.list().concat();
        for(Resource server : servers) {
            System.out.println("server = " + server);
        }
    }

    @Test
    public void test_006_servers_in_detail() {
        final FluentIterable<? extends Server> servers = serverApi.listInDetail().concat();
        for(Server server : servers) {
            System.out.println("server = " + server);
        }
    }

    @Test
    public void test_007_server_create_delete() throws InterruptedException {
        final String imageRef = imageApi.list().iterator().next().iterator().next().getId();
        final String flavorRef = flavorApi.list().iterator().next().iterator().next().getId();
        final CreateServerOptions defaultOptions = new CreateServerOptions().adminPass("foobar");
        final ServerCreated serverCreated = serverApi.create("foobar", imageRef, flavorRef, defaultOptions);
        final String serverID = serverCreated.getId();
        System.out.println("serverID = " + serverID);
        final String serverName = serverCreated.getName();
        System.out.println("serverName = " + serverName);
        // This is jClouds 1.6.0 API
//        final Optional<String> serverAdminPass = serverCreated.getAdminPass();
        // This is jClouds 1.5.5 API
        final String serverAdminPass = serverCreated.getAdminPass();

        System.out.println("serverAdminPass = " + serverAdminPass);
        final Set<Link> serverLinks = serverCreated.getLinks();
        for(Link serverLink : serverLinks) {
            System.out.println("serverLink = " + serverLink);
        }

        System.out.println("Waiting for server to be fully active...");
        final Proc<Server.Status> waitActiveStep = new Proc<Server.Status>() {
            @Override
            public void apply(Server.Status status) {
                System.out.println("Waiting until " + Server.Status.ACTIVE + ", status = " + status);
            }
        };
        novaServerApiHelpers.waitServerStatus(serverID, Server.Status.ACTIVE, 5000L, waitActiveStep);

        System.out.println("Deleting server " + serverID);
        final Proc<Server.Status> waitDeletedStep = new Proc<Server.Status>() {
            @Override
            public void apply(Server.Status status) {
                System.out.println("Waiting until " + Server.Status.DELETED + ", status = " + status);
            }
        };
        novaServerApiHelpers.deleteServerAndWait(serverID, 5000L, waitDeletedStep);
    }
}
