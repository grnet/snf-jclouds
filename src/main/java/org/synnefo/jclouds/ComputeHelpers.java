package org.synnefo.jclouds;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.NodeMetadata;

import java.util.EnumSet;

/**
 * Helper methods for the JClouds generic ComputeService API.
 *
 * @author Christos KK Loverdos <loverdos@gmail.com>
 */
public class ComputeHelpers {
    private final ComputeService computeService;

    public ComputeHelpers(ComputeService computeService) {
        this.computeService = computeService;
    }

    public NodeMetadata.Status getNodeStatus(String nodeID) {
        return computeService.getNodeMetadata(nodeID).getStatus();
    }

    public NodeMetadata.Status waitNodeStatuses(
        String nodeID,
        EnumSet<NodeMetadata.Status> allowed,
        long callDelayMillis,
        Proc<NodeMetadata.Status> reporter
    ) throws InterruptedException {
        final Proc<NodeMetadata.Status> safeStep = Proc.Helpers.safe(reporter);

        NodeMetadata.Status status = getNodeStatus(nodeID);
        safeStep.apply(status);

        while(!allowed.contains(status)) {
            Thread.sleep(callDelayMillis);
            status = getNodeStatus(nodeID);
            safeStep.apply(status);
        }

        return status;
    }

    public NodeMetadata.Status waitNodeStatus(
        String nodeID,
        NodeMetadata.Status allowed,
        long callDelayMillis,
        Proc<NodeMetadata.Status> reportProc
    ) throws InterruptedException {
        return waitNodeStatuses(
            nodeID,
            EnumSet.of(allowed),
            callDelayMillis,
            reportProc
        );
    }

    public NodeMetadata.Status destroyNodeAndWait(
        String nodeID,
        long callDelayMillis,
        Proc<NodeMetadata.Status> reporter
    ) throws InterruptedException {

        computeService.destroyNode(nodeID);

        return waitNodeStatuses(
            nodeID,
            EnumSet.of(NodeMetadata.Status.TERMINATED, NodeMetadata.Status.UNRECOGNIZED),
            callDelayMillis,
            reporter
        );
    }
}
