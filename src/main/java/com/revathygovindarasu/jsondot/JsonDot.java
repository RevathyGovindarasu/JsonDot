package com.revathygovindarasu.jsondot;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Collections;
import java.lang.*;

public class JsonDot {
    private JSONObject jsonObject;

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
        
        // Check for invalid characters in path segments
        String[] parts = path.split("\\.");
        for (String part : parts) {
            if (part.isEmpty() || !part.matches("^[a-zA-Z0-9_\\[\\]]+$")) {
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
        
        String[] parts = path.split("\\.");
        JSONObject current = jsonObject;
        
        // Navigate through the path, creating missing objects
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            if (!current.has(part)) {
                current.put(part, new JSONObject());
            }
            current = current.getJSONObject(part);
        }
        
        // Add the value at the final path
        String lastPart = parts[parts.length - 1];
        
        // Handle Java arrays by converting them to JSONArray
        if (value != null && value.getClass().isArray()) {
            JSONArray jsonArray = new JSONArray();
            Object[] array = (Object[]) value;
            for (Object item : array) {
                jsonArray.put(item);
            }
            current.put(lastPart, jsonArray);
        } else {
            current.put(lastPart, value);
        }
        
        return this;
    }

    /**
     * Removes an element at the specified path using dot notation
     * @param path Dot notation path (e.g., "parent1.parent2.child")
     * @return The removed value, or null if not found
     * @throws JSONException if the path is invalid
     */
    public Object removeElement(String path) throws JSONException {
        String[] parts = path.split("\\.");
        JSONObject current = jsonObject;
        
        // Navigate through the path
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            if (!current.has(part)) {
                return null;
            }
            current = current.getJSONObject(part);
        }
        
        // Remove and return the value at the final path
        String lastPart = parts[parts.length - 1];
        if (current.has(lastPart)) {
            Object value = current.get(lastPart);
            current.remove(lastPart);
            return value;
        }
        return null;
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

        String[] parts = path.split("\\.");
        Object current = jsonObject;
        
        // Navigate through the path
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            
            if (current instanceof JSONObject) {
                JSONObject obj = (JSONObject) current;
                if (!obj.has(part)) {
                    throw new JSONException("Path not found: " + path);
                }
                current = obj.get(part);
            } else if (current instanceof JSONArray) {
                if (part.matches("\\d+")) {
                    JSONArray array = (JSONArray) current;
                    int index = Integer.parseInt(part);
                    if (index >= array.length()) {
                        throw new JSONException("Array index out of bounds: " + path);
                    }
                    current = array.get(index);
                } else {
                    throw new JSONException("Invalid array index: " + path);
                }
            } else {
                throw new JSONException("Path not found: " + path);
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
        String[] parts = path.split("\\.");
        JSONObject current = jsonObject;
        
        // Navigate through the path
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            if (!current.has(part)) {
                return null;
            }
            current = current.getJSONObject(part);
        }
        
        // Return the JSON object at the final path
        String lastPart = parts[parts.length - 1];
        return current.has(lastPart) ? new JsonDot(current.getJSONObject(lastPart)) : null;
    }

    /**
     * Gets a JSON array at the specified path using dot notation
     * @param path Dot notation path (e.g., "parent1.parent2.child")
     * @return JsonArrayDot instance containing the JSON array
     * @throws JSONException if the path is invalid or element is not a JSON array
     */
    public JsonArrayDot getArray(String path) throws JSONException {
        String[] parts = path.split("\\.");
        JSONObject current = jsonObject;
        
        // Navigate through the path
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            if (!current.has(part)) {
                return null;
            }
            current = current.getJSONObject(part);
        }
        
        // Return the JSON array at the final path
        String lastPart = parts[parts.length - 1];
        return current.has(lastPart) ? new JsonArrayDot(current.getJSONArray(lastPart)) : null;
    }

    /**
     * Checks if a path exists in the JSON object
     * @param path Dot notation path (e.g., "parent1.parent2.child")
     * @return true if the path exists, false otherwise
     */
    public boolean hasPath(String path) {
        try {
            String[] parts = path.split("\\.");
            JSONObject current = jsonObject;
            
            for (int i = 0; i < parts.length - 1; i++) {
                String part = parts[i];
                if (!current.has(part)) {
                    return false;
                }
                current = current.getJSONObject(part);
            }
            
            return current.has(parts[parts.length - 1]);
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
        return value != null ? value.toString() : null;
    }

    /**
     * Gets an integer value at the specified path
     * @param path Dot notation path
     * @return Integer value, or null if not found or not a number
     * @throws JSONException if the path is invalid
     */
    public Integer getInt(String path) throws JSONException {
        Object value = getElement(path);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Gets a double value at the specified path
     * @param path Dot notation path
     * @return Double value, or null if not found or not a number
     * @throws JSONException if the path is invalid
     */
    public Double getDouble(String path) throws JSONException {
        Object value = getElement(path);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Gets a boolean value at the specified path
     * @param path Dot notation path
     * @return Boolean value, or null if not found or not a boolean
     * @throws JSONException if the path is invalid
     */
    public Boolean getBoolean(String path) throws JSONException {
        Object value = getElement(path);
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
        return null;
    }

    /**
     * Suggests possible path completions based on a partial path.
     * 
     * @param partialPath The partial path to get suggestions for. If null or empty,
     *                    returns all top-level keys.
     * @return A Set of suggested complete paths that match the partial path.
     */
    public Set<String> suggestPaths(String partialPath) {
        Set<String> suggestions = new HashSet<>();
        
        // Handle null or empty path
        if (partialPath == null || partialPath.isEmpty()) {
            if (jsonObject instanceof JSONObject) {
                suggestions.addAll(((JSONObject) jsonObject).keySet());
            } else if (jsonObject instanceof JSONArray) {
                for (int i = 0; i < ((JSONArray) jsonObject).length(); i++) {
                    suggestions.add("[" + i + "]");
                }
            }
            return suggestions;
        }

        // Split the path into segments
        String[] segments = partialPath.split("\\.");
        Object current = jsonObject;
        StringBuilder currentPath = new StringBuilder();
        
        // Navigate through all but the last segment
        for (int i = 0; i < segments.length - 1; i++) {
            String segment = segments[i];
            
            // Handle array access
            if (segment.matches(".*\\[\\d+\\]")) {
                String arrayName = segment.substring(0, segment.indexOf('['));
                int index = Integer.parseInt(segment.substring(segment.indexOf('[') + 1, segment.indexOf(']')));
                
                if (current instanceof JSONObject && ((JSONObject) current).has(arrayName)) {
                    current = ((JSONObject) current).optJSONArray(arrayName);
                    if (current instanceof JSONArray && index < ((JSONArray) current).length()) {
                        current = ((JSONArray) current).opt(index);
                        currentPath.append(currentPath.length() > 0 ? "." : "").append(segment);
                        continue;
                    }
                }
                return suggestions;
            }
            
            // Handle regular object access
            if (current instanceof JSONObject && ((JSONObject) current).has(segment)) {
                current = ((JSONObject) current).opt(segment);
                currentPath.append(currentPath.length() > 0 ? "." : "").append(segment);
            } else {
                return suggestions;
            }
        }
        
        // Process the last segment for suggestions
        String lastSegment = segments[segments.length - 1];
        
        // Handle array index suggestions
        if (lastSegment.endsWith("[") || lastSegment.endsWith("].")) {
            String arrayName = lastSegment.replace("[", "").replace("].", "");
            if (current instanceof JSONObject && ((JSONObject) current).has(arrayName)) {
                Object array = ((JSONObject) current).opt(arrayName);
                if (array instanceof JSONArray) {
                    JSONArray jsonArray = (JSONArray) array;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        String suggestion = currentPath.toString() + 
                            (currentPath.length() > 0 ? "." : "") + 
                            arrayName + "[" + i + "]";
                        if (lastSegment.endsWith("].")) {
                            Object element = jsonArray.opt(i);
                            if (element instanceof JSONObject) {
                                for (String key : ((JSONObject) element).keySet()) {
                                    suggestions.add(suggestion + "." + key);
                                }
                            }
                        } else {
                            suggestions.add(suggestion);
                        }
                    }
                }
                return suggestions;
            }
        }
        
        // Handle regular object suggestions
        if (current instanceof JSONObject) {
            String prefix = currentPath.toString();
            prefix = prefix.isEmpty() ? "" : prefix + ".";
            
            for (String key : ((JSONObject) current).keySet()) {
                if (key.toLowerCase().startsWith(lastSegment.toLowerCase())) {
                    suggestions.add(prefix + key);
                }
            }
        } else if (current instanceof JSONArray) {
            String prefix = currentPath.toString();
            prefix = prefix.isEmpty() ? "" : prefix + ".";
            
            for (int i = 0; i < ((JSONArray) current).length(); i++) {
                Object element = ((JSONArray) current).opt(i);
                if (element instanceof JSONObject) {
                    for (String key : ((JSONObject) element).keySet()) {
                        if (key.toLowerCase().startsWith(lastSegment.toLowerCase())) {
                            suggestions.add(prefix + "[" + i + "]." + key);
                        }
                    }
                }
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
                if (element instanceof JSONObject && condition.test(element)) {
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
    public JsonArrayDot filterArray(String path, Predicate<Object> condition) {
        try {
            // Handle wildcard paths
            if (path.contains("*")) {
                List<Object> results = query(path);
                JSONArray filtered = new JSONArray();
                for (Object result : results) {
                    if (result instanceof JSONArray) {
                        JSONArray array = (JSONArray) result;
                        for (int i = 0; i < array.length(); i++) {
                            Object element = array.get(i);
                            if (element instanceof JSONObject && condition.test(element)) {
                                filtered.put(element);
                            }
                        }
                    } else if (result instanceof JSONObject && condition.test(result)) {
                        filtered.put(result);
                    }
                }
                return new JsonArrayDot(filtered);
            }

            // Handle direct paths
            Object value = getElement(path);
            if (value instanceof JSONArray) {
                return filterArray((JSONArray) value, condition);
            }
            return new JsonArrayDot(new JSONArray());
        } catch (Exception e) {
            return new JsonArrayDot(new JSONArray());
        }
    }
} 