package org.synnefo.jclouds;

import com.google.common.base.Optional;
import org.jclouds.Context;
import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.*;
import org.jclouds.domain.Location;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.providers.ProviderMetadata;
import org.jclouds.rest.Utils;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Set;

/**
 * @author Christos KK Loverdos <loverdos@gmail.com>
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ComputeTest {
    private ComputeServiceContext computeContext;
    private ComputeService computeService;
    private ComputeHelpers computeHelpers;

    @Before
    public void setUp() throws Exception {
        this.computeContext = ContextBuilder.newBuilder(Environment.JCloudsProvider).
            endpoint(Environment.SnfEndpoint).
            credentials(Environment.SnfUserUUID, Environment.SnfUserToken).
            modules(Environment.Modules).
            apiVersion(Environment.APIVersion).
            buildView(ComputeServiceContext.class);

        this.computeService = this.computeContext.getComputeService();
        this.computeHelpers = new ComputeHelpers(computeService);
    }

    @After
    public void tearDown() throws Exception {
        this.computeContext.close();
    }

    @Test
    public void test_000_context() {
        final Context context = this.computeContext.unwrap();
        String identity = context.getIdentity();
        String id = context.getId();
        String name = context.getName();
        ProviderMetadata providerMetadata = context.getProviderMetadata();
        Utils utils = context.getUtils();
        String description = context.getDescription();
    }

    @Test
    public void test_001_nodes() {
        final Set<? extends ComputeMetadata> nodes = this.computeService.listNodes();
        for(ComputeMetadata node : nodes) {
            System.out.println("Node: " + node);
        }
    }

    @Test
    public void test_002_images() {
        final Set<? extends Image> images = this.computeService.listImages();
        for(Image image : images) {
            System.out.println("Image:  " + image);
        }
    }

    @Test
    public void test_003_hardware_profiles() {
        // flavors
        final Set<? extends Hardware> hwProfiles = this.computeService.listHardwareProfiles();
        for(Hardware hwProfile : hwProfiles) {
            System.out.println("H/W Profile: " + hwProfile);
        }
    }

    @Test
    public void test_004_assignable_locations() {
        final Set<? extends Location> assignableLocations = this.computeService.listAssignableLocations();
        for(Location assignableLocation : assignableLocations) {
            System.out.println("Assignable Location: " + assignableLocation);
        }
    }

    @Test
    public void test_005_template_builder() {
        final String imageId = this.computeService.listImages().iterator().next().getId();
        System.out.println(" Image ID: " + imageId);
        final TemplateBuilder templateBuilder = this.computeService.templateBuilder();
        final Template template = templateBuilder.imageId(imageId).build();
        System.out.println("  Template: " + template);
        final Image image = template.getImage();
        System.out.println("    Image: " + image);
        final LoginCredentials credentials = image.getDefaultCredentials();

        if(credentials != null) {
            final Optional<String> optionalPassword = credentials.getOptionalPassword();
            final Optional<String> optionalPrivateKey = credentials.getOptionalPrivateKey();

            System.out.println(
                String.format(
                    "      Credentials: user=%s, password.exists=%s, privateKey.exists=%s, shouldAuthenticateSudo=%s",
                    credentials.getUser(),
                    Environment.isPresentSafe(optionalPassword),
                    Environment.isPresentSafe(optionalPrivateKey),
                    credentials.shouldAuthenticateSudo()
                )
            );
        }
    }

    @Test
    public void test_007_node_create_delete() throws RunNodesException, InterruptedException {
        final String imageId = this.computeService.listImages().iterator().next().getId();
        System.out.println("Image ID: " + imageId);
        final TemplateBuilder templateBuilder = this.computeService.templateBuilder();
        final Template template = templateBuilder.imageId(imageId).build();

        System.out.println("Creating a new VM");
        final Set<? extends NodeMetadata> nodes = this.computeService.createNodesInGroup("snf-jclouds-testgen", 1, template);
        final NodeMetadata node = nodes.iterator().next(); // we had just one
        final String nodeID = node.getId();

        System.out.println("Waiting for server to be fully active...");
        computeHelpers.waitNodeStatus(nodeID, NodeMetadata.Status.RUNNING, 5000L, new Proc<NodeMetadata.Status>() {
            @Override
            public void apply(NodeMetadata.Status status) {
                System.out.println("Waiting until " + NodeMetadata.Status.RUNNING + ", status = " + status);
            }
        });


        System.out.println("Destroying node " + nodeID);
        computeHelpers.destroyNodeAndWait(nodeID, 5000L, new Proc<NodeMetadata.Status>() {
            @Override
            public void apply(NodeMetadata.Status status) {
                System.out.println("Waiting until node is destroyed, status = " + status);
            }
        });
    }
}