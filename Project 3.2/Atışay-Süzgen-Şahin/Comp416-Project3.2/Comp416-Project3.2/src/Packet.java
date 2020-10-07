import java.util.ArrayList;

/***
 * This class symbols the packet that will be transmitted between the routers.
 * It has three main fields:
 * packetName: Name of the flow of the packet.
 * flowInfo: Arraylist, consists of source and destination IDs, packet size and the bottleneck bandwidth
 * flowRoute: The route that is followed in the flow of the packet.
 * In addition to those fields, their getter and setter functions are implemented.
 */
public class Packet {
    private String packetName;

    private int[] flowInfo;
    private ArrayList<Integer> flowRoute;
    public Packet(String packetName, int[] flowInfo, ArrayList<Integer> flowRoute) {
        this.packetName = packetName;
        this.flowInfo = flowInfo;
        this.flowRoute = flowRoute;
    }

    public void setPacketName(String packetName) {
        this.packetName = packetName;
    }

    public void setFlowInfo(int[] flowInfo) {
        this.flowInfo = flowInfo;
    }

    public void appendFlowRoute(Integer node) {
        this.flowRoute.add(node);
    }

    public String getPacketName() {
        return packetName;
    }

    public int[] getFlowInfo() {
        return flowInfo;
    }

    public ArrayList<Integer> getFlowRoute() {
        return flowRoute;
    }
}
