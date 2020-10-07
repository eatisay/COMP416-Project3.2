import java.util.Hashtable;

/***
 * This class holds the messages that will shared between the routers.
 * It has four main fields:
 * senderID: Sender ID of the message and receiver(receiverID) IDs.
 * receiverID: Receiver ID of the message and receiver(receiverID) IDs.
 * linkBandwith: Bandwidth of the link that is used.
 * distanceVectorEstimates: It is the essential part of the message. It holds the distance vector of sender.
 * In addition to those fields, their getter and setter functions are also implemented.
 */
public class Message {

    private Hashtable<Integer, Integer> distanceVectorEstimates = new Hashtable<>();
    private Hashtable<Integer, Integer> linkBandwith = new Hashtable<>();
    private int senderID;
    private int receiverID;

    public Message(Hashtable<Integer, Integer> distanceVectorEstimates, Hashtable<Integer, Integer> linkBandwith, int senderID, int receiverID) {
        this.distanceVectorEstimates = distanceVectorEstimates;
        this.linkBandwith = linkBandwith;
        this.senderID = senderID;
        this.receiverID = receiverID;
    }

    public Hashtable<Integer, Integer> getDistanceVectorEstimates() {
        return distanceVectorEstimates;
    }

    public void setDistanceVectorEstimates(Hashtable<Integer, Integer> distanceVectorEstimates) {
        this.distanceVectorEstimates = distanceVectorEstimates;
    }

    public Hashtable<Integer, Integer> getLinkBandwith() {
        return linkBandwith;
    }

    public void setLinkBandwith(Hashtable<Integer, Integer> linkBandwith) {
        this.linkBandwith = linkBandwith;
    }

    public int getSenderID() {
        return senderID;
    }

    public void setSenderID(int senderID) {
        this.senderID = senderID;
    }

    public int getReceiverID() {
        return receiverID;
    }

    public void setReceiverID(int receiverID) {
        this.receiverID = receiverID;
    }
}
