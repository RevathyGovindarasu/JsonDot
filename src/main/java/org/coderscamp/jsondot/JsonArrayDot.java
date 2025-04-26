package org.coderscamp.jsondot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;

public class JsonArrayDot {
    private JSONArray jsonArray;

    public JsonArrayDot() {
        this.jsonArray = new JSONArray();
    }

    public JsonArrayDot(String jsonString) throws JSONException {
        this.jsonArray = new JSONArray(jsonString);
    }

    public JsonArrayDot(JSONArray jsonArray) {
        this.jsonArray = jsonArray;
    }

    /**
     * Adds an element to the array
     * @param value Value to add
     * @return this JsonArrayDot instance for method chaining
     */
    public JsonArrayDot add(Object value) {
        jsonArray.put(value);
        return this;
    }

    /**
     * Adds an element at the specified index
     * @param index Index where to add the element
     * @param value Value to add
     * @return this JsonArrayDot instance for method chaining
     * @throws JSONException if the index is invalid
     */
    public JsonArrayDot add(int index, Object value) throws JSONException {
        jsonArray.put(index, value);
        return this;
    }

    /**
     * Gets an element at the specified index
     * @param index Index of the element to get
     * @return The element at the specified index
     * @throws JSONException if the index is invalid
     */
    public Object get(int index) throws JSONException {
        return jsonArray.get(index);
    }

    /**
     * Gets a JSON object at the specified index
     * @param index Index of the JSON object to get
     * @return JsonDot instance containing the JSON object
     * @throws JSONException if the index is invalid or element is not a JSON object
     */
    public JsonDot getObject(int index) throws JSONException {
        return new JsonDot(jsonArray.getJSONObject(index));
    }

    /**
     * Gets a JSON array at the specified index
     * @param index Index of the JSON array to get
     * @return JsonArrayDot instance containing the JSON array
     * @throws JSONException if the index is invalid or element is not a JSON array
     */
    public JsonArrayDot getArray(int index) throws JSONException {
        return new JsonArrayDot(jsonArray.getJSONArray(index));
    }

    /**
     * Removes an element at the specified index
     * @param index Index of the element to remove
     * @return The removed element
     * @throws JSONException if the index is invalid
     */
    public Object remove(int index) throws JSONException {
        Object value = jsonArray.get(index);
        JSONArray newArray = new JSONArray();
        for (int i = 0; i < jsonArray.length(); i++) {
            if (i != index) {
                newArray.put(jsonArray.get(i));
            }
        }
        jsonArray = newArray;
        return value;
    }

    /**
     * Gets the length of the array
     * @return The number of elements in the array
     */
    public int length() {
        return jsonArray.length();
    }

    /**
     * Converts the JSON array to a string
     * @return String representation of the JSON array
     */
    @Override
    public String toString() {
        return jsonArray.toString();
    }

    /**
     * Converts the JSON array to a pretty-printed string
     * @return Pretty-printed string representation of the JSON array
     */
    public String toPrettyString() {
        return jsonArray.toString(2);
    }

    /**
     * Gets the underlying JSONArray
     * @return The JSONArray instance
     */
    public JSONArray getJSONArray() {
        return jsonArray;
    }

    /**
     * Filters the array based on a predicate
     * @param predicate The condition to filter by
     * @return A new JsonArrayDot containing only the elements that match the predicate
     */
    public JsonArrayDot filter(Predicate<Object> predicate) {
        JSONArray filtered = new JSONArray();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                Object element = jsonArray.get(i);
                if (predicate.test(element)) {
                    filtered.put(element);
                }
            } catch (JSONException e) {
                // Skip elements that can't be accessed
            }
        }
        return new JsonArrayDot(filtered);
    }

    /**
     * Maps each element in the array using a mapper function
     * @param mapper The function to transform each element
     * @return A new JsonArrayDot containing the transformed elements
     */
    public JsonArrayDot map(java.util.function.Function<Object, Object> mapper) {
        JSONArray mapped = new JSONArray();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                Object element = jsonArray.get(i);
                Object transformed = mapper.apply(element);
                mapped.put(transformed);
            } catch (JSONException e) {
                // Skip elements that can't be accessed
            }
        }
        return new JsonArrayDot(mapped);
    }

    /**
     * Gets all elements that match a predicate
     * @param predicate The condition to match
     * @return List of matching elements
     */
    public List<Object> findAll(Predicate<Object> predicate) {
        List<Object> results = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                Object element = jsonArray.get(i);
                if (predicate.test(element)) {
                    results.add(element);
                }
            } catch (JSONException e) {
                // Skip elements that can't be accessed
            }
        }
        return results;
    }
} 