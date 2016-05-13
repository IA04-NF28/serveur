package mainCt.restAgt;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.restlet.data.Method;

import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

import utils.JSON;
import utils.Services;
import utils.Messaging;
import utils.Errors;
import mainCt.restAgt.RestServer;

public class RestAgt extends Agent {
    public AgentContainer diaContainer = null;
    
    protected void setup() {
        // retrieve diagram container
        Object[] args = getArguments();
        diaContainer = (AgentContainer) args[0];
        
        RestServer.launchServer(this);
    }
    
    // create a new element and sets its propreties
    public void addNewElement(String queryId, List<String> path, String propertyMapSerialized) {
        ACLMessage msg = Messaging.addNewElement(this, null, path, propertyMapSerialized);
        msg.setConversationId(queryId);
        this.send(msg);
        addBehaviour(new ReceiveBhv(this, queryId));
    }
    
    // remove an element or a diagram
    public void rmElement(String queryId, List<String> path) {
        try {
            ACLMessage msg = Messaging.rmElement(this, null, path);
            msg.setConversationId(queryId);
            this.send(msg);
            addBehaviour(new ReceiveBhv(this, queryId));
        }
        catch (RuntimeException re) {
            // do not raise an error if the diagram to remove does not exists
            if (path.size() == 1) {
                Map<String, String> map = new HashMap<>();
                map.put(Messaging.STATUS, Messaging.OK);
                map.put(Messaging.TYPE, Method.DELETE.toString());
                map.put(Messaging.PATH, JSON.serializeStringList(path));
                RestServer.returnQueue.put(queryId, JSON.serializeStringMap(map));
            }
            else {
                throw re;
            }
        }
    }
    
    // rm a list of properties from an element
    public void rmProperties(String queryId, List<String> path, List<String> propertiesList) {
        ACLMessage msg = Messaging.rmProperties(this, null, path, propertiesList);
        msg.setConversationId(queryId);
        this.send(msg);
        addBehaviour(new ReceiveBhv(this, queryId));
    }
    
    // change/add properties of an element
    public void chProperties(String queryId, List<String> path, Map<String, String> propertyMap) {
        ACLMessage msg = Messaging.chProperties(this, null, path, propertyMap);
        msg.setConversationId(queryId);
        this.send(msg);
        addBehaviour(new ReceiveBhv(this, queryId));
    }
    
    // retrieve the complete element description
    public void getElementDescription(String queryId, List<String> path) {
        ACLMessage msg = Messaging.getElementDescription(this, null, path);
        msg.setConversationId(queryId);
        this.send(msg);
        addBehaviour(new ReceiveBhv(this, queryId));
    }
}
