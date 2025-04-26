package tech.coderscamp.jsondot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.XML;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
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
     * Deep merges two JsonDot objects
     * @param json1 First JsonDot object (target)
     * @param json2 Second JsonDot object (source)
     * @return Merged JsonDot object
     * @throws JSONException if merging fails
     */
    public static JsonDot deepMerge(JsonDot json1, JsonDot json2) throws JSONException {
        return deepMerge(json1, json2, ArrayMergeStrategy.APPEND);
    }

    /**
     * Deep merges two JsonDot objects with a specified array merge strategy
     * @param json1 First JsonDot object (target)
     * @param json2 Second JsonDot object (source)
     * @param arrayMergeStrategy Strategy to use when merging arrays
     * @return Merged JsonDot object
     * @throws JSONException if merging fails
     */
    public static JsonDot deepMerge(JsonDot json1, JsonDot json2, ArrayMergeStrategy arrayMergeStrategy) throws JSONException {
        if (json1 == null) return json2;
        if (json2 == null) return json1;
        
        JSONObject merged = deepMergeObjects(json1.getJSONObject(), json2.getJSONObject(), arrayMergeStrategy);
        return new JsonDot(merged.toString());
    }

    /**
     * Deep merges two JSONObjects recursively
     * @param target Target JSONObject
     * @param source Source JSONObject
     * @param strategy Strategy to use when merging arrays
     * @return Merged JSONObject
     * @throws JSONException if merging fails
     */
    private static JSONObject deepMergeObjects(JSONObject target, JSONObject source, ArrayMergeStrategy strategy) {
        if (target == null) return source;
        if (source == null) return target;

        JSONObject result = new JSONObject();
        
        // First copy all target fields
        for (String key : target.keySet()) {
            result.put(key, target.get(key));
        }
        
        // Then merge source fields
        for (String key : source.keySet()) {
            if (!result.has(key)) {
                // If key doesn't exist in target, just add it
                result.put(key, source.get(key));
            } else {
                Object targetValue = result.get(key);
                Object sourceValue = source.get(key);
                
                if (targetValue instanceof JSONObject && sourceValue instanceof JSONObject) {
                    // For objects, always merge recursively regardless of strategy
                    result.put(key, deepMergeObjects((JSONObject) targetValue, (JSONObject) sourceValue, strategy));
                } else if (targetValue instanceof JSONArray && sourceValue instanceof JSONArray) {
                    // For arrays, use the specified strategy
                    result.put(key, mergeArrays((JSONArray) targetValue, (JSONArray) sourceValue, strategy));
                } else {
                    // For primitive values, use source value
                    result.put(key, sourceValue);
                }
            }
        }
        
        return result;
    }

    /**
     * Merges two JSONArrays based on the specified strategy
     * @param target Target JSONArray
     * @param source Source JSONArray
     * @param strategy Strategy to use when merging arrays
     * @return Merged JSONArray
     * @throws JSONException if merging fails
     */
    private static JSONArray mergeArrays(JSONArray target, JSONArray source, ArrayMergeStrategy strategy) {
        if (target == null) return source;
        if (source == null) return target;

        JSONArray result = new JSONArray();
        
        switch (strategy) {
            case OVERWRITE:
                // For OVERWRITE, just return a new array with source elements
                for (int i = 0; i < source.length(); i++) {
                    result.put(source.get(i));
                }
                return result;
                
            case APPEND:
                // First add all target elements
                for (int i = 0; i < target.length(); i++) {
                    result.put(target.get(i));
                }
                // Then add all source elements
                for (int i = 0; i < source.length(); i++) {
                    result.put(source.get(i));
                }
                break;
                
            default:
                throw new IllegalArgumentException("Unknown merge strategy: " + strategy);
        }
        
        return result;
    }

    private static boolean haveMatchingKeys(JSONObject obj1, JSONObject obj2) {
        if (obj1 == null || obj2 == null) return false;
        for (String key : obj1.keySet()) {
            if (obj2.has(key)) return true;
        }
        return false;
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
     * @param xml String XML string to convert
     * @return JsonDot instance
     * @throws JSONException if conversion fails
     */
    public static JsonDot fromXml(String xml) throws JSONException {
        try {
            JSONObject jsonObject = XML.toJSONObject(xml);
            return new JsonDot(jsonObject);
        } catch (JSONException e) {
            throw new JSONException("Failed to convert XML to JSON: " + e.getMessage());
        }
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
        OVERWRITE
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