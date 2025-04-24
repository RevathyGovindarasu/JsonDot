package com.rgov.jsondot;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.Collections;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.IOException;

public class JsonDot {
    private JSONObject jsonObject;
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private static final XmlMapper xmlMapper = new XmlMapper();
    private static final String PATH_SPLIT_REGEX = "(?<!\\.)\\.(?!\\[)";

    public JsonDot() {
        this.jsonObject = new JSONObject();
    }

    public JsonDot(String jsonString) throws JSONException {
        this.jsonObject = new JSONObject(jsonString);
    }

    public JsonDot(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    /**
     * Validates if a path is correctly formatted
     * @param path Path to validate
     * @return true if the path is valid, false otherwise
     */
    public static boolean isValidPath(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        
        // Check for consecutive dots
        if (path.contains("..")) {
            return false;
        }
        
        // Split by dots but preserve array indices
        String[] parts = path.split(PATH_SPLIT_REGEX);
        for (String part : parts) {
            if (part.isEmpty()) {
                return false;
            }
            
            // Handle wildcards and array indices
            if (part.equals("*") || (part.contains("[") && part.matches("^\\w+\\[(\\d+|\\*)\\]$"))) {
                continue;
            }
            
            // Validate regular key - allow alphanumeric and underscore
            if (!part.matches("^\\w+$")) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Adds a value at the specified path using dot notation, auto-creating missing paths
     * @param path Dot notation path (e.g., "parent1.parent2.child")
     * @param value Value to add
     * @return this JsonDot instance for method chaining
     * @throws JSONException if the path is invalid
     */
    public JsonDot addElement(String path, Object value) throws JSONException {
        if (!isValidPath(path)) {
            throw new JSONException("Invalid path format: " + path);
        }
        
        // Split by dots but preserve array indices
        String[] parts = path.split(PATH_SPLIT_REGEX);
        Object current = jsonObject;
        
        // Navigate through the path, creating missing objects
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            if (current instanceof JSONObject) {
                JSONObject obj = (JSONObject) current;
                if (part.contains("[") && part.endsWith("]")) {
                    // Handle array access
                    String arrayName = part.substring(0, part.indexOf("["));
                    String indexStr = part.substring(part.indexOf("[") + 1, part.length() - 1);
                    
                    if (!obj.has(arrayName)) {
                        obj.put(arrayName, new JSONArray());
                    }
                    
                    Object arrayValue = obj.get(arrayName);
                    if (arrayValue instanceof JSONArray) {
                        JSONArray array = (JSONArray) arrayValue;
                        if (indexStr.equals("*")) {
                            // Handle wildcard array index
                            if (array.length() > 0) {
                                current = array.get(0);
                            } else {
                                throw new JSONException("Array is empty at path: " + part);
                            }
                        } else {
                            int index = Integer.parseInt(indexStr);
                            // Ensure array is large enough
                            while (array.length() <= index) {
                                array.put(new JSONObject());
                            }
                            current = array.get(index);
                        }
                    } else {
                        throw new JSONException("Expected array at path: " + arrayName);
                    }
                } else {
                    // Handle regular object access
                    if (!obj.has(part)) {
                        obj.put(part, new JSONObject());
                    }
                    current = obj.get(part);
                }
            } else if (current instanceof JSONArray) {
                JSONArray array = (JSONArray) current;
                if (part.contains("[") && part.endsWith("]")) {
                    String indexStr = part.substring(part.indexOf("[") + 1, part.length() - 1);
                    if (indexStr.equals("*")) {
                        // Handle wildcard array index
                        if (array.length() > 0) {
                            current = array.get(0);
                        } else {
                            throw new JSONException("Array is empty at path: " + part);
                        }
                    } else {
                        int index = Integer.parseInt(indexStr);
                        while (array.length() <= index) {
                            array.put(new JSONObject());
                        }
                        current = array.get(index);
                    }
                } else {
                    throw new JSONException("Cannot access non-array element with array notation: " + part);
                }
            } else {
                throw new JSONException("Cannot traverse through non-object/non-array at path: " + part);
            }
        }
        
        // Add the value at the final path
        String lastPart = parts[parts.length - 1];
        if (current instanceof JSONObject) {
            JSONObject obj = (JSONObject) current;
            if (lastPart.contains("[") && lastPart.endsWith("]")) {
                // Handle array access for the last part
                String arrayName = lastPart.substring(0, lastPart.indexOf("["));
                String indexStr = lastPart.substring(lastPart.indexOf("[") + 1, lastPart.length() - 1);
                
                if (!obj.has(arrayName)) {
                    obj.put(arrayName, new JSONArray());
                }
                
                Object arrayValue = obj.get(arrayName);
                if (arrayValue instanceof JSONArray) {
                    JSONArray array = (JSONArray) arrayValue;
                    if (indexStr.equals("*")) {
                        // Add value to all array elements
                        for (int i = 0; i < array.length(); i++) {
                            array.put(i, value);
                        }
                    } else {
                        int index = Integer.parseInt(indexStr);
                        while (array.length() <= index) {
                            array.put(JSONObject.NULL);
                        }
                        array.put(index, value);
                    }
                } else {
                    throw new JSONException("Expected array at path: " + arrayName);
                }
            } else {
                obj.put(lastPart, value);
            }
        } else if (current instanceof JSONArray) {
            JSONArray array = (JSONArray) current;
            if (lastPart.contains("[") && lastPart.endsWith("]")) {
                String indexStr = lastPart.substring(lastPart.indexOf("[") + 1, lastPart.length() - 1);
                if (indexStr.equals("*")) {
                    // Add value to all array elements
                    for (int i = 0; i < array.length(); i++) {
                        array.put(i, value);
                    }
                } else {
                    int index = Integer.parseInt(indexStr);
                    while (array.length() <= index) {
                        array.put(JSONObject.NULL);
                    }
                    array.put(index, value);
                }
            } else {
                throw new JSONException("Cannot access non-array element with array notation: " + lastPart);
            }
        } else {
            throw new JSONException("Cannot add element to non-object/non-array at path: " + lastPart);
        }
        
        return this;
    }

    /**
     * Removes an element at all matching paths using dot notation
     * @param path Dot notation path (e.g., "*.child" or "parent1.parent2.child")
     * @return List of removed values
     * @throws JSONException if the path is invalid
     */
    public List<Object> removeElementFromAllPaths(String path) throws JSONException {
        if (!isValidPath(path)) {
            throw new JSONException("Invalid path format: " + path);
        }
        
        String[] parts = path.split(PATH_SPLIT_REGEX);
        List<Object> targetObjects = new ArrayList<>();
        targetObjects.add(jsonObject);
        List<Object> removedValues = new ArrayList<>();
        
        // Navigate through the path, collecting all matching objects
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            List<Object> nextLevel = new ArrayList<>();
            
            for (Object current : targetObjects) {
                if (current instanceof JSONObject) {
                    JSONObject obj = (JSONObject) current;
                    if (part.equals("*")) {
                        // Handle wildcard - add all objects at this level
                        for (String key : obj.keySet()) {
                            Object val = obj.get(key);
                            if (val instanceof JSONObject || val instanceof JSONArray) {
                                nextLevel.add(val);
                            }
                        }
                    } else if (part.contains("[") && part.endsWith("]")) {
                        // Handle array access
                        String arrayName = part.substring(0, part.indexOf("["));
                        String indexStr = part.substring(part.indexOf("[") + 1, part.length() - 1);
                        
                        if (obj.has(arrayName)) {
                            Object arrayValue = obj.get(arrayName);
                            if (arrayValue instanceof JSONArray) {
                                JSONArray array = (JSONArray) arrayValue;
                                if (indexStr.equals("*")) {
                                    // Add all array elements
                                    for (int j = 0; j < array.length(); j++) {
                                        nextLevel.add(array.get(j));
                                    }
                                } else {
                                    int index = Integer.parseInt(indexStr);
                                    if (index < array.length()) {
                                        nextLevel.add(array.get(index));
                                    }
                                }
                            }
                        }
                    } else {
                        // Handle regular object access
                        if (obj.has(part)) {
                            Object val = obj.get(part);
                            if (val instanceof JSONObject || val instanceof JSONArray) {
                                nextLevel.add(val);
                            }
                        }
                    }
                } else if (current instanceof JSONArray) {
                    JSONArray array = (JSONArray) current;
                    if (part.equals("*")) {
                        // Handle wildcard - add all array elements
                        for (int j = 0; j < array.length(); j++) {
                            Object val = array.get(j);
                            if (val instanceof JSONObject || val instanceof JSONArray) {
                                nextLevel.add(val);
                            }
                        }
                    } else if (part.contains("[") && part.endsWith("]")) {
                        // Handle array access
                        String indexStr = part.substring(part.indexOf("[") + 1, part.length() - 1);
                        if (indexStr.equals("*")) {
                            // Add all array elements
                            for (int j = 0; j < array.length(); j++) {
                                nextLevel.add(array.get(j));
                            }
                        } else {
                            int index = Integer.parseInt(indexStr);
                            if (index < array.length()) {
                                nextLevel.add(array.get(index));
                            }
                        }
                    } else {
                        // Handle regular object access
                        for (int j = 0; j < array.length(); j++) {
                            Object val = array.get(j);
                            if (val instanceof JSONObject) {
                                JSONObject obj = (JSONObject) val;
                                if (obj.has(part)) {
                                    Object nestedVal = obj.get(part);
                                    if (nestedVal instanceof JSONObject || nestedVal instanceof JSONArray) {
                                        nextLevel.add(nestedVal);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            targetObjects = nextLevel;
            if (targetObjects.isEmpty()) {
                return removedValues; // No matching paths found
            }
        }
        
        // Remove the value from all matching objects
        String lastPart = parts[parts.length - 1];
        for (Object target : targetObjects) {
            if (target instanceof JSONObject) {
                JSONObject obj = (JSONObject) target;
                if (lastPart.contains("[") && lastPart.endsWith("]")) {
                    // Handle array access for the last part
                    String arrayName = lastPart.substring(0, lastPart.indexOf("["));
                    String indexStr = lastPart.substring(lastPart.indexOf("[") + 1, lastPart.length() - 1);
                    
                    if (obj.has(arrayName)) {
                        Object arrayValue = obj.get(arrayName);
                        if (arrayValue instanceof JSONArray) {
                            JSONArray array = (JSONArray) arrayValue;
                            if (indexStr.equals("*")) {
                                // Remove all array elements
                                for (int i = 0; i < array.length(); i++) {
                                    removedValues.add(array.remove(i));
                                }
                            } else {
                                int index = Integer.parseInt(indexStr);
                                if (index < array.length()) {
                                    removedValues.add(array.remove(index));
                                }
                            }
                        }
                    }
                } else {
                    if (obj.has(lastPart)) {
                        removedValues.add(obj.remove(lastPart));
                    }
                }
            } else if (target instanceof JSONArray) {
                JSONArray array = (JSONArray) target;
                if (lastPart.contains("[") && lastPart.endsWith("]")) {
                    String indexStr = lastPart.substring(lastPart.indexOf("[") + 1, lastPart.length() - 1);
                    if (indexStr.equals("*")) {
                        // Remove all array elements
                        for (int i = 0; i < array.length(); i++) {
                            removedValues.add(array.remove(i));
                        }
                    } else {
                        int index = Integer.parseInt(indexStr);
                        if (index < array.length()) {
                            removedValues.add(array.remove(index));
                        }
                    }
                } else {
                    // Handle regular object access for array elements
                    for (int i = 0; i < array.length(); i++) {
                        Object element = array.get(i);
                        if (element instanceof JSONObject) {
                            JSONObject obj = (JSONObject) element;
                            if (obj.has(lastPart)) {
                                removedValues.add(obj.remove(lastPart));
                            }
                        }
                    }
                }
            }
        }
        
        return removedValues;
    }

    /**
     * Removes an element at the specified path using dot notation
     * @param path Dot notation path (e.g., "parent1.parent2.child")
     * @return The removed value, or null if not found
     * @throws JSONException if the path is invalid
     */
    public Object removeElementFromPath(String path) throws JSONException {
        if (!isValidPath(path)) {
            throw new JSONException("Invalid path format: " + path);
        }

        // Split by dots but preserve array indices
        String[] parts = path.split(PATH_SPLIT_REGEX);
        Object current = jsonObject;
        Object result = null;
        
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            
            if (current instanceof JSONObject) {
                JSONObject obj = (JSONObject) current;
                if (part.contains("[") && part.endsWith("]")) {
                    // Handle array access
                    String arrayName = part.substring(0, part.indexOf("["));
                    String indexStr = part.substring(part.indexOf("[") + 1, part.length() - 1);
                    
                    if (!obj.has(arrayName)) {
                        throw new JSONException("Path not found: " + path);
                    }
                    
                    Object arrayValue = obj.get(arrayName);
                    if (arrayValue instanceof JSONArray) {
                        JSONArray array = (JSONArray) arrayValue;
                        if (indexStr.equals("*")) {
                            throw new JSONException("Cannot use wildcard in path for element removal");
                        }
                        int index = Integer.parseInt(indexStr);
                        if (index >= array.length()) {
                            throw new JSONException("Array index out of bounds: " + path);
                        }
                        current = array.get(index);
                    } else {
                        throw new JSONException("Expected array at path: " + arrayName);
                    }
                } else {
                    if (!obj.has(part)) {
                        throw new JSONException("Path not found: " + path);
                    }
                    current = obj.get(part);
                }
            } else if (current instanceof JSONArray) {
                JSONArray array = (JSONArray) current;
                if (part.contains("[") && part.endsWith("]")) {
                    String indexStr = part.substring(part.indexOf("[") + 1, part.length() - 1);
                    if (indexStr.equals("*")) {
                        throw new JSONException("Cannot use wildcard in path for element removal");
                    }
                    int index = Integer.parseInt(indexStr);
                    if (index >= array.length()) {
                        throw new JSONException("Array index out of bounds: " + path);
                    }
                    current = array.get(index);
                } else {
                    throw new JSONException("Cannot access non-array element with array notation: " + part);
                }
            } else {
                throw new JSONException("Cannot traverse through non-object/non-array at path: " + part);
            }
        }
        
        String lastPart = parts[parts.length - 1];
        if (current instanceof JSONObject) {
            JSONObject obj = (JSONObject) current;
            if (lastPart.contains("[") && lastPart.endsWith("]")) {
                // Handle array access for the last part
                String arrayName = lastPart.substring(0, lastPart.indexOf("["));
                String indexStr = lastPart.substring(lastPart.indexOf("[") + 1, lastPart.length() - 1);
                
                if (!obj.has(arrayName)) {
                    throw new JSONException("Path not found: " + path);
                }
                
                Object arrayValue = obj.get(arrayName);
                if (arrayValue instanceof JSONArray) {
                    JSONArray array = (JSONArray) arrayValue;
                    if (indexStr.equals("*")) {
                        throw new JSONException("Cannot use wildcard in path for element removal");
                    }
                    int index = Integer.parseInt(indexStr);
                    if (index >= array.length()) {
                        throw new JSONException("Array index out of bounds: " + path);
                    }
                    result = array.remove(index);
                } else {
                    throw new JSONException("Expected array at path: " + arrayName);
                }
            } else {
                if (!obj.has(lastPart)) {
                    throw new JSONException("Path not found: " + path);
                }
                result = obj.remove(lastPart);
            }
        } else if (current instanceof JSONArray) {
            JSONArray array = (JSONArray) current;
            if (lastPart.contains("[") && lastPart.endsWith("]")) {
                String indexStr = lastPart.substring(lastPart.indexOf("[") + 1, lastPart.length() - 1);
                if (indexStr.equals("*")) {
                    throw new JSONException("Cannot use wildcard in path for element removal");
                }
                int index = Integer.parseInt(indexStr);
                if (index >= array.length()) {
                    throw new JSONException("Array index out of bounds: " + path);
                }
                result = array.remove(index);
            } else {
                throw new JSONException("Cannot access non-array element with array notation: " + lastPart);
            }
        } else {
            throw new JSONException("Cannot remove element from non-object/non-array at path: " + lastPart);
        }

        return result;
    }

    /**
     * Gets a value at the specified path using dot notation
     * @param path Dot notation path (e.g., "parent1.parent2.child")
     * @return The value at the specified path
     * @throws JSONException if the path is invalid
     */
    public Object getElement(String path) throws JSONException {
        if (!isValidPath(path)) {
            throw new JSONException("Invalid path format: " + path);
        }

        // Split by dots but preserve array indices
        String[] parts = path.split(PATH_SPLIT_REGEX);
        Object current = jsonObject;
        
        for (String part : parts) {
            if (current == null) {
                throw new JSONException("Path not found: " + path);
            }
            
            if (part.equals("*")) {
                if (current instanceof JSONArray) {
                    JSONArray array = (JSONArray) current;
                    if (array.length() > 0) {
                        current = array.get(0);
                    } else {
                        throw new JSONException("Path not found: " + path);
                    }
                } else if (current instanceof JSONObject) {
                    JSONObject obj = (JSONObject) current;
                    if (obj.length() > 0) {
                        current = obj.get(obj.keys().next());
                    } else {
                        throw new JSONException("Path not found: " + path);
                    }
                } else {
                    throw new JSONException("Path not found: " + path);
                }
            } else if (part.contains("[")) {
                // Handle array access
                String[] arrayParts = part.split("\\[");
                String key = arrayParts[0];
                int index = Integer.parseInt(arrayParts[1].replace("]", ""));
                
                if (current instanceof JSONObject) {
                    JSONObject obj = (JSONObject) current;
                    if (!obj.has(key)) {
                        throw new JSONException("Path not found: " + path);
                    }
                    Object value = obj.get(key);
                    if (value instanceof JSONArray) {
                        JSONArray array = (JSONArray) value;
                        if (index >= array.length()) {
                            throw new JSONException("Array index out of bounds: " + path);
                        }
                        current = array.get(index);
                    } else {
                        throw new JSONException("Path not found: " + path);
                    }
                } else {
                    throw new JSONException("Path not found: " + path);
                }
            } else {
                // Handle regular object access
                if (current instanceof JSONObject) {
                    JSONObject obj = (JSONObject) current;
                    if (!obj.has(part)) {
                        throw new JSONException("Path not found: " + path);
                    }
                    current = obj.get(part);
                } else if (current instanceof JSONArray) {
                    // Handle direct array access
                    JSONArray array = (JSONArray) current;
                    if (array.length() > 0) {
                        current = array.get(0);
                    } else {
                        throw new JSONException("Path not found: " + path);
                    }
                } else {
                    throw new JSONException("Path not found: " + path);
                }
            }
        }
        
        return current;
    }

    /**
     * Gets a JSON object at the specified path using dot notation
     * @param path Dot notation path (e.g., "parent1.parent2.child")
     * @return JsonDot instance containing the JSON object
     * @throws JSONException if the path is invalid or element is not a JSON object
     */
    public JsonDot getObject(String path) throws JSONException {
        if (!isValidPath(path)) {
            throw new JSONException("Invalid path format: " + path);
        }
        
        Object value = getElement(path);
        if (value == null) {
            throw new JSONException("Path not found: " + path);
        }
        
        if (value instanceof JSONObject) {
            return new JsonDot((JSONObject) value);
        } else if (value instanceof JSONArray) {
            // Convert array to object with numeric keys
            JSONObject obj = new JSONObject();
            JSONArray array = (JSONArray) value;
            for (int i = 0; i < array.length(); i++) {
                obj.put(String.valueOf(i), array.get(i));
            }
            return new JsonDot(obj);
        } else {
            throw new JSONException("Value at path '" + path + "' is not a JSON object");
        }
    }

    /**
     * Gets a JSON array at the specified path using dot notation
     * @param path Dot notation path (e.g., "parent1.parent2.child")
     * @return JsonArrayDot instance containing the JSON array
     * @throws JSONException if the path is invalid or element is not a JSON array
     */
    public JsonArrayDot getArray(String path) throws JSONException {
        if (!isValidPath(path)) {
            throw new JSONException("Invalid path format: " + path);
        }

        // Split by dots but preserve array indices
        String[] parts = path.split(PATH_SPLIT_REGEX);
        Object current = jsonObject;
        
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            
            if (current instanceof JSONObject) {
                JSONObject obj = (JSONObject) current;
                if (part.contains("[") && part.endsWith("]")) {
                    // Handle array access
                    String arrayName = part.substring(0, part.indexOf("["));
                    String indexStr = part.substring(part.indexOf("[") + 1, part.length() - 1);
                    
                    if (!obj.has(arrayName)) {
                        return null;
                    }
                    
                    Object arrayValue = obj.get(arrayName);
                    if (arrayValue instanceof JSONArray) {
                        JSONArray array = (JSONArray) arrayValue;
                        if (indexStr.equals("*")) {
                            throw new JSONException("Cannot use wildcard in array access");
                        }
                        int index = Integer.parseInt(indexStr);
                        if (index >= array.length()) {
                            return null;
                        }
                        current = array.get(index);
                    } else {
                        return null;
                    }
                } else {
                    if (!obj.has(part)) {
                        return null;
                    }
                    current = obj.get(part);
                }
            } else if (current instanceof JSONArray) {
                JSONArray array = (JSONArray) current;
                if (part.contains("[") && part.endsWith("]")) {
                    String indexStr = part.substring(part.indexOf("[") + 1, part.length() - 1);
                    if (indexStr.equals("*")) {
                        throw new JSONException("Cannot use wildcard in array access");
                    }
                    int index = Integer.parseInt(indexStr);
                    if (index >= array.length()) {
                        return null;
                    }
                    current = array.get(index);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
        
        String lastPart = parts[parts.length - 1];
        if (current instanceof JSONObject) {
            JSONObject obj = (JSONObject) current;
            if (lastPart.contains("[") && lastPart.endsWith("]")) {
                // Handle array access for the last part
                String arrayName = lastPart.substring(0, lastPart.indexOf("["));
                String indexStr = lastPart.substring(lastPart.indexOf("[") + 1, lastPart.length() - 1);
                
                if (!obj.has(arrayName)) {
                    return null;
                }
                
                Object arrayValue = obj.get(arrayName);
                if (arrayValue instanceof JSONArray) {
                    JSONArray array = (JSONArray) arrayValue;
                    if (indexStr.equals("*")) {
                        throw new JSONException("Cannot use wildcard in array access");
                    }
                    int index = Integer.parseInt(indexStr);
                    if (index >= array.length()) {
                        return null;
                    }
                    return new JsonArrayDot(array);
                } else {
                    return null;
                }
            } else {
                if (!obj.has(lastPart)) {
                    return null;
                }
                Object value = obj.get(lastPart);
                if (value instanceof JSONArray) {
                    return new JsonArrayDot((JSONArray) value);
                }
                return null;
            }
        } else if (current instanceof JSONArray) {
            JSONArray array = (JSONArray) current;
            if (lastPart.contains("[") && lastPart.endsWith("]")) {
                String indexStr = lastPart.substring(lastPart.indexOf("[") + 1, lastPart.length() - 1);
                if (indexStr.equals("*")) {
                    throw new JSONException("Cannot use wildcard in array access");
                }
                int index = Integer.parseInt(indexStr);
                if (index >= array.length()) {
                    return null;
                }
                Object value = array.get(index);
                if (value instanceof JSONArray) {
                    return new JsonArrayDot((JSONArray) value);
                }
                return null;
            } else {
                return null;
            }
        }
        
        return null;
    }

    /**
     * Checks if a path exists in the JSON object.
     * The path can include array indices like "parent1.array[0].child" or wildcards like "parent1.array[*].child".
     * 
     * @param path The path to check
     * @return true if the path exists, false otherwise
     */
    public boolean hasPath(String path) {
        try {
            // Split by dots but preserve array indices
            String[] parts = path.split(PATH_SPLIT_REGEX);
            Object current = jsonObject;

            for (String part : parts) {
                if (current == null) {
                    return false;
                }

                if (part.contains("[") && part.endsWith("]")) {
                    // Handle array access
                    String arrayName = part.substring(0, part.indexOf("["));
                    String indexStr = part.substring(part.indexOf("[") + 1, part.length() - 1);
                    
                    if (current instanceof JSONObject) {
                        JSONObject obj = (JSONObject) current;
                        if (!obj.has(arrayName)) {
                            return false;
                        }
                        Object arrayValue = obj.get(arrayName);
                        if (!(arrayValue instanceof JSONArray)) {
                            return false;
                        }
                        JSONArray array = (JSONArray) arrayValue;
                        
                        if (indexStr.equals("*")) {
                            // Wildcard means we just need to check if the array exists and is not empty
                            if (array.length() == 0) {
                                return false;
                            }
                            current = array.get(0); // Use first element for further path traversal
                        } else {
                            try {
                                int index = Integer.parseInt(indexStr);
                                if (index < 0 || index >= array.length()) {
                                    return false;
                                }
                                current = array.get(index);
                            } catch (NumberFormatException e) {
                                return false;
                            }
                        }
                    } else if (current instanceof JSONArray) {
                        JSONArray array = (JSONArray) current;
                        try {
                            int index = Integer.parseInt(indexStr);
                            if (index < 0 || index >= array.length()) {
                                return false;
                            }
                            current = array.get(index);
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                } else {
                    // Handle regular object access
                    if (current instanceof JSONObject) {
                        JSONObject obj = (JSONObject) current;
                        if (!obj.has(part)) {
                            return false;
                        }
                        current = obj.get(part);
                    } else if (current instanceof JSONArray) {
                        // If we're accessing a property on an array, check if any element has it
                        JSONArray array = (JSONArray) current;
                        boolean found = false;
                        for (int i = 0; i < array.length(); i++) {
                            Object item = array.get(i);
                            if (item instanceof JSONObject && ((JSONObject) item).has(part)) {
                                current = ((JSONObject) item).get(part);
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    /**
     * Gets all keys at the root level
     * @return Array of keys
     */
    public String[] getKeys() {
        return JSONObject.getNames(jsonObject);
    }

    /**
     * Gets the underlying JSONObject
     * @return The JSONObject instance
     */
    public JSONObject getJSONObject() {
        return jsonObject;
    }

    /**
     * Converts the JSON object to a string
     * @return String representation of the JSON object
     */
    @Override
    public String toString() {
        return jsonObject.toString();
    }

    /**
     * Converts the JSON object to a pretty-printed string
     * @return Pretty-printed string representation of the JSON object
     */
    public String toPrettyString() {
        return jsonObject.toString(2);
    }

    /**
     * Gets a string value at the specified path
     * @param path Dot notation path
     * @return String value, or null if not found or not a string
     * @throws JSONException if the path is invalid
     */
    public String getString(String path) throws JSONException {
        Object value = getElement(path);
        if (value == null) {
            throw new JSONException("Path not found: " + path);
        }
        return value.toString();
    }

    /**
     * Gets an integer value at the specified path
     * @param path Dot notation path
     * @return Integer value, or null if not found or not a number
     * @throws JSONException if the path is invalid
     */
    public Integer getInt(String path) throws JSONException {
        Object value = getElement(path);
        if (value == null) {
            throw new JSONException("Path not found: " + path);
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                throw new JSONException("Value at path '" + path + "' is not a valid integer");
            }
        }
        throw new JSONException("Value at path '" + path + "' is not a number");
    }

    /**
     * Gets a double value at the specified path
     * @param path Dot notation path
     * @return Double value, or null if not found or not a number
     * @throws JSONException if the path is invalid
     */
    public Double getDouble(String path) throws JSONException {
        Object value = getElement(path);
        if (value == null) {
            throw new JSONException("Path not found: " + path);
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                throw new JSONException("Value at path '" + path + "' is not a valid double");
            }
        }
        throw new JSONException("Value at path '" + path + "' is not a number");
    }

    /**
     * Gets a boolean value at the specified path
     * @param path Dot notation path
     * @return Boolean value, or null if not found or not a boolean
     * @throws JSONException if the path is invalid
     */
    public Boolean getBoolean(String path) throws JSONException {
        Object value = getElement(path);
        if (value == null) {
            throw new JSONException("Path not found: " + path);
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            String strValue = ((String) value).toLowerCase();
            if ("true".equals(strValue)) {
                return true;
            } else if ("false".equals(strValue)) {
                return false;
            }
        }
        throw new JSONException("Value at path '" + path + "' is not a boolean");
    }

    /**
     * Suggests possible path completions based on a partial path.
     * 
     * @param partialPath The partial path to get suggestions for. If null or empty,
     *                    returns all top-level keys.
     * @return A List of suggested complete paths that match the partial path.
     */
    public List<String> suggestPaths(String partialPath) {
        List<String> suggestions = new ArrayList<>();
        
        // Handle null or empty path
        if (partialPath == null || partialPath.isEmpty()) {
            if (jsonObject != null) {
                suggestions.addAll(jsonObject.keySet());
            }
            return suggestions;
        }
        
        String[] parts = partialPath.split("\\.");
        JSONObject current = jsonObject;
        StringBuilder currentPath = new StringBuilder();
        
        // Navigate through all but the last segment
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            if (current.has(part)) {
                Object value = current.get(part);
                if (value instanceof JSONObject) {
                    current = (JSONObject) value;
                    currentPath.append(currentPath.length() > 0 ? "." : "").append(part);
                } else {
                    return suggestions; // Path doesn't exist
                }
            } else {
                return suggestions; // Path doesn't exist
            }
        }
        
        // Get suggestions for the last segment
        String lastPart = parts[parts.length - 1];
        String prefix = currentPath.length() > 0 ? currentPath.toString() + "." : "";
        
        for (String key : current.keySet()) {
            if (key.startsWith(lastPart)) {
                suggestions.add(prefix + key);
            }
        }
        
        return suggestions;
    }

    /**
     * Queries JSON using wildcard patterns
     * @param pattern The wildcard pattern (e.g., "user.*.city")
     * @return List of matching values
     */
    public List<Object> query(String pattern) {
        List<Object> results = new ArrayList<>();
        if (pattern == null || pattern.isEmpty()) {
            return results;
        }

        String[] segments = pattern.split("\\.");
        queryRecursive(jsonObject, segments, 0, results);
        return results;
    }

    private void queryRecursive(Object current, String[] segments, int index, List<Object> results) {
        if (current == null || index >= segments.length) {
            return;
        }

        String segment = segments[index];
        boolean isLastSegment = index == segments.length - 1;

        if (segment.equals("*")) {
            // Handle wildcard
            if (current instanceof JSONObject) {
                JSONObject obj = (JSONObject) current;
                for (String key : obj.keySet()) {
                    Object value = obj.get(key);
                    if (isLastSegment) {
                        results.add(value);
                    } else {
                        queryRecursive(value, segments, index + 1, results);
                    }
                }
            } else if (current instanceof JSONArray) {
                JSONArray array = (JSONArray) current;
                for (int i = 0; i < array.length(); i++) {
                    Object item = array.get(i);
                    if (isLastSegment) {
                        results.add(item);
                    } else {
                        queryRecursive(item, segments, index + 1, results);
                    }
                }
            }
        } else {
            // Handle specific key
            if (current instanceof JSONObject) {
                JSONObject obj = (JSONObject) current;
                if (obj.has(segment)) {
                    Object value = obj.get(segment);
                    if (isLastSegment) {
                        results.add(value);
                    } else {
                        queryRecursive(value, segments, index + 1, results);
                    }
                }
            } else if (current instanceof JSONArray) {
                JSONArray array = (JSONArray) current;
                for (int i = 0; i < array.length(); i++) {
                    Object item = array.get(i);
                    if (item instanceof JSONObject) {
                        JSONObject obj = (JSONObject) item;
                        if (obj.has(segment)) {
                            Object value = obj.get(segment);
                            if (isLastSegment) {
                                results.add(value);
                            } else {
                                queryRecursive(value, segments, index + 1, results);
                            }
                        }
                    } else if (item instanceof JSONArray) {
                        // Recursively search nested arrays
                        queryRecursive(item, segments, index, results);
                    }
                }
            }
        }
    }

    /**
     * Filters an array based on a condition
     * @param array The array to filter
     * @param condition The condition to apply
     * @return A new JsonArrayDot containing the filtered results
     */
    private JsonArrayDot filterArray(JSONArray array, Predicate<Object> condition) {
        JSONArray filtered = new JSONArray();
        for (int i = 0; i < array.length(); i++) {
            try {
                Object element = array.get(i);
                if (condition.test(element)) {
                    filtered.put(element);
                }
            } catch (JSONException e) {
                // Skip elements that can't be accessed
            }
        }
        return new JsonArrayDot(filtered);
    }

    /**
     * Filters an array at the given path based on a condition
     * @param path Path to the array
     * @param condition Predicate to filter array elements
     * @return Filtered JsonArrayDot instance
     */
    public JsonArrayDot filterArray(String path, Predicate<Object> condition) throws JSONException {
        try {
            Object value = getElement(path);
            if (value instanceof JSONArray) {
                return filterArray((JSONArray) value, condition);
            }
            return new JsonArrayDot(new JSONArray());
        } catch (Exception e) {
            return new JsonArrayDot(new JSONArray());
        }
    }

    /**
     * Converts the JSON object to XML string
     * @return XML string representation of the JSON object
     * @throws JSONException if conversion fails
     */
    public String toXml() throws JSONException {
        try {
            // Convert JSONObject to Map
            Object json = jsonMapper.readValue(jsonObject.toString(), Object.class);
            // Convert to XML
            return xmlMapper.writeValueAsString(json);
        } catch (IOException e) {
            throw new JSONException("Failed to convert JSON to XML: " + e.getMessage());
        }
    }

    /**
     * Converts the JSON object to pretty-printed XML string
     * @return Pretty-printed XML string representation of the JSON object
     * @throws JSONException if conversion fails
     */
    public String toPrettyXml() throws JSONException {
        try {
            // Convert JSONObject to Map
            Object json = jsonMapper.readValue(jsonObject.toString(), Object.class);
            // Convert to pretty-printed XML
            return xmlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (IOException e) {
            throw new JSONException("Failed to convert JSON to XML: " + e.getMessage());
        }
    }

    /**
     * Converts a specific path in the JSON object to XML
     * @param path The path to convert
     * @return XML string representation of the specified path
     * @throws JSONException if conversion fails or path is invalid
     */
    public String pathToXml(String path) throws JSONException {
        try {
            Object value = getElement(path);
            // Convert the value to XML
            return xmlMapper.writeValueAsString(value);
        } catch (IOException e) {
            throw new JSONException("Failed to convert path to XML: " + e.getMessage());
        }
    }

    /**
     * Adds a value to all elements in an array at the specified path
     * @param path The path to the array (e.g., "user.contacts")
     * @param key The key to add to each array element
     * @param value The value to add
     * @return this JsonDot instance for method chaining
     * @throws JSONException if the path is invalid or does not point to an array
     */
    public JsonDot addElementToArray(String path, String key, Object value) throws JSONException {
        Object target = getElement(path);
        if (!(target instanceof JSONArray)) {
            throw new JSONException("Path does not point to an array: " + path);
        }
        
        JSONArray array = (JSONArray) target;
        for (int i = 0; i < array.length(); i++) {
            Object element = array.get(i);
            if (element instanceof JSONObject) {
                JSONObject obj = (JSONObject) element;
                obj.put(key, value);
            }
        }
        
        return this;
    }

    /**
     * Adds a value at all matching paths using dot notation, auto-creating missing paths
     * @param path Dot notation path (e.g., "*.child" or "parent1.parent2.child")
     * @param value Value to add
     * @return this JsonDot instance for method chaining
     * @throws JSONException if the path is invalid
     */
    public JsonDot addElementToAllPaths(String path, Object value) throws JSONException {
        if (!isValidPath(path)) {
            throw new JSONException("Invalid path format: " + path);
        }
        
        // Split by dots but preserve array indices
        String[] parts = path.split(PATH_SPLIT_REGEX);
        List<Object> targetObjects = new ArrayList<>();
        targetObjects.add(jsonObject);
        
        // Navigate through the path, collecting all matching objects
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            List<Object> nextLevel = new ArrayList<>();
            
            for (Object current : targetObjects) {
                if (current instanceof JSONObject) {
                    JSONObject obj = (JSONObject) current;
                    if (part.contains("[") && part.endsWith("]")) {
                        // Handle array access
                        String arrayName = part.substring(0, part.indexOf("["));
                        String indexStr = part.substring(part.indexOf("[") + 1, part.length() - 1);
                        
                        if (!obj.has(arrayName)) {
                            continue;
                        }
                        
                        Object arrayValue = obj.get(arrayName);
                        if (arrayValue instanceof JSONArray) {
                            JSONArray array = (JSONArray) arrayValue;
                            if (indexStr.equals("*")) {
                                // Add all array elements
                                for (int j = 0; j < array.length(); j++) {
                                    Object element = array.get(j);
                                    if (element instanceof JSONObject) {
                                        nextLevel.add(element);
                                    }
                                }
                            } else {
                                int index = Integer.parseInt(indexStr);
                                if (index < array.length()) {
                                    Object element = array.get(index);
                                    if (element instanceof JSONObject) {
                                        nextLevel.add(element);
                                    }
                                }
                            }
                        }
                    } else {
                        if (obj.has(part)) {
                            Object val = obj.get(part);
                            if (val instanceof JSONObject) {
                                nextLevel.add(val);
                            }
                        }
                    }
                }
            }
            
            targetObjects = nextLevel;
            if (targetObjects.isEmpty()) {
                return this; // No matching paths found
            }
        }
        
        // Add the value to all matching objects
        String lastPart = parts[parts.length - 1];
        for (Object target : targetObjects) {
            if (target instanceof JSONObject) {
                JSONObject obj = (JSONObject) target;
                obj.put(lastPart, value);
            }
        }
        
        return this;
    }

    /**
     * Finds all matching paths for a given pattern
     * @param path The path pattern to match (e.g., "*.tags")
     * @return List of matching paths
     * @throws JSONException if the path is invalid
     */
    public List<String> findMatchingPaths(String path) throws JSONException {
        if (!isValidPath(path)) {
            throw new JSONException("Invalid path format: " + path);
        }

        String[] parts = path.split(PATH_SPLIT_REGEX);
        List<String> matchingPaths = new ArrayList<>();
        List<String> currentPath = new ArrayList<>();
        findMatchingPathsRecursive(jsonObject, parts, 0, currentPath, matchingPaths);
        return matchingPaths;
    }

    private void findMatchingPathsRecursive(Object current, String[] parts, int index, List<String> currentPath, List<String> matchingPaths) throws JSONException {
        if (index >= parts.length) {
            if (!currentPath.isEmpty()) {
                if (current instanceof JSONArray) {
                    // For arrays, add each element as a separate path
                    JSONArray array = (JSONArray) current;
                    String basePath = String.join(".", currentPath);
                    for (int i = 0; i < array.length(); i++) {
                        matchingPaths.add(basePath + "[" + i + "]");
                    }
                } else {
                    matchingPaths.add(String.join(".", currentPath));
                }
            }
            return;
        }

        String part = parts[index];
        boolean isLastSegment = index == parts.length - 1;

        if (part.equals("*")) {
            // Handle wildcard - look at all objects at current level
            if (current instanceof JSONObject) {
                JSONObject obj = (JSONObject) current;
                // Sort keys to ensure consistent ordering
                List<String> keys = new ArrayList<>(obj.keySet());
                Collections.sort(keys);
                for (String key : keys) {
                    Object value = obj.get(key);
                    if (isLastSegment) {
                        // If this is the last segment and it matches the target key, add it
                        if (key.equals(parts[parts.length - 1])) {
                            currentPath.add(key);
                            if (value instanceof JSONArray) {
                                // For arrays, add each element as a separate path
                                JSONArray array = (JSONArray) value;
                                String basePath = String.join(".", currentPath);
                                for (int i = 0; i < array.length(); i++) {
                                    matchingPaths.add(basePath + "[" + i + "]");
                                }
                            } else {
                                matchingPaths.add(String.join(".", currentPath));
                            }
                            currentPath.remove(currentPath.size() - 1);
                        }
                    } else {
                        // Continue searching in this object
                        currentPath.add(key);
                        findMatchingPathsRecursive(value, parts, index + 1, currentPath, matchingPaths);
                        currentPath.remove(currentPath.size() - 1);
                    }
                }
            } else if (current instanceof JSONArray) {
                JSONArray array = (JSONArray) current;
                for (int i = 0; i < array.length(); i++) {
                    Object item = array.get(i);
                    if (item instanceof JSONObject) {
                        JSONObject obj = (JSONObject) item;
                        // Sort keys to ensure consistent ordering
                        List<String> keys = new ArrayList<>(obj.keySet());
                        Collections.sort(keys);
                        for (String key : keys) {
                            Object value = obj.get(key);
                            if (isLastSegment) {
                                // If this is the last segment and it matches the target key, add it
                                if (key.equals(parts[parts.length - 1])) {
                                    currentPath.add(key);
                                    if (value instanceof JSONArray) {
                                        // For arrays, add each element as a separate path
                                        JSONArray valueArray = (JSONArray) value;
                                        String basePath = String.join(".", currentPath);
                                        for (int j = 0; j < valueArray.length(); j++) {
                                            matchingPaths.add(basePath + "[" + j + "]");
                                        }
                                    } else {
                                        matchingPaths.add(String.join(".", currentPath));
                                    }
                                    currentPath.remove(currentPath.size() - 1);
                                }
                            } else {
                                // Continue searching in this object
                                currentPath.add(key);
                                findMatchingPathsRecursive(value, parts, index + 1, currentPath, matchingPaths);
                                currentPath.remove(currentPath.size() - 1);
                            }
                        }
                    }
                }
            }
        } else if (part.contains("[") && part.endsWith("]")) {
            // Handle array access
            String arrayName = part.substring(0, part.indexOf("["));
            String indexStr = part.substring(part.indexOf("[") + 1, part.length() - 1);
            
            if (current instanceof JSONObject) {
                JSONObject obj = (JSONObject) current;
                if (obj.has(arrayName)) {
                    Object arrayValue = obj.get(arrayName);
                    if (arrayValue instanceof JSONArray) {
                        JSONArray array = (JSONArray) arrayValue;
                        if (indexStr.equals("*")) {
                            // Handle wildcard array index
                            for (int i = 0; i < array.length(); i++) {
                                Object item = array.get(i);
                                currentPath.add(arrayName + "[" + i + "]");
                                if (isLastSegment) {
                                    if (item instanceof JSONArray) {
                                        // For arrays, add each element as a separate path
                                        JSONArray itemArray = (JSONArray) item;
                                        String basePath = String.join(".", currentPath);
                                        for (int j = 0; j < itemArray.length(); j++) {
                                            matchingPaths.add(basePath + "[" + j + "]");
                                        }
                                    } else {
                                        matchingPaths.add(String.join(".", currentPath));
                                    }
                                } else {
                                    findMatchingPathsRecursive(item, parts, index + 1, currentPath, matchingPaths);
                                }
                                currentPath.remove(currentPath.size() - 1);
                            }
                        } else {
                            // Handle specific array index
                            int arrayIndex = Integer.parseInt(indexStr);
                            if (arrayIndex < array.length()) {
                                Object item = array.get(arrayIndex);
                                currentPath.add(part);
                                if (isLastSegment) {
                                    if (item instanceof JSONArray) {
                                        // For arrays, add each element as a separate path
                                        JSONArray itemArray = (JSONArray) item;
                                        String basePath = String.join(".", currentPath);
                                        for (int i = 0; i < itemArray.length(); i++) {
                                            matchingPaths.add(basePath + "[" + i + "]");
                                        }
                                    } else {
                                        matchingPaths.add(String.join(".", currentPath));
                                    }
                                } else {
                                    findMatchingPathsRecursive(item, parts, index + 1, currentPath, matchingPaths);
                                }
                                currentPath.remove(currentPath.size() - 1);
                            }
                        }
                    }
                }
            }
        } else {
            // Handle regular object access
            if (current instanceof JSONObject) {
                JSONObject obj = (JSONObject) current;
                if (obj.has(part)) {
                    Object value = obj.get(part);
                    currentPath.add(part);
                    if (isLastSegment) {
                        if (value instanceof JSONArray) {
                            // For arrays, add each element as a separate path
                            JSONArray array = (JSONArray) value;
                            String basePath = String.join(".", currentPath);
                            for (int i = 0; i < array.length(); i++) {
                                matchingPaths.add(basePath + "[" + i + "]");
                            }
                        } else {
                            matchingPaths.add(String.join(".", currentPath));
                        }
                    } else {
                        findMatchingPathsRecursive(value, parts, index + 1, currentPath, matchingPaths);
                    }
                    currentPath.remove(currentPath.size() - 1);
                }
            } else if (current instanceof JSONArray) {
                JSONArray array = (JSONArray) current;
                for (int i = 0; i < array.length(); i++) {
                    Object item = array.get(i);
                    if (item instanceof JSONObject) {
                        JSONObject obj = (JSONObject) item;
                        if (obj.has(part)) {
                            Object value = obj.get(part);
                            currentPath.add(part);
                            if (isLastSegment) {
                                if (value instanceof JSONArray) {
                                    // For arrays, add each element as a separate path
                                    JSONArray valueArray = (JSONArray) value;
                                    String basePath = String.join(".", currentPath);
                                    for (int j = 0; j < valueArray.length(); j++) {
                                        matchingPaths.add(basePath + "[" + j + "]");
                                    }
                                } else {
                                    matchingPaths.add(String.join(".", currentPath));
                                }
                            } else {
                                findMatchingPathsRecursive(value, parts, index + 1, currentPath, matchingPaths);
                            }
                            currentPath.remove(currentPath.size() - 1);
                        }
                    }
                }
            }
        }
    }

    public JsonDot addElementAtPath(String path, Object value) throws JSONException {
        if (!isValidPath(path)) {
            throw new JSONException("Invalid path format: " + path);
        }

        // Split by dots but preserve array indices
        String[] parts = path.split(PATH_SPLIT_REGEX);
        Object current = jsonObject;

        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            
            if (current instanceof JSONObject) {
                JSONObject obj = (JSONObject) current;
                if (part.contains("[") && part.endsWith("]")) {
                    // Handle array access
                    String arrayName = part.substring(0, part.indexOf("["));
                    int index = Integer.parseInt(part.substring(part.indexOf("[") + 1, part.length() - 1));
                    
                    if (!obj.has(arrayName)) {
                        obj.put(arrayName, new JSONArray());
                    }
                    
                    Object arrayValue = obj.get(arrayName);
                    if (arrayValue instanceof JSONArray) {
                        JSONArray array = (JSONArray) arrayValue;
                        while (array.length() <= index) {
                            array.put(new JSONObject());
                        }
                        current = array.get(index);
                    } else {
                        throw new JSONException("Expected array at path: " + arrayName);
                    }
                } else {
                    // Handle regular object access
                    if (!obj.has(part)) {
                        obj.put(part, new JSONObject());
                    }
                    current = obj.get(part);
                }
            } else if (current instanceof JSONArray) {
                JSONArray array = (JSONArray) current;
                if (part.contains("[") && part.endsWith("]")) {
                    // Handle array access
                    int index = Integer.parseInt(part.substring(part.indexOf("[") + 1, part.length() - 1));
                    while (array.length() <= index) {
                        array.put(new JSONObject());
                    }
                    current = array.get(index);
                } else {
                    throw new JSONException("Cannot access non-array element with array notation: " + part);
                }
            } else {
                throw new JSONException("Cannot traverse through non-object/non-array at path: " + part);
            }
        }

        String lastPart = parts[parts.length - 1];
        if (current instanceof JSONObject) {
            JSONObject obj = (JSONObject) current;
            if (lastPart.contains("[") && lastPart.endsWith("]")) {
                // Handle array access for the last part
                String arrayName = lastPart.substring(0, lastPart.indexOf("["));
                int index = Integer.parseInt(lastPart.substring(lastPart.indexOf("[") + 1, lastPart.length() - 1));
                
                if (!obj.has(arrayName)) {
                    obj.put(arrayName, new JSONArray());
                }
                
                Object arrayValue = obj.get(arrayName);
                if (arrayValue instanceof JSONArray) {
                    JSONArray array = (JSONArray) arrayValue;
                    while (array.length() <= index) {
                        array.put(JSONObject.NULL);
                    }
                    array.put(index, value);
                } else {
                    throw new JSONException("Expected array at path: " + arrayName);
                }
            } else {
                obj.put(lastPart, value);
            }
        } else if (current instanceof JSONArray) {
            JSONArray array = (JSONArray) current;
            if (lastPart.contains("[") && lastPart.endsWith("]")) {
                int index = Integer.parseInt(lastPart.substring(lastPart.indexOf("[") + 1, lastPart.length() - 1));
                while (array.length() <= index) {
                    array.put(JSONObject.NULL);
                }
                array.put(index, value);
            } else {
                throw new JSONException("Cannot access non-array element with array notation: " + lastPart);
            }
        } else {
            throw new JSONException("Cannot add element to non-object/non-array at path: " + lastPart);
        }

        return this;
    }
} 