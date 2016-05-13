package diaCt.diaAgt;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

import utils.JSON;
import utils.Errors;

public class DiaElt {
    public Map<String, String> propertyMap = new HashMap<String, String>();
    public Map<String, DiaElt> subEltMap = new HashMap<String, DiaElt>();
    
    public DiaElt(Map<String, String> propertyMap) {
        this.propertyMap = propertyMap;
    }
    
    // retrieve a sub element
    public DiaElt retrieveElt(List<String> path) {
        DiaElt curElt = this;
        String pathString = "";
        
        for (int i=0; i<path.size(); i++) {
            pathString+=path.get(i)+"/";
            if (curElt.subEltMap.containsKey(path.get(i))) {
                curElt = curElt.subEltMap.get(path.get(i));
            }
            else {
                Errors.throwKO("Path '"+pathString+"' does not exists");
            }
        }
        return curElt;
    }
    
    // retrieve the complete description of an element recursively
    public String getDescription() {
        Map<String, String> map = new HashMap<>(propertyMap);
        for (String str : subEltMap.keySet()) {
            map.put(str, subEltMap.get(str).getDescription());
        }
        return JSON.serializeStringMap(map);
    }
    
    // recursively restore elements
    public void restoreElements(Map<String, String> description) {
        for (String key : description.keySet()) {
            String jsonStr = description.get(key);
            if (JSON.isJSONObject(jsonStr)) {
                // it's a sub-element --> create it and make a recursive call
                DiaElt newElt = new DiaElt(new HashMap<>());
                newElt.restoreElements(JSON.deserializeStringMap(jsonStr));
                subEltMap.put(key, newElt);
            }
            else {
                // it's a property
                propertyMap.put(key, jsonStr);
            }
        }
    }
}
