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
    private AgentContainer diaContainer = null;
    
    protected void setup() {
        // retrieve diagram container
        Object[] args = getArguments();
        diaContainer = (AgentContainer) args[0];
        
        RestServer.launchServer(this);
    }
    
    // create a new diagram agent
    public void addNewDiagram(String diaName) {
        try {
            Services.getDiagram(this, diaName);
        }
        catch(RuntimeException re) {
            try {
                // create DiaAgt if it does not exists yet
                AgentController agentCc = diaContainer.createNewAgent("DiaAgt-"+diaName, "diaCt.diaAgt.DiaAgt", null);
                agentCc.start();
            }
            catch (Exception e) {
                Errors.throwKO("Unable to create diagram '"+diaName+"'");
            }
        }
    }
    
    // create a new element and sets its propreties
    public void addNewElement(String queryId, List<String> path, String propertyMapSerialized) {
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        
        String diaName = path.get(0);
        //path = path.subList(1, path.size());
        message.addReceiver(Services.getDiagram(this, diaName));
        
        Map<String, String> map = new HashMap<>();
        map.put(Messaging.TYPE, Method.PUT.toString());
        map.put(Messaging.PATH, JSON.serializeStringList(path));
        map.put(Messaging.PROPERTIES, propertyMapSerialized);
        
        message.setContent(JSON.serializeStringMap(map));
        message.setConversationId(queryId);
        this.send(message);
        
        addBehaviour(new ReceiveBhv(this, queryId));
    }
    
    // remove an element or a diagram
    public void rmElement(String queryId, List<String> path) {
        Map<String, String> map = new HashMap<>();
        map.put(Messaging.TYPE, Method.DELETE.toString());
        map.put(Messaging.PATH, JSON.serializeStringList(path));
        
        try {
            ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
            
            String diaName = path.get(0);
            message.addReceiver(Services.getDiagram(this, diaName));
            
            message.setContent(JSON.serializeStringMap(map));
            message.setConversationId(queryId);
            
            this.send(message);
            
            addBehaviour(new ReceiveBhv(this, queryId));
        }
        catch (RuntimeException re) {
            // do not raise an error if the diagram to remove does not exists
            if (path.size() == 1) {
                map.put(Messaging.STATUS, Messaging.OK);
                RestServer.returnQueue.put(queryId, JSON.serializeStringMap(map));
            }
            else {
                throw re;
            }
        }
    }
    
    // rm a list of properties from an element
    public void rmProperties(String queryId, List<String> path, List<String> propertiesList) {
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        
        String diaName = path.get(0);
        //path = path.subList(1, path.size());
        message.addReceiver(Services.getDiagram(this, diaName));
        
        Map<String, String> map = new HashMap<>();
        map.put(Messaging.TYPE, Method.DELETE.toString());
        map.put(Messaging.PATH, JSON.serializeStringList(path));
        map.put(Messaging.PROPERTIES_LIST, JSON.serializeStringList(propertiesList));
        
        message.setContent(JSON.serializeStringMap(map));
        message.setConversationId(queryId);
        this.send(message);
        
        addBehaviour(new ReceiveBhv(this, queryId));
    }
    
    public void chProperties(String queryId, List<String> path, Map<String, String> propertyMap) {
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        
        String diaName = path.get(0);
        //path = path.subList(1, path.size());
        message.addReceiver(Services.getDiagram(this, diaName));
        
        Map<String, String> map = new HashMap<>();
        map.put(Messaging.TYPE, Method.POST.toString());
        map.put(Messaging.PATH, JSON.serializeStringList(path));
        map.put(Messaging.PROPERTIES, JSON.serializeStringMap(propertyMap));
        
        message.setContent(JSON.serializeStringMap(map));
        message.setConversationId(queryId);
        this.send(message);
        
        addBehaviour(new ReceiveBhv(this, queryId));
    }
    
    // retrieve the complete element description
    public void getElementDescription(String queryId, List<String> path) {
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        
        String diaName = path.get(0);
        //path = path.subList(1, path.size());
        message.addReceiver(Services.getDiagram(this, diaName));
        
        Map<String, String> map = new HashMap<>();
        map.put(Messaging.TYPE, Method.GET.toString());
        map.put(Messaging.PATH, JSON.serializeStringList(path));
        //map.put(Messaging.PROPERTIES, propertyMapSerialized);
        
        message.setContent(JSON.serializeStringMap(map));
        message.setConversationId(queryId);
        this.send(message);
        
        addBehaviour(new ReceiveBhv(this, queryId));
    }
}
