package objects;

import objects.exceptions.MalformedJIDException;

/**
 *
 * Created by prashanth on 26/12/14.
 */
public class JID {

    private String domain;
    private String resource;
    private String bareJID;
    private String fullJID;

    // We populate the JID as soon as the JID object is created
    public JID(String fullJID) throws MalformedJIDException {
        try{
            bareJID = fullJID.split("/")[0];
            resource = fullJID.split("/")[1];
            domain = bareJID.split("@")[1];
            this.fullJID = fullJID;
        }catch (Exception e){
            throw new MalformedJIDException();
        }
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getBareJID() {
        return bareJID;
    }

    public void setBareJID(String bareJID) {
        this.bareJID = bareJID;
    }

    public String getFullJID(){
        return fullJID;
    }

    @Override
    public int hashCode(){
        return fullJID.hashCode();
    }

    @Override
    public boolean equals(Object other){

        if(other == this){
            return true;
        }

        if (!(other instanceof JID)) {
            return false;
        }

        JID otherJID = (JID) other;

        return this.fullJID.equals(otherJID.fullJID);
    }

    @Override
    public String toString(){
        return "<fullJID>" + fullJID + " <resource>" + resource + " <domain>" + domain;
    }
}
