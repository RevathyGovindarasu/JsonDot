package com.rgov.jsondot;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

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
} 