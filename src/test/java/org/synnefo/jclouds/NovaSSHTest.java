package org.synnefo.jclouds;

import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.Utils;
import org.jclouds.compute.domain.ExecResponse;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.NodeMetadataBuilder;
import org.jclouds.compute.options.RunScriptOptions;
import org.jclouds.domain.Credentials;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.io.Payloads;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.NovaAsyncApi;
import org.jclouds.openstack.nova.v2_0.compute.options.NovaTemplateOptions;
import org.jclouds.openstack.nova.v2_0.domain.Server;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;
import org.jclouds.rest.RestContext;
import org.jclouds.scriptbuilder.domain.Statements;
import org.jclouds.ssh.SshClient;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Christos KK Loverdos <loverdos@gmail.com>
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NovaSSHTest {
    // This test assumes you have a ready server
    @Test
    public void test() {
        Environment.checkAndComplain(Environment.SNF_SERVER_ID, Environment.SnfServerID);
        Environment.checkAndComplain(Environment.SNF_SERVER_ZONE, Environment.SnfServerZone);
        Environment.checkAndComplain(Environment.SNF_SERVER_USERNAME, Environment.SnfServerUsername);
        Environment.checkAndComplain(Environment.SNF_SERVER_PASSWORD, Environment.SnfServerPassword);
        
        ComputeServiceContext context = ContextBuilder.newBuilder(Environment.JCloudsProvider).
            endpoint(Environment.SnfEndpoint).
            credentials(Environment.SnfUserUUID, Environment.SnfUserToken).
            modules(Environment.Modules).
            apiVersion(Environment.APIVersion).
            buildView(ComputeServiceContext.class);

        final String serverId = Environment.SnfServerID;
        final String serverZone = Environment.SnfServerZone;
        final String id = String.format("%s/%s", serverZone, serverId); // This is the ID needed by jClouds API
        final String username = Environment.SnfServerUsername;
        final String password = Environment.SnfServerPassword;


        ComputeService compute = context.getComputeService();
        RestContext<NovaApi, NovaAsyncApi> nova = context.unwrap();
        NovaApi novaApi = nova.getApi();
        ServerApi serverApi = novaApi.getServerApiForZone("default");

        Server server = serverApi.get(serverId);
        System.out.println("server = " + server);
        String ipv4 = server.getAccessIPv4();
        System.out.println("ipv4 = " + ipv4);
        String keyname = server.getKeyName();
        System.out.println("keyname = " + keyname);

        NovaTemplateOptions nto = (NovaTemplateOptions) new NovaTemplateOptions().
            overrideLoginUser(username).
            overrideLoginPassword(password).
            wrapInInitScript(false).
            runAsRoot(false);

        final String script0 = "python -V";
        final String script = "id -a";

        RunScriptOptions rso = nto;
        long t0 = System.currentTimeMillis();
        ExecResponse response = compute.runScriptOnNode(id, Statements.interpret(script), rso);
        long dt = System.currentTimeMillis() - t0;
        double dtf = 0.001 * dt;
        System.out.println("Remote script ran in = " + dtf + " sec");
        final String output = response.getOutput();
        final String error = response.getError();
        final int exitStatus = response.getExitStatus();
        System.out.println("output = " + output);
        System.out.println("error = " + error);
        System.out.println("exitStatus = " + exitStatus);


        NodeMetadata baseNodeMetadata = compute.getNodeMetadata(id);
        NodeMetadata nodeMetadata = NodeMetadataBuilder.fromNodeMetadata(baseNodeMetadata).
            credentials(LoginCredentials.fromCredentials(new Credentials(username, password))).
            build();
        final Utils contextUtils = context.utils();
        SshClient sshClient = contextUtils.sshForNode().apply(nodeMetadata);
        System.out.println("sshClient = " + sshClient);
        try {
            sshClient.connect();
            final String path = "/tmp/normalizer12";
            sshClient.put(path, Payloads.newStringPayload("CKKL has been here!!\n"));
            ExecResponse sshResponse = sshClient.exec("cat " + path);
            System.out.println("sshResponse = " + sshResponse);
        }
        finally {
            if(sshClient != null) {
                sshClient.disconnect();
            }
        }
    }
}
