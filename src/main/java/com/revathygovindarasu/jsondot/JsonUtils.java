package com.revathygovindarasu.jsondot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class JsonUtils {
    /**
     * Reads a JSON file and returns a JsonDot instance
     * @param filePath Path to the JSON file
     * @return JsonDot instance containing the JSON data
     * @throws IOException if there's an error reading the file
     * @throws JSONException if the file contains invalid JSON
     */
    public static JsonDot readJsonFile(String filePath) throws IOException, JSONException {
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        return new JsonDot(content);
    }

    /**
     * Writes a JsonDot instance to a file
     * @param jsonDot JsonDot instance to write
     * @param filePath Path where to write the file
     * @param prettyPrint Whether to pretty print the JSON
     * @throws IOException if there's an error writing the file
     */
    public static void writeJsonFile(JsonDot jsonDot, String filePath, boolean prettyPrint) throws IOException {
        try (FileWriter file = new FileWriter(filePath)) {
            file.write(prettyPrint ? jsonDot.toPrettyString() : jsonDot.toString());
        }
    }

    /**
     * Merges two JSON objects
     * @param json1 First JSON object
     * @param json2 Second JSON object
     * @return Merged JsonDot instance
     * @throws JSONException if there's an error merging the JSON objects
     */
    public static JsonDot merge(JsonDot json1, JsonDot json2) throws JSONException {
        JSONObject merged = new JSONObject(json1.toString());
        JSONObject second = new JSONObject(json2.toString());
        
        for (String key : second.keySet()) {
            if (merged.has(key)) {
                if (merged.get(key) instanceof JSONObject && second.get(key) instanceof JSONObject) {
                    merged.put(key, merge(new JsonDot(merged.getJSONObject(key)), 
                                       new JsonDot(second.getJSONObject(key))).getJSONObject());
                } else if (merged.get(key) instanceof JSONArray && second.get(key) instanceof JSONArray) {
                    JSONArray array1 = merged.getJSONArray(key);
                    JSONArray array2 = second.getJSONArray(key);
                    for (int i = 0; i < array2.length(); i++) {
                        array1.put(array2.get(i));
                    }
                } else {
                    merged.put(key, second.get(key));
                }
            } else {
                merged.put(key, second.get(key));
            }
        }
        
        return new JsonDot(merged);
    }

    /**
     * Deep merges two JSON objects
     * @param json1 First JSON object
     * @param json2 Second JSON object
     * @return Deep merged JsonDot instance
     * @throws JSONException if there's an error merging the JSON objects
     */
    public static JsonDot deepMerge(JsonDot json1, JsonDot json2) throws JSONException {
        JSONObject merged = new JSONObject(json1.toString());
        JSONObject second = new JSONObject(json2.toString());
        
        for (String key : second.keySet()) {
            if (merged.has(key)) {
                if (merged.get(key) instanceof JSONObject && second.get(key) instanceof JSONObject) {
                    // Recursively merge nested objects
                    merged.put(key, deepMerge(
                        new JsonDot(merged.getJSONObject(key)), 
                        new JsonDot(second.getJSONObject(key))
                    ).getJSONObject());
                } else if (merged.get(key) instanceof JSONArray && second.get(key) instanceof JSONArray) {
                    // Merge arrays by appending elements
                    JSONArray array1 = merged.getJSONArray(key);
                    JSONArray array2 = second.getJSONArray(key);
                    for (int i = 0; i < array2.length(); i++) {
                        array1.put(array2.get(i));
                    }
                } else {
                    // For non-object, non-array values, second object takes precedence
                    merged.put(key, second.get(key));
                }
            } else {
                // Key doesn't exist in first object, add it
                merged.put(key, second.get(key));
            }
        }
        
        return new JsonDot(merged);
    }

    /**
     * Deep merges two JSON objects with custom array merge strategy
     * @param json1 First JSON object
     * @param json2 Second JSON object
     * @param arrayMergeStrategy Strategy for merging arrays (APPEND, OVERWRITE, or MERGE)
     * @return Deep merged JsonDot instance
     * @throws JSONException if there's an error merging the JSON objects
     */
    public static JsonDot deepMerge(JsonDot json1, JsonDot json2, ArrayMergeStrategy arrayMergeStrategy) throws JSONException {
        JSONObject merged = new JSONObject(json1.toString());
        JSONObject source = new JSONObject(json2.toString());
        
        for (String key : source.keySet()) {
            Object sourceValue = source.get(key);
            if (!merged.has(key)) {
                merged.put(key, sourceValue);
            } else {
                Object targetValue = merged.get(key);
                if (sourceValue instanceof JSONObject && targetValue instanceof JSONObject) {
                    merged.put(key, deepMergeObjects((JSONObject) targetValue, (JSONObject) sourceValue, arrayMergeStrategy));
                } else if (sourceValue instanceof JSONArray && targetValue instanceof JSONArray) {
                    merged.put(key, mergeArrays((JSONArray) targetValue, (JSONArray) sourceValue, arrayMergeStrategy));
                } else {
                    merged.put(key, sourceValue);
                }
            }
        }
        
        return new JsonDot(merged);
    }

    private static JSONObject deepMergeObjects(JSONObject target, JSONObject source, ArrayMergeStrategy arrayMergeStrategy) throws JSONException {
        JSONObject merged = new JSONObject(target.toString());
        
        for (String key : source.keySet()) {
            Object sourceValue = source.get(key);
            if (!merged.has(key)) {
                merged.put(key, sourceValue);
            } else {
                Object targetValue = merged.get(key);
                if (sourceValue instanceof JSONObject && targetValue instanceof JSONObject) {
                    merged.put(key, deepMergeObjects((JSONObject) targetValue, (JSONObject) sourceValue, arrayMergeStrategy));
                } else if (sourceValue instanceof JSONArray && targetValue instanceof JSONArray) {
                    merged.put(key, mergeArrays((JSONArray) targetValue, (JSONArray) sourceValue, arrayMergeStrategy));
                } else {
                    merged.put(key, sourceValue);
                }
            }
        }
        
        return merged;
    }

    private static JSONArray mergeArrays(JSONArray target, JSONArray source, ArrayMergeStrategy arrayMergeStrategy) throws JSONException {
        JSONArray merged = new JSONArray();
        
        switch (arrayMergeStrategy) {
            case APPEND:
                // Append all elements from both arrays
                for (int i = 0; i < target.length(); i++) {
                    merged.put(target.get(i));
                }
                for (int i = 0; i < source.length(); i++) {
                    merged.put(source.get(i));
                }
                break;
                
            case OVERWRITE:
                // Use only the source array
                for (int i = 0; i < source.length(); i++) {
                    merged.put(source.get(i));
                }
                break;
                
            case MERGE:
                // Merge arrays based on matching IDs or other unique identifiers
                for (int i = 0; i < target.length(); i++) {
                    Object targetItem = target.get(i);
                    boolean found = false;
                    
                    for (int j = 0; j < source.length(); j++) {
                        Object sourceItem = source.get(j);
                        if (targetItem instanceof JSONObject && sourceItem instanceof JSONObject) {
                            JSONObject targetObj = (JSONObject) targetItem;
                            JSONObject sourceObj = (JSONObject) sourceItem;
                            
                            if (targetObj.has("id") && sourceObj.has("id") && 
                                targetObj.get("id").equals(sourceObj.get("id"))) {
                                merged.put(deepMergeObjects(targetObj, sourceObj, arrayMergeStrategy));
                                found = true;
                                break;
                            }
                        }
                    }
                    
                    if (!found) {
                        merged.put(targetItem);
                    }
                }
                
                // Add any remaining source items that weren't merged
                for (int i = 0; i < source.length(); i++) {
                    Object sourceItem = source.get(i);
                    boolean found = false;
                    
                    for (int j = 0; j < merged.length(); j++) {
                        Object mergedItem = merged.get(j);
                        if (sourceItem instanceof JSONObject && mergedItem instanceof JSONObject) {
                            JSONObject sourceObj = (JSONObject) sourceItem;
                            JSONObject mergedObj = (JSONObject) mergedItem;
                            
                            if (sourceObj.has("id") && mergedObj.has("id") && 
                                sourceObj.get("id").equals(mergedObj.get("id"))) {
                                found = true;
                                break;
                            }
                        }
                    }
                    
                    if (!found) {
                        merged.put(sourceItem);
                    }
                }
                break;
        }
        
        return merged;
    }

    /**
     * Validates if a string is a valid JSON
     * @param jsonString String to validate
     * @return true if the string is valid JSON, false otherwise
     */
    public static boolean isValidJson(String jsonString) {
        try {
            new JSONObject(jsonString);
            return true;
        } catch (JSONException e) {
            try {
                new JSONArray(jsonString);
                return true;
            } catch (JSONException ex) {
                return false;
            }
        }
    }

    /**
     * Converts a JSON object to a JSON array
     * @param jsonDot JsonDot instance to convert
     * @return JsonArrayDot instance containing the converted data
     * @throws JSONException if there's an error converting the JSON
     */
    public static JsonArrayDot toArray(JsonDot jsonDot) throws JSONException {
        JSONArray array = new JSONArray();
        array.put(jsonDot.getJSONObject());
        return new JsonArrayDot(array);
    }

    /**
     * Gets all keys from a JSON object
     * @param jsonDot JsonDot instance
     * @return Array of keys
     */
    public static String[] getKeys(JsonDot jsonDot) {
        return JSONObject.getNames(jsonDot.getJSONObject());
    }

    /**
     * Checks if a JSON object has a specific key
     * @param jsonDot JsonDot instance
     * @param key Key to check
     * @return true if the key exists, false otherwise
     */
    public static boolean hasKey(JsonDot jsonDot, String key) {
        return jsonDot.getJSONObject().has(key);
    }

    /**
     * Converts a JsonDot instance to XML string
     * @param jsonDot JsonDot instance to convert
     * @return XML string representation
     * @throws JsonProcessingException if conversion fails
     */
    public static String toXml(JsonDot jsonDot) throws JsonProcessingException {
        ObjectMapper jsonMapper = new ObjectMapper();
        XmlMapper xmlMapper = new XmlMapper();
        Object jsonObj = jsonMapper.readValue(jsonDot.toString(), Object.class);
        return xmlMapper.writeValueAsString(jsonObj);
    }

    /**
     * Converts XML string to JsonDot instance
     * @param xmlString XML string to convert
     * @return JsonDot instance
     * @throws JsonProcessingException if conversion fails
     */
    public static JsonDot fromXml(String xmlString) throws JsonProcessingException {
        XmlMapper xmlMapper = new XmlMapper();
        ObjectMapper jsonMapper = new ObjectMapper();
        Object xmlObj = xmlMapper.readValue(xmlString, Object.class);
        return new JsonDot(jsonMapper.writeValueAsString(xmlObj));
    }

    /**
     * Converts a JsonDot instance to XML and writes it to a file
     * @param jsonDot JsonDot instance to convert
     * @param filePath Path where to write the XML file
     * @throws IOException if there's an error writing the file
     * @throws JsonProcessingException if conversion fails
     */
    public static void writeXmlFile(JsonDot jsonDot, String filePath) throws IOException, JsonProcessingException {
        try (FileWriter file = new FileWriter(filePath)) {
            file.write(toXml(jsonDot));
        }
    }

    /**
     * Reads an XML file and converts it to JsonDot instance
     * @param filePath Path to the XML file
     * @return JsonDot instance
     * @throws IOException if there's an error reading the file
     * @throws JsonProcessingException if conversion fails
     */
    public static JsonDot readXmlFile(String filePath) throws IOException, JsonProcessingException {
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        return fromXml(content);
    }

    /**
     * Enum defining strategies for merging arrays
     */
    public enum ArrayMergeStrategy {
        /**
         * Append all elements from the second array to the first array
         */
        APPEND,
        
        /**
         * Replace the first array with the second array
         */
        OVERWRITE,
        
        /**
         * Merge arrays by matching indices and recursively merging objects
         */
        MERGE
    }

    /**
     * Represents a difference between two JSON objects
     */
    public static class JsonDiff {
        private final String path;
        private final Object oldValue;
        private final Object newValue;
        private final DiffType type;

        public JsonDiff(String path, Object oldValue, Object newValue, DiffType type) {
            this.path = path;
            this.oldValue = oldValue;
            this.newValue = newValue;
            this.type = type;
        }

        public String getPath() { return path; }
        public Object getOldValue() { return oldValue; }
        public Object getNewValue() { return newValue; }
        public DiffType getType() { return type; }

        @Override
        public String toString() {
            switch (type) {
                case ADDED:
                    return String.format("Added: %s = %s", path, newValue);
                case REMOVED:
                    return String.format("Removed: %s = %s", path, oldValue);
                case MODIFIED:
                    return String.format("Modified: %s: %s -> %s", path, oldValue, newValue);
                default:
                    return "";
            }
        }
    }

    /**
     * Type of difference between JSON objects
     */
    public enum DiffType {
        ADDED, REMOVED, MODIFIED
    }

    /**
     * Compares two JSON objects and returns their differences
     * @param json1 First JSON object
     * @param json2 Second JSON object
     * @return List of differences
     */
    public static List<JsonDiff> diff(JsonDot json1, JsonDot json2) {
        List<JsonDiff> differences = new ArrayList<>();
        diffRecursive("", json1.getJSONObject(), json2.getJSONObject(), differences);
        return differences;
    }

    private static void diffRecursive(String path, JSONObject obj1, JSONObject obj2, List<JsonDiff> differences) {
        // Find added and modified fields
        for (String key : obj2.keySet()) {
            String currentPath = path.isEmpty() ? key : path + "." + key;
            if (!obj1.has(key)) {
                differences.add(new JsonDiff(currentPath, null, obj2.get(key), DiffType.ADDED));
            } else {
                Object value1 = obj1.get(key);
                Object value2 = obj2.get(key);
                if (value1 instanceof JSONObject && value2 instanceof JSONObject) {
                    diffRecursive(currentPath, (JSONObject) value1, (JSONObject) value2, differences);
                } else if (value1 instanceof JSONArray && value2 instanceof JSONArray) {
                    diffArrays(currentPath, (JSONArray) value1, (JSONArray) value2, differences);
                } else if (!value1.equals(value2)) {
                    differences.add(new JsonDiff(currentPath, value1, value2, DiffType.MODIFIED));
                }
            }
        }

        // Find removed fields
        for (String key : obj1.keySet()) {
            if (!obj2.has(key)) {
                String currentPath = path.isEmpty() ? key : path + "." + key;
                differences.add(new JsonDiff(currentPath, obj1.get(key), null, DiffType.REMOVED));
            }
        }
    }

    private static void diffArrays(String path, JSONArray arr1, JSONArray arr2, List<JsonDiff> differences) {
        if (arr1.length() != arr2.length()) {
            differences.add(new JsonDiff(path, arr1, arr2, DiffType.MODIFIED));
            return;
        }

        for (int i = 0; i < arr1.length(); i++) {
            String currentPath = path + "[" + i + "]";
            Object value1 = arr1.get(i);
            Object value2 = arr2.get(i);
            if (value1 instanceof JSONObject && value2 instanceof JSONObject) {
                diffRecursive(currentPath, (JSONObject) value1, (JSONObject) value2, differences);
            } else if (value1 instanceof JSONArray && value2 instanceof JSONArray) {
                diffArrays(currentPath, (JSONArray) value1, (JSONArray) value2, differences);
            } else if (!value1.equals(value2)) {
                differences.add(new JsonDiff(currentPath, value1, value2, DiffType.MODIFIED));
            }
        }
    }
} 