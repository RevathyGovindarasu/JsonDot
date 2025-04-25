package com.rgov.jsondot;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.rgov.jsondot.JsonUtils.ArrayMergeStrategy;
import com.rgov.jsondot.JsonDot;
import com.rgov.jsondot.JsonArrayDot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.JVM)
public class JsonDotTest {
    private JsonDot json;
    private String sampleJson;

    @Before
    public void setUp() throws IOException {
        // Read the sample JSON file
        sampleJson = new String(Files.readAllBytes(Paths.get("src/test/resources/sampleJson.json")));
        json = new JsonDot(sampleJson);
    }

    @Test
    public void testBasicOperations() throws JSONException {
        System.out.println("\n=== Testing Basic Operations ===");
        
        // Get values using dot notation
        String name = json.getString("user.name");
        assertEquals("John Doe", name);
        System.out.println("Name: " + name);

        Integer age = json.getInt("user.age");
        assertEquals(30, age.intValue());
        System.out.println("Age: " + age);

        Boolean active = json.getBoolean("user.active");
        assertTrue(active);
        System.out.println("Active: " + active);

        // Pretty print
        System.out.println("\nPretty Printed JSON:");
        System.out.println(json.toPrettyString());
    }

    @Test
    public void testNestedObjectAccess() throws JSONException {
        System.out.println("\n=== Testing Nested Object Access ===");
        
        String city = json.getString("user.address.city");
        assertEquals("New York", city);
        System.out.println("City: " + city);

        Double lat = json.getDouble("user.address.coordinates.lat");
        assertEquals(40.7128, lat, 0.0001);
        System.out.println("Latitude: " + lat);

        // Pretty print the address object
        System.out.println("\nAddress Object:");
        System.out.println(json.getObject("user.address").toPrettyString());
    }

    @Test
    public void testArrayOperations() throws JSONException {
        System.out.println("\n=== Testing Array Operations ===");
        
        // Get array length
        JsonArrayDot contacts = json.getArray("user.contacts");
        assertEquals(2, contacts.length());
        System.out.println("Number of contacts: " + contacts.length());

        // Get array element
        JsonDot firstContact = new JsonDot(contacts.get(0).toString());
        String email = firstContact.getString("value");
        assertEquals("john@example.com", email);
        System.out.println("Primary contact: " + email);

        // Pretty print the contacts array
        System.out.println("\nContacts Array:");
        System.out.println(contacts.toPrettyString());
    }

    @Test
    public void testPathSuggestions() throws JSONException {
        System.out.println("\n=== Testing Path Suggestions ===");
        
        // Test path suggestions
        List<String> suggestions = json.suggestPaths("user.ad");
        System.out.println("Suggestions for 'user.ad':");
        suggestions.forEach(System.out::println);
        
        // Verify suggestions
        assertTrue(suggestions.contains("user.address"));
        assertFalse(suggestions.contains("user.active"));
    }

    @Test
    public void testWildcardQueries() throws JSONException {
        System.out.println("\n=== Testing Wildcard Queries ===");
        
        List<Object> prices = json.query("products.*.price");
        assertEquals(2, prices.size());
        System.out.println("Product prices: " + prices);

        List<Object> tags = json.query("products.*.tags.*");
        assertEquals(4, tags.size());
        System.out.println("All tags: " + tags);
    }

    @Test
    public void testArrayFiltering() throws JSONException {
        System.out.println("\n=== Testing Array Filtering ===");
        
        // Filter products in stock
        JsonArrayDot inStockProducts = json.filterArray("products", 
            product -> {
                JsonDot productDot = new JsonDot(product.toString());
                return productDot.getBoolean("inStock") == true;
            });
        assertEquals(1, inStockProducts.length());
        System.out.println("In-stock products:");
        System.out.println(inStockProducts.toPrettyString());

        // Filter contacts by type
        JsonArrayDot emailContacts = json.filterArray("user.contacts", 
            contact -> {
                JsonDot contactDot = new JsonDot(contact.toString());
                return "email".equals(contactDot.getString("type"));
            });
        assertEquals(1, emailContacts.length());
        System.out.println("\nEmail contacts:");
        System.out.println(emailContacts.toPrettyString());
    }

    @Test
    public void testModifications() throws JSONException {
        System.out.println("\n=== Testing Modifications ===");
        
        // Add new element
        json.addElement("user.address.country", "USA");
        assertEquals("USA", json.getString("user.address.country"));
        System.out.println("After adding country:");
        System.out.println(json.getObject("user.address").toPrettyString());

        // Modify existing element
        json.addElement("user.age", 31);
        assertEquals(31, json.getInt("user.age").intValue());
        System.out.println("\nAfter updating age:");
        System.out.println(json.getObject("user").toPrettyString());

        // Remove element
        json.removeElementFromPath("user.preferences.theme");
        assertFalse(json.hasPath("user.preferences.theme"));
        System.out.println("\nAfter removing theme:");
        System.out.println(json.getObject("user.preferences").toPrettyString());
    }

    @Test
    public void testXMLConversion() throws JSONException {
        System.out.println("\n=== Testing XML Conversion ===");
        
        String xml = json.toXml();
        assertNotNull(xml);
        System.out.println("XML Conversion:");
        System.out.println(xml);

        String prettyXml = json.toPrettyXml();
        assertNotNull(prettyXml);
        System.out.println("\nPretty Printed XML:");
        System.out.println(prettyXml);
    }

    @Test
    public void testErrorHandling() {
        System.out.println("\n=== Testing Error Handling ===");
        
        // Test invalid path
        try {
            json.getString("nonexistent.path");
            fail("Expected JSONException");
        } catch (JSONException e) {
            System.out.println("Expected error for invalid path: " + e.getMessage());
        }

        // Test array index out of bounds
        try {
            json.getElement("user.contacts[10]");
            fail("Expected JSONException");
        } catch (JSONException e) {
            System.out.println("Expected error for array index: " + e.getMessage());
        }

        // Test invalid JSON
        try {
            new JsonDot("{invalid json}");
            fail("Expected JSONException");
        } catch (JSONException e) {
            System.out.println("Expected error for invalid JSON: " + e.getMessage());
        }
    }

    @Test
    public void testComprehensivePathOperations() throws JSONException {
        System.out.println("\n=== Testing Comprehensive Path Operations ===");
        
        // 1. Add to first element
        json.addElement("user.contacts[0].priority", "high");
        assertTrue(json.hasPath("user.contacts[0].priority"));
        System.out.println("After adding priority to first contact:");
        System.out.println(json.getObject("user.contacts[0]").toPrettyString());
        
        // 2. Add to second contact
        json.addElement("user.contacts[1].priority", "medium");
        assertTrue(json.hasPath("user.contacts[1].priority"));
        System.out.println("\nAfter adding priority to 2nd contact:");
        System.out.println(json.getObject("user.contacts[1]").toPrettyString());
        
        // 3. Add to all contacts
        json.addElementToAllPaths("user.contacts[*].status", "active");
        JsonArrayDot contacts = json.getArray("user.contacts");
        for (int i = 0; i < contacts.length(); i++) {
            JsonDot contact = new JsonDot(contacts.get(i).toString());
            assertTrue(contact.hasPath("status"));
        }
        System.out.println("\nAfter adding status to all contacts:");
        System.out.println(json.getObject("user.contacts").toPrettyString());
        
        // 4. Remove from first element
        Object removedPriority = json.removeElementFromPath("user.contacts[0].priority");
        assertEquals("high", removedPriority);
        assertFalse(json.hasPath("user.contacts[0].priority"));
        System.out.println("\nAfter removing priority from first contact:");
        System.out.println(json.getObject("user.contacts[0]").toPrettyString());
        
        // 5. Remove from second contact
        Object removedStatus = json.removeElementFromPath("user.contacts[1].status");
        assertEquals("active", removedStatus);
        System.out.println("\nAfter removing status from second contact:");
        System.out.println(json.getObject("user.contacts").toPrettyString());
        
        // 6. Remove from all contacts
        List<Object> removedStatuses = json.removeElementFromAllPaths("user.contacts[*].status");
        assertEquals(1, removedStatuses.size()); // Only one status left to remove
        contacts = json.getArray("user.contacts");
        for (int i = 0; i < contacts.length(); i++) {
            JsonDot contact = new JsonDot(contacts.get(i).toString());
            assertFalse(contact.hasPath("status"));
        }
        System.out.println("\nAfter removing status from all contacts:");
        System.out.println(json.getObject("user.contacts").toPrettyString());
    }

    @Test
    public void testMultiplePathOperations() throws JSONException {
        System.out.println("\n=== Testing Multiple Path Operations ===");
        
        // First add tags in different locations
        System.out.println("\nBefore adding tags:");
        System.out.println(json.toPrettyString());
        
        // Initialize arrays first
        System.out.println("\nInitializing arrays...");
        json.addElement("user.tags", new JSONArray());
        json.addElement("products[0].tags", new JSONArray());
        json.addElement("products[1].tags", new JSONArray());
        
        // Add tags under user
        System.out.println("\nAdding user tags...");
        json.addElement("user.tags[0]", "tag1");
        json.addElement("user.tags[1]", "tag2");
        json.addElement("user.tags[2]", "tag3");
        
        // Add tags under products (each product has its own tags array)
        System.out.println("\nAdding product tags...");
        json.addElement("products[0].tags[0]", "tag4");
        json.addElement("products[0].tags[1]", "tag5");
        json.addElement("products[1].tags[0]", "tag6");
        json.addElement("products[1].tags[1]", "tag7");
        
        System.out.println("\nAfter adding tags:");
        System.out.println(json.toPrettyString());
        
        // Access and remove tags using explicit paths
        System.out.println("\nRemoving tags...");
        // Remove tag2 from user.tags
        Object removedTag = json.removeElementFromPath("user.tags[1]");
        System.out.println("\nRemoved tag from user.tags[1]: " + removedTag);
        
        // Remove tag5 from first product
        removedTag = json.removeElementFromPath("products[0].tags[1]");
        System.out.println("\nRemoved tag from products[0].tags[1]: " + removedTag);
        
        // Print the final state
        System.out.println("\nFinal state of user.tags:");
        System.out.println(json.getArray("user.tags").toPrettyString());
        
        System.out.println("\nChecking products[0].tags...");
        JsonArrayDot product1Tags = json.getArray("products[0].tags");
        System.out.println("product1Tags: " + (product1Tags == null ? "null" : product1Tags.toPrettyString()));
        
        System.out.println("\nChecking products[1].tags...");
        JsonArrayDot product2Tags = json.getArray("products[1].tags");
        System.out.println("product2Tags: " + (product2Tags == null ? "null" : product2Tags.toPrettyString()));
        
        // Verify the removals
        JsonArrayDot userTags = json.getArray("user.tags");
        
        // user.tags should have 2 tags (tag2 removed)
        assertEquals(2, userTags.length());
        assertEquals("tag1", userTags.get(0));
        assertEquals("tag3", userTags.get(1));
        
        // products[0].tags should have 1 tag (tag5 removed)
        assertNotNull(product1Tags);
        assertEquals(1, product1Tags.length());
        assertEquals("tag4", product1Tags.get(0));
        
        // products[1].tags should still have both tags
        assertNotNull(product2Tags);
        assertEquals(2, product2Tags.length());
        assertEquals("tag6", product2Tags.get(0));
        assertEquals("tag7", product2Tags.get(1));
    }

    @Test
    public void testDeepMerge() {
        System.out.println("\n=== Testing Deep Merge ===");
        
        // Initialize hobbies array in the target JSON
        json.addElement("user.hobbies", new JSONArray().put("reading").put("gaming"));
        
        JSONObject source = new JSONObject("{\n" +
            "    \"user\": {\n" +
            "        \"hobbies\": [\"swimming\", \"hiking\"],\n" +
            "        \"scores\": [90, 85, 95]\n" +
            "    }\n" +
            "}");
        
        JsonDot sourceDot = new JsonDot(source);
        JsonDot result = JsonUtils.deepMerge(json, sourceDot, ArrayMergeStrategy.APPEND);
        
        // Check array merge
        JsonArrayDot hobbies = result.getArray("user.hobbies");
        assertEquals(4, hobbies.length());
        List<Object> hobbiesList = hobbies.findAll(obj -> true);
        assertTrue(hobbiesList.containsAll(java.util.Arrays.asList("reading", "gaming", "swimming", "hiking")));
        
        // Check new array
        JsonArrayDot scores = result.getArray("user.scores");
        assertEquals(3, scores.length());
        List<Object> scoresList = scores.findAll(obj -> true);
        assertEquals(Integer.valueOf(90), scoresList.get(0));
        assertEquals(Integer.valueOf(85), scoresList.get(1));
        assertEquals(Integer.valueOf(95), scoresList.get(2));
    }

    @Test
    public void testDeepMergeWithNull() {
        System.out.println("\n=== Testing Deep Merge With Null ===");
        
        JSONObject source = new JSONObject("{\n" +
            "    \"user\": {\n" +
            "        \"name\": null,\n" +
            "        \"newField\": \"value\"\n" +
            "    }\n" +
            "}");
        
        System.out.println("Source JSON:");
        System.out.println(source.toString(2));
        
        System.out.println("\nTarget JSON:");
        System.out.println(json.toPrettyString());
        
        JsonDot sourceDot = new JsonDot(source);
        JsonDot result = JsonUtils.deepMerge(json, sourceDot);
        
        System.out.println("\nMerged Result:");
        System.out.println(result.toPrettyString());
        
        // Check that null values are preserved
        System.out.println("\nChecking null values...");
        System.out.println("user.name is null: " + result.isNull("user.name"));
        System.out.println("user.newField: " + result.getString("user.newField"));
        
        assertTrue(result.isNull("user.name"));
        assertEquals("value", result.getString("user.newField"));
    }

    @Test
    public void testDeepMergeWithArrays() throws JSONException {
        System.out.println("\n=== Testing Deep Merge With Arrays ===");
        
        // Initialize test data
        JsonDot json1 = new JsonDot("{\"arr\": [1, 2, {\"x\": 1}]}");
        JsonDot json2 = new JsonDot("{\"arr\": [3, null, {\"x\": 2, \"y\": 3}]}");
        
        System.out.println("Original JSON (json1):");
        System.out.println(json1.toPrettyString());
        System.out.println("\nUpdate JSON (json2):");
        System.out.println(json2.toPrettyString());
        
        // Test APPEND strategy
        System.out.println("\n1. Testing APPEND Strategy");
        System.out.println("Expected: Combine both arrays, preserving all elements");
        JsonDot appendMerged = JsonUtils.deepMerge(json1, json2, ArrayMergeStrategy.APPEND);
        System.out.println("Result after APPEND:");
        System.out.println(appendMerged.toPrettyString());
        
        JSONArray appendArr = appendMerged.getJSONArray("arr");
        System.out.println("\nVerifying APPEND results:");
        System.out.println("Array length: " + appendArr.length());
        for (int i = 0; i < appendArr.length(); i++) {
            System.out.println("Element " + i + ": " + appendArr.get(i));
        }
        
        // Verify APPEND results
        assertEquals(6, appendArr.length());
        assertEquals(1, appendArr.getInt(0));
        assertEquals(2, appendArr.getInt(1));
        assertEquals(1, ((JSONObject)appendArr.get(2)).getInt("x"));
        assertEquals(3, appendArr.getInt(3));
        assertTrue(appendArr.isNull(4));
        assertEquals(2, ((JSONObject)appendArr.get(5)).getInt("x"));
        assertEquals(3, ((JSONObject)appendArr.get(5)).getInt("y"));

        // Test OVERWRITE strategy
        System.out.println("\n2. Testing OVERWRITE Strategy");
        System.out.println("Expected: Replace entire array with source array");
        JsonDot overwriteMerged = JsonUtils.deepMerge(json1, json2, ArrayMergeStrategy.OVERWRITE);
        System.out.println("Result after OVERWRITE:");
        System.out.println(overwriteMerged.toPrettyString());
        
        JSONArray overwriteArr = overwriteMerged.getJSONArray("arr");
        System.out.println("\nVerifying OVERWRITE results:");
        System.out.println("Array length: " + overwriteArr.length());
        for (int i = 0; i < overwriteArr.length(); i++) {
            System.out.println("Element " + i + ": " + overwriteArr.get(i));
        }
        
        // Verify OVERWRITE results
        assertEquals(3, overwriteArr.length());
        assertEquals(3, overwriteArr.getInt(0));
        assertTrue(overwriteArr.isNull(1));
        assertEquals(2, ((JSONObject)overwriteArr.get(2)).getInt("x"));
        assertEquals(3, ((JSONObject)overwriteArr.get(2)).getInt("y"));
    }

    @Test
    public void testIsValidJson() {
        // Valid JSON strings
        assertTrue(JsonUtils.isValidJson("{}"));
        assertTrue(JsonUtils.isValidJson("{\"key\":\"value\"}"));
        assertTrue(JsonUtils.isValidJson("[]"));
        assertTrue(JsonUtils.isValidJson("[1,2,3]"));
        
        // Invalid JSON strings
        assertFalse(JsonUtils.isValidJson(""));
        assertFalse(JsonUtils.isValidJson("{"));
        assertFalse(JsonUtils.isValidJson("["));
        assertFalse(JsonUtils.isValidJson("{\"key\":}"));
    }

    @Test
    public void testToXml() throws JsonProcessingException {
        JSONObject jsonObj = new JSONObject("{\n" +
            "    \"user\": {\n" +
            "        \"name\": \"John\",\n" +
            "        \"age\": 30,\n" +
            "        \"active\": true\n" +
            "    }\n" +
            "}");
        
        JsonDot jsonDot = new JsonDot(jsonObj);
        String xml = jsonDot.toXml();
        assertTrue(xml.contains("<user>"));
        assertTrue(xml.contains("<name>John</name>"));
        assertTrue(xml.contains("<age>30</age>"));
        assertTrue(xml.contains("<active>true</active>"));
    }

    @Test
    public void testFromXml() throws JsonProcessingException {
        String xml = "<user>\n" +
            "    <name>John</name>\n" +
            "    <age>30</age>\n" +
            "    <active>true</active>\n" +
            "</user>";
        
        JsonDot jsonDot = JsonUtils.fromXml(xml);
        assertEquals("John", jsonDot.getString("user.name"));
        assertEquals(Integer.valueOf(30), jsonDot.getInt("user.age"));
        assertTrue(jsonDot.getBoolean("user.active"));
    }

    @Test
    public void testDeepMergeWithNulls() throws JSONException {
        JsonDot json1 = new JsonDot("{\"a\": 1, \"b\": null, \"c\": {\"d\": 2}}");
        JsonDot json2 = new JsonDot("{\"b\": 2, \"c\": null, \"e\": 3}");
        JsonDot merged = JsonUtils.deepMerge(json1, json2, ArrayMergeStrategy.APPEND);
        
        assertEquals(1, merged.get("a"));
        assertEquals(2, merged.get("b"));
        assertTrue(merged.isNull("c"));
        assertEquals(3, merged.get("e"));
    }

    @Test
    public void testOverwriteStrategy() throws JSONException {
        System.out.println("\n=== Testing OVERWRITE Strategy ===");
        
        // Original JSON with complete user information
        JsonDot original = new JsonDot("{\n" +
            "    \"user\": {\n" +
            "        \"name\": \"John\",\n" +
            "        \"age\": 30,\n" +
            "        \"address\": {\n" +
            "            \"city\": \"Los Angeles\",\n" +
            "            \"state\": \"CA\",\n" +
            "            \"zip\": \"90001\"\n" +
            "        },\n" +
            "        \"contacts\": [\n" +
            "            {\"type\": \"email\", \"value\": \"john@example.com\"},\n" +
            "            {\"type\": \"phone\", \"value\": \"123-456-7890\"}\n" +
            "        ]\n" +
            "    }\n" +
            "}");
        
        System.out.println("Original JSON:");
        System.out.println(original.toPrettyString());
        
        // Update JSON with partial changes
        JsonDot updates = new JsonDot("{\n" +
            "    \"user\": {\n" +
            "        \"age\": 31,\n" +
            "        \"address\": {\n" +
            "            \"state\": \"NY\"\n" +
            "        },\n" +
            "        \"contacts\": [\n" +
            "            {\"type\": \"email\", \"value\": \"john.new@example.com\"}\n" +
            "        ]\n" +
            "    }\n" +
            "}");
        
        System.out.println("\nUpdate JSON:");
        System.out.println(updates.toPrettyString());
        
        // Apply updates using OVERWRITE strategy
        JsonDot result = JsonUtils.deepMerge(original, updates, ArrayMergeStrategy.OVERWRITE);
        
        System.out.println("\nResult after OVERWRITE:");
        System.out.println(result.toPrettyString());
        
        // Verify that only specific paths were updated
        assertEquals("John", result.getString("user.name"));  // Unchanged
        assertEquals(Integer.valueOf(31), result.getInt("user.age"));  // Updated
        
        // Verify address updates
        JSONObject address = result.getJSONObject("user.address");
        assertEquals("Los Angeles", address.getString("city"));  // Unchanged
        assertEquals("NY", address.getString("state"));          // Updated
        assertEquals("90001", address.getString("zip"));         // Unchanged
        
        // Verify contacts array was completely replaced
        JSONArray contacts = result.getJSONArray("user.contacts");
        assertEquals(1, contacts.length());  // Only one contact now
        JSONObject emailContact = contacts.getJSONObject(0);
        assertEquals("email", emailContact.getString("type"));
        assertEquals("john.new@example.com", emailContact.getString("value"));
    }

    @Test
    public void testUpdateValues() throws JSONException {
        System.out.println("\n=== Testing Value Updates ===");
        
        // Initialize a sample JSON with user information
        JsonDot json = new JsonDot("{\n" +
            "    \"user\": {\n" +
            "        \"name\": \"John\",\n" +
            "        \"age\": 30,\n" +
            "        \"active\": true,\n" +
            "        \"address\": {\n" +
            "            \"city\": \"Los Angeles\",\n" +
            "            \"state\": \"CA\",\n" +
            "            \"zip\": \"90001\"\n" +
            "        },\n" +
            "        \"contacts\": [\n" +
            "            {\"type\": \"email\", \"value\": \"john@example.com\"},\n" +
            "            {\"type\": \"phone\", \"value\": \"123-456-7890\"}\n" +
            "        ]\n" +
            "    }\n" +
            "}");
        
        System.out.println("Original JSON:");
        System.out.println(json.toPrettyString());
        
        // 1. Update a primitive value (string)
        System.out.println("\n1. Updating primitive value (string):");
        json.addElement("user.name", "John Doe");
        assertEquals("John Doe", json.getString("user.name"));
        System.out.println("After updating name:");
        System.out.println(json.getObject("user").toPrettyString());
        
        // 2. Update a primitive value (number)
        System.out.println("\n2. Updating primitive value (number):");
        json.addElement("user.age", 31);
        assertEquals(Integer.valueOf(31), json.getInt("user.age"));
        System.out.println("After updating age:");
        System.out.println(json.getObject("user").toPrettyString());
        
        // 3. Update a primitive value (boolean)
        System.out.println("\n3. Updating primitive value (boolean):");
        json.addElement("user.active", false);
        assertFalse(json.getBoolean("user.active"));
        System.out.println("After updating active status:");
        System.out.println(json.getObject("user").toPrettyString());
        
        // 4. Update a nested object value
        System.out.println("\n4. Updating nested object value:");
        json.addElement("user.address.state", "NY");
        assertEquals("NY", json.getString("user.address.state"));
        System.out.println("After updating state:");
        System.out.println(json.getObject("user.address").toPrettyString());
        
        // 5. Update an array element by index
        System.out.println("\n5. Updating array element by index:");
        json.addElement("user.contacts[0].value", "john.doe@example.com");
        assertEquals("john.doe@example.com", 
            json.getObject("user.contacts[0]").getString("value"));
        System.out.println("After updating first contact:");
        System.out.println(json.getArray("user.contacts").toPrettyString());
        
        // 6. Update multiple nested values in one go
        System.out.println("\n6. Updating multiple nested values:");
        json.addElement("user.address.city", "New York");
        json.addElement("user.address.zip", "10001");
        assertEquals("New York", json.getString("user.address.city"));
        assertEquals("10001", json.getString("user.address.zip"));
        System.out.println("After updating address:");
        System.out.println(json.getObject("user.address").toPrettyString());
        
        // 7. Update with null value
        System.out.println("\n7. Updating with null value:");
        json.addElement("user.age", null);
        assertFalse(json.hasPath("user.age"));  // Path should be removed when set to null
        System.out.println("After setting age to null:");
        System.out.println(json.getObject("user").toPrettyString());
        
        // 8. Update non-existent path (creates new path)
        System.out.println("\n8. Creating new path:");
        json.addElement("user.preferences.theme", "dark");
        assertEquals("dark", json.getString("user.preferences.theme"));
        System.out.println("After adding new preference:");
        System.out.println(json.getObject("user.preferences").toPrettyString());
        
        // Print final state
        System.out.println("\nFinal JSON state:");
        System.out.println(json.toPrettyString());
    }

    @Test
    public void testUpdateWithInvalidPaths() {
        System.out.println("\n=== Testing Invalid Path Updates ===");
        
        JsonDot json = new JsonDot("{\"user\": {\"name\": \"John\", \"contacts\": [{\"type\": \"email\"}]}}");
        
        // 1. Test updating with invalid array index
        try {
            System.out.println("Attempting to add element with invalid array index...");
            json.addElement("user.contacts[invalid].type", "email");
            System.out.println("No exception thrown!");
        } catch (JSONException e) {
            System.out.println("Caught exception: " + e.getMessage());
            assertTrue(e.getMessage().contains("Invalid path format"));
        }
        
        // 2. Test updating with out of bounds array index
        try {
            System.out.println("\nAttempting to add element with out of bounds index...");
            json.addElement("user.contacts[10].type", "email");
            System.out.println("No exception thrown!");
        } catch (JSONException e) {
            System.out.println("Caught exception: " + e.getMessage());
            assertTrue(e.getMessage().contains("Array index out of bounds"));
        }
        
        // 3. Test updating with malformed path
        try {
            System.out.println("\nAttempting to add element with malformed path...");
            json.addElement("user..name", "John");
            System.out.println("No exception thrown!");
        } catch (JSONException e) {
            System.out.println("Caught exception: " + e.getMessage());
            assertTrue(e.getMessage().contains("Invalid path format"));
        }
    }

    @Test
    public void testWildcardArrayAddElement() throws JSONException {
        System.out.println("\n=== Testing Wildcard Array Add Element ===");
        
        // Create a test JSON with an array of objects
        JSONObject testJson = new JSONObject();
        JSONArray products = new JSONArray();
        
        // Add two product objects
        JSONObject product1 = new JSONObject();
        product1.put("name", "Product 1");
        product1.put("price", 100);
        
        JSONObject product2 = new JSONObject();
        product2.put("name", "Product 2");
        product2.put("price", 200);
        
        products.put(product1);
        products.put(product2);
        testJson.put("products", products);
        
        JsonDot json = new JsonDot(testJson);
        
        // Add category to all products using wildcard
        json.addElement("products[*].category", "Electronics");
        
        // Verify that both products have the category
        JsonArrayDot productsArray = json.getArray("products");
        assertEquals(2, productsArray.length());
        
        for (int i = 0; i < productsArray.length(); i++) {
            JsonDot product = new JsonDot(productsArray.get(i).toString());
            assertEquals("Electronics", product.getString("category"));
        }
        
        System.out.println("After adding category to all products:");
        System.out.println(json.toPrettyString());
    }

    @Test
    public void testWildcardArrayRemoveElement() throws JSONException {
        System.out.println("\n=== Testing Wildcard Array Remove Element ===");
        
        // Create a test JSON with nested arrays
        JSONObject testJson = new JSONObject();
        JSONArray outerArray = new JSONArray();
        
        // Create first inner array with elements
        JSONArray innerArray1 = new JSONArray();
        innerArray1.put("element1");
        innerArray1.put("element2");
        innerArray1.put("element3");
        
        // Create second inner array with elements
        JSONArray innerArray2 = new JSONArray();
        innerArray2.put("element4");
        innerArray2.put("element5");
        innerArray2.put("element6");
        
        // Add inner arrays to outer array
        outerArray.put(innerArray1);
        outerArray.put(innerArray2);
        testJson.put("arrays", outerArray);
        
        JsonDot json = new JsonDot(testJson);
        System.out.println("Original JSON:");
        System.out.println(json.toPrettyString());
        
        // Remove all elements from inner arrays using wildcard
        List<Object> removedElements = json.removeElementFromAllPaths("arrays[*]");
        
        // Verify that 1 element was removed (the first inner array)
        assertEquals(1, removedElements.size());
        
        // Verify that the outer array now has 1 element
        JsonArrayDot outerArrayDot = json.getArray("arrays");
        assertEquals(1, outerArrayDot.length());
        
        System.out.println("\nAfter removing first inner array:");
        System.out.println(json.toPrettyString());
    }

    @Test
    public void testWildcardArrayGet() throws JSONException {
        System.out.println("\n=== Testing Wildcard Array Get ===");
        
        // Create a test JSON with nested arrays
        JSONObject testJson = new JSONObject();
        JSONArray outerArray = new JSONArray();
        
        // Create first inner array with elements
        JSONArray innerArray1 = new JSONArray();
        innerArray1.put("element1");
        innerArray1.put("element2");
        innerArray1.put("element3");
        
        // Create second inner array with elements
        JSONArray innerArray2 = new JSONArray();
        innerArray2.put("element4");
        innerArray2.put("element5");
        innerArray2.put("element6");
        
        // Add inner arrays to outer array
        outerArray.put(innerArray1);
        outerArray.put(innerArray2);
        testJson.put("arrays", outerArray);
        
        JsonDot json = new JsonDot(testJson);
        System.out.println("Original JSON:");
        System.out.println(json.toPrettyString());
        
        // Get all elements from inner arrays using wildcard
        JsonArrayDot resultArray = json.getArray("arrays[*]");
        
        // Verify that we got all elements from both inner arrays
        assertEquals(6, resultArray.length());
        
        // Verify the elements are in the correct order
        assertEquals("element1", resultArray.get(0));
        assertEquals("element2", resultArray.get(1));
        assertEquals("element3", resultArray.get(2));
        assertEquals("element4", resultArray.get(3));
        assertEquals("element5", resultArray.get(4));
        assertEquals("element6", resultArray.get(5));
        
        System.out.println("\nResult after getting all elements:");
        System.out.println(resultArray.toPrettyString());
    }

    @Test
    public void testDiff() {
        System.out.println("\n=== Testing JSON Diff ===");
        
        // Create original JSON
        JsonDot original = new JsonDot("{\n" +
            "    \"user\": {\n" +
            "        \"name\": \"John\",\n" +
            "        \"age\": 30,\n" +
            "        \"address\": {\n" +
            "            \"city\": \"New York\",\n" +
            "            \"zip\": \"10001\"\n" +
            "        },\n" +
            "        \"hobbies\": [\"reading\", \"gaming\"]\n" +
            "    }\n" +
            "}");
        
        // Create modified JSON
        JsonDot modified = new JsonDot("{\n" +
            "    \"user\": {\n" +
            "        \"name\": \"John\",\n" +
            "        \"age\": 31,\n" +
            "        \"address\": {\n" +
            "            \"city\": \"Boston\",\n" +
            "            \"zip\": \"10001\"\n" +
            "        },\n" +
            "        \"hobbies\": [\"reading\", \"swimming\"],\n" +
            "        \"email\": \"john@example.com\"\n" +
            "    }\n" +
            "}");
        
        System.out.println("Original JSON:");
        System.out.println(original.toPrettyString());
        System.out.println("\nModified JSON:");
        System.out.println(modified.toPrettyString());
        
        // Get differences
        List<JsonUtils.JsonDiff> differences = original.diff(modified);
        
        System.out.println("\nDifferences found:");
        for (JsonUtils.JsonDiff diff : differences) {
            System.out.println(diff);
        }
        
        // Verify differences
        assertEquals(4, differences.size());
        
        // Check modified field
        JsonUtils.JsonDiff ageDiff = differences.stream()
            .filter(d -> d.getPath().equals("user.age"))
            .findFirst()
            .orElse(null);
        assertNotNull(ageDiff);
        assertEquals(JsonUtils.DiffType.MODIFIED, ageDiff.getType());
        assertEquals(30, ageDiff.getOldValue());
        assertEquals(31, ageDiff.getNewValue());
        
        // Check modified nested field
        JsonUtils.JsonDiff cityDiff = differences.stream()
            .filter(d -> d.getPath().equals("user.address.city"))
            .findFirst()
            .orElse(null);
        assertNotNull(cityDiff);
        assertEquals(JsonUtils.DiffType.MODIFIED, cityDiff.getType());
        assertEquals("New York", cityDiff.getOldValue());
        assertEquals("Boston", cityDiff.getNewValue());
        
        // Check added field
        JsonUtils.JsonDiff emailDiff = differences.stream()
            .filter(d -> d.getPath().equals("user.email"))
            .findFirst()
            .orElse(null);
        assertNotNull(emailDiff);
        assertEquals(JsonUtils.DiffType.ADDED, emailDiff.getType());
        assertEquals("john@example.com", emailDiff.getNewValue());

        // Check array element difference
        JsonUtils.JsonDiff hobbiesDiff = differences.stream()
            .filter(d -> d.getPath().equals("user.hobbies[1]"))
            .findFirst()
            .orElse(null);
        assertNotNull(hobbiesDiff);
        assertEquals(JsonUtils.DiffType.MODIFIED, hobbiesDiff.getType());
        assertEquals("gaming", hobbiesDiff.getOldValue());
        assertEquals("swimming", hobbiesDiff.getNewValue());
    }
} 