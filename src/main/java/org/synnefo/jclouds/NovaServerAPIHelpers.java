package org.synnefo.jclouds;

import org.jclouds.openstack.nova.v2_0.domain.Server;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;

import java.util.EnumSet;

/**
 * Helper methods for Nova-specific ServerApi.
 *
 * @author Christos KK Loverdos <loverdos@gmail.com>
 */
public final class NovaServerApiHelpers {
    private final ServerApi serverApi;

    public NovaServerApiHelpers(ServerApi serverApi) {
        this.serverApi = serverApi;
    }

    public Server getServer(String serverID) {
        return serverApi.get(serverID);
    }

    public Server.Status getServerStatus(String serverID) {
        return getServer(serverID).getStatus();
    }

    public Server.Status waitServerStatuses(
        String serverID,
        EnumSet<Server.Status> allowed,
        long callDelayMillis,
        Proc<Server.Status> reportProc
    ) throws InterruptedException {
        final Proc<Server.Status> safeStep = Proc.Helpers.safe(reportProc);

        Server.Status status = getServerStatus(serverID);
        safeStep.apply(status);

        while(!allowed.contains(status)) {
            Thread.sleep(callDelayMillis);
            status = getServerStatus(serverID);
            safeStep.apply(status);
        }

        return status;
    }

    public Server.Status waitServerStatus(
        String serverID,
        Server.Status allowed,
        long callDelayMillis,
        Proc<Server.Status> reportProc
    ) throws InterruptedException {

        return waitServerStatuses(
            serverID,
            EnumSet.of(allowed),
            callDelayMillis,
            reportProc
        );
    }

    public Server.Status deleteServerAndWait(
        String serverID,
        long callDelayMillis,
        Proc<Server.Status> step
    ) throws InterruptedException {

        serverApi.delete(serverID);

        return waitServerStatus(
            serverID,
            Server.Status.DELETED,
            callDelayMillis,
            step
        );
    }
}
