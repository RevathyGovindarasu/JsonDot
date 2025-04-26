package tech.coderscamp.jsondot;

import org.json.JSONException;
import java.io.IOException;

public class JsonDotExample {
    public static void main(String[] args) {
        try {
            // Create a new JSON object
            JsonDot json = new JsonDot();
            
            // Add elements using dot notation
            json.addElement("user.name", "John Doe")
                .addElement("user.age", 30)
                .addElement("user.address.street", "123 Main St")
                .addElement("user.address.city", "New York")
                .addElement("user.hobbies", new String[]{"reading", "gaming", "coding"});
            
            // Print the JSON object
            System.out.println("Created JSON object:");
            System.out.println(json.toPrettyString());
            
            // Get values using dot notation
            System.out.println("\nGetting values:");
            System.out.println("Name: " + json.getElement("user.name"));
            System.out.println("Age: " + json.getElement("user.age"));
            
            // Get nested objects
            JsonDot address = json.getObject("user.address");
            System.out.println("\nAddress object:");
            System.out.println(address.toPrettyString());
            
            // Get array
            JsonArrayDot hobbies = json.getArray("user.hobbies");
            System.out.println("\nHobbies array:");
            System.out.println(hobbies.toPrettyString());
            
            // Check if path exists
            System.out.println("\nPath checks:");
            System.out.println("Has user.name: " + json.hasPath("user.name"));
            System.out.println("Has user.email: " + json.hasPath("user.email"));
            
            // Remove an element
            json.removeElementFromPath("user.age");
            System.out.println("\nAfter removing age:");
            System.out.println(json.toPrettyString());
            
            // Create another JSON object
            JsonDot json2 = new JsonDot();
            json2.addElement("user.email", "john@example.com")
                 .addElement("user.phone", "123-456-7890");
            
            // Merge JSON objects
            JsonDot merged = JsonUtils.merge(json, json2);
            System.out.println("\nMerged JSON:");
            System.out.println(merged.toPrettyString());
            
            // Write to file
            JsonUtils.writeJsonFile(merged, "user.json", true);
            System.out.println("\nJSON written to user.json");
            
            // Read from file
            JsonDot readJson = JsonUtils.readJsonFile("user.json");
            System.out.println("\nRead from file:");
            System.out.println(readJson.toPrettyString());
            
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }
} 