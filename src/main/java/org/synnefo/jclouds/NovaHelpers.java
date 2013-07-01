package org.synnefo.jclouds;

import org.jclouds.openstack.nova.v2_0.domain.Server;
import org.jclouds.openstack.nova.v2_0.features.FlavorApi;
import org.jclouds.openstack.nova.v2_0.features.ImageApi;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;

import java.util.EnumSet;

/**
 * @author Christos KK Loverdos <loverdos@gmail.com>
 */
public final class NovaHelpers {
    private final FlavorApi flavorApi;
    private final ImageApi imageApi;
    private final ServerApi serverApi;

    public NovaHelpers(FlavorApi flavorApi, ImageApi imageApi, ServerApi serverApi) {
        this.flavorApi = flavorApi;
        this.imageApi = imageApi;
        this.serverApi = serverApi;
    }

    public Server getServer(String serverID) {
        return serverApi.get(serverID);
    }

    public Server.Status waitServerStatuses(
        String serverID,
        EnumSet<Server.Status> allowed,
        long callDelayMillis,
        Proc<Server.Status> step
    ) throws InterruptedException {
        final Proc<Server.Status> safeStep = Proc.Helpers.safe(step);

        Server.Status status = getServer(serverID).getStatus();
        safeStep.apply(status);

        while(!allowed.contains(status)) {
            Thread.sleep(callDelayMillis);
            status = getServer(serverID).getStatus();
            safeStep.apply(status);
        }

        return status;
    }

    public Server.Status waitServerStatus(
        String serverID,
        Server.Status allowed,
        long callDelayMillis,
        Proc<Server.Status> step
    ) throws InterruptedException {

        return waitServerStatuses(
            serverID,
            EnumSet.of(allowed),
            callDelayMillis,
            step
        );
    }
}
