# JsonDot

[![Maven Central](https://img.shields.io/maven-central/v/com.revathygovindarasu/jsondot.svg)](https://search.maven.org/artifact/com.revathygovindarasu/jsondot)
[![Java Version](https://img.shields.io/badge/java-11-blue.svg)](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)
[![Build Status](https://img.shields.io/github/actions/workflow/status/revathygovindarasu/jsondot/build.yml?branch=main)](https://github.com/revathygovindarasu/jsondot/actions)

**JsonDot** is a lightweight, developer-friendly Java library for effortlessly accessing, manipulating, and querying JSON data using dot notation, wildcard queries, intelligent path suggestions, deep merging, and more.

## Requirements

- Java 11 or higher
- Maven 3.6 or higher (for building from source)

## Table of Contents

- [What's New](#whats-new)
- [Features](#features)
- [Installation](#installation)
- [Usage Examples](#usage-examples)
- [Error Handling](#error-handling)
- [Working with Arrays](#working-with-arrays)
- [File Operations](#file-operations)
- [Merging JSON Objects](#merging-json-objects)
- [XML Conversion](#xml-conversion)
- [Deep Merge](#deep-merge)
- [API Reference](#api-reference)
- [Contributing](#contributing)
- [Roadmap](#roadmap)
- [License](#license)

## What's New

### Path Suggestions
Receive intelligent suggestions for incomplete paths:
```java
JsonDot json = new JsonDot("{\"user\":{\"address\":{\"city\":\"New York\"}}}");
List<String> suggestions = json.suggestPaths("user.adres");
// Returns: List<String> containing ["user.address"]
```

### Wildcard Queries
Query JSON using wildcard patterns to match multiple paths:
```java
JsonDot json = new JsonDot("{\"users\":[{\"name\":\"John\",\"city\":\"NY\"},{\"name\":\"Jane\",\"city\":\"LA\"}]}");
List<Object> cities = json.query("users.*.city");
// Returns: List<Object> containing ["NY", "LA"]
```

### Conditional Array Filters
Filter arrays based on conditions:
```java
JsonDot json = new JsonDot("{\"users\":[{\"name\":\"John\",\"age\":25},{\"name\":\"Jane\",\"age\":30}]}");
JsonArrayDot usersOver25 = json.filterArray("users", user -> (Integer)user.getElement("age") > 25);
// Returns: JsonArrayDot containing [{"name":"Jane","age":30}]
```

### JSON Diff
Compare two JSON objects and get their differences:
```java
JsonDot json1 = new JsonDot("{\"name\":\"John\",\"age\":30}");
JsonDot json2 = new JsonDot("{\"name\":\"John\",\"age\":31,\"city\":\"NY\"}");
List<JsonDiff> differences = JsonUtils.diff(json1, json2);
// Returns: List<JsonDiff> containing [
//   Modified: age: 30 -> 31
//   Added: city = NY
// ]
```

## Features

- Dot notation access to JSON objects (e.g., "user.address.city")
- Intelligent path auto-suggestions and wildcard queries
- Array operations with index-based access
- Conditional filtering of arrays
- JSON diff and comparison
- File operations (read/write JSON files)
- JSON validation
- JSON merging with customizable strategies
- Pretty printing
- Method chaining support
- Lightweight and zero external dependencies
- Designed for performance and minimal memory footprint

## Installation

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.revathygovindarasu</groupId>
    <artifactId>jsondot</artifactId>
    <version>1.0-SNAPSHOT</version> <!-- Coming soon: stable 1.0 release! -->
</dependency>
```

## Usage Examples

### Basic JSON Operations

```java
// Create a new JSON object
JsonDot json = new JsonDot();

// Add elements using dot notation
json.addElement("user.name", "John Doe")
    .addElement("user.age", 30)
    .addElement("user.address.city", "New York");

// Get values
String name = (String) json.getElement("user.name");
Integer age = (Integer) json.getElement("user.age");

// Check if path exists
boolean hasName = json.hasPath("user.name");

// Remove an element
json.removeElement("user.age");

// Pretty print
System.out.println(json.toPrettyString());
```

### Error Handling

```java
try {
    // Attempt to access a non-existent path
    json.getElement("nonexistent.path");
} catch (JSONException e) {
    System.err.println("Path not found: " + e.getMessage());
}

try {
    // Attempt to add an invalid value
    json.addElement("user.age", new Object()); // Not JSON-serializable
} catch (JSONException e) {
    System.err.println("Invalid value: " + e.getMessage());
}

// Safe path checking
if (json.hasPath("user.address.city")) {
    String city = (String) json.getElement("user.address.city");
} else {
    System.out.println("City not found");
}
```

### Working with Arrays

```java
// Add an array of hobbies
json.addElement("user.hobbies", new String[]{"reading", "gaming", "coding"});

// Get array
JsonArrayDot hobbies = json.getArray("user.hobbies");

// Add to array
hobbies.add("swimming");

// Get first hobby from the array
String firstHobby = (String) hobbies.get(0);

// Remove array element
hobbies.remove(1);
```

### File Operations

```java
// Write to file (pretty print optional)
JsonUtils.writeJsonFile(json, "user.json", true);

// Read from file
JsonDot readJson = JsonUtils.readJsonFile("user.json");
```

### Merging JSON Objects

```java
JsonDot json1 = new JsonDot();
json1.addElement("user.name", "John");

JsonDot json2 = new JsonDot();
json2.addElement("user.age", 30);

// Merge JSON objects
JsonDot merged = JsonUtils.merge(json1, json2);
```

### XML Conversion

```java
// Convert JSON to XML
String xml = JsonUtils.toXml(json);

// Convert XML to JSON
JsonDot jsonFromXml = JsonUtils.fromXml(xml);

// Write JSON as XML file
JsonUtils.writeXmlFile(json, "user.xml");

// Read XML file as JSON
JsonDot jsonFromXmlFile = JsonUtils.readXmlFile("user.xml");
```

## Deep Merge

The library provides powerful deep merge capabilities for JSON objects with customizable array merge strategies. If no array merge strategy is specified, the default is APPEND.

### Basic Deep Merge

```java
JsonDot json1 = new JsonDot("{\"name\":\"John\",\"age\":30,\"address\":{\"city\":\"New York\"}}");
JsonDot json2 = new JsonDot("{\"age\":31,\"address\":{\"country\":\"USA\"}}");

JsonDot merged = JsonUtils.deepMerge(json1, json2);
// Result: {"name":"John","age":31,"address":{"city":"New York","country":"USA"}}
```

### Advanced Deep Merge with Array Strategies

```java
JsonDot json1 = new JsonDot("{\"items\":[{\"id\":1,\"value\":\"a\"},{\"id\":2,\"value\":\"b\"}]}");
JsonDot json2 = new JsonDot("{\"items\":[{\"id\":1,\"value\":\"A\"},{\"id\":3,\"value\":\"c\"}]}");

// Append strategy - adds all elements from json2 to json1
JsonDot appended = JsonUtils.deepMerge(json1, json2, JsonUtils.ArrayMergeStrategy.APPEND);
// Result: {"items":[{"id":1,"value":"a"},{"id":2,"value":"b"},{"id":1,"value":"A"},{"id":3,"value":"c"}]}

// Overwrite strategy - replaces json1's array with json2's array
JsonDot overwritten = JsonUtils.deepMerge(json1, json2, JsonUtils.ArrayMergeStrategy.OVERWRITE);
// Result: {"items":[{"id":1,"value":"A"},{"id":3,"value":"c"}]}

// Merge strategy - merges arrays by matching indices and recursively merging objects
JsonDot merged = JsonUtils.deepMerge(json1, json2, JsonUtils.ArrayMergeStrategy.MERGE);
// Result: {"items":[{"id":1,"value":"A"},{"id":2,"value":"b"},{"id":3,"value":"c"}]}
```

The merge strategy is particularly useful when dealing with arrays of objects, as it will recursively merge objects at matching indices while preserving the structure of both arrays.

## API Reference

For detailed API documentation, please refer to our [Javadoc](https://revathygovindarasu.github.io/jsondot/javadoc/).

<details>
<summary>JsonDot Class Methods</summary>

| Method | Description |
|--------|-------------|
| `addElement(String path, Object value)` | Adds a value at a specified dot-notation path |
| `removeElement(String path)` | Removes an element at a specified dot-notation path |
| `getElement(String path)` | Gets a value at a specified dot-notation path |
| `getObject(String path)` | Gets a JSON object at a specified dot-notation path |
| `getArray(String path)` | Gets a JSON array at a specified dot-notation path |
| `hasPath(String path)` | Checks if a path exists in the JSON object |
| `getKeys()` | Gets all keys at the root level |
| `toString()` | Converts to string |
| `toPrettyString()` | Converts to pretty-printed string |
| `suggestPaths(String partialPath)` | Gets path suggestions for a partial path |
| `query(String pattern)` | Queries JSON using wildcard patterns |
| `filterArray(String path, Predicate<JsonDot> condition)` | Filters an array based on a condition |

</details>

<details>
<summary>JsonArrayDot Class Methods</summary>

| Method | Description |
|--------|-------------|
| `add(Object value)` | Adds an element to the end of the array |
| `add(int index, Object value)` | Adds an element at a specific index |
| `get(int index)` | Gets an element at a specific index |
| `getObject(int index)` | Gets a JSON object at a specific index |
| `getArray(int index)` | Gets a JSON array at a specific index |
| `remove(int index)` | Removes an element at a specific index |
| `length()` | Gets the number of elements |
| `toString()` | Converts to string |
| `toPrettyString()` | Converts to pretty-printed string |

</details>

<details>
<summary>JsonUtils Class Methods</summary>

| Method | Description |
|--------|-------------|
| `readJsonFile(String filePath)` | Reads a JSON file |
| `writeJsonFile(JsonDot jsonDot, String filePath, boolean prettyPrint)` | Writes to a JSON file |
| `merge(JsonDot json1, JsonDot json2)` | Merges two JSON objects |
| `deepMerge(JsonDot json1, JsonDot json2)` | Deep merges two JSON objects |
| `deepMerge(JsonDot json1, JsonDot json2, ArrayMergeStrategy strategy)` | Deep merges with custom array strategy |
| `isValidJson(String jsonString)` | Validates JSON string |
| `toArray(JsonDot jsonDot)` | Converts object to array |
| `getKeys(JsonDot jsonDot)` | Gets all keys |
| `hasKey(JsonDot jsonDot, String key)` | Checks if key exists |
| `toXml(JsonDot jsonDot)` | Converts JSON to XML string |
| `fromXml(String xmlString)` | Converts XML string to JSON |
| `writeXmlFile(JsonDot jsonDot, String filePath)` | Writes JSON as XML file |
| `readXmlFile(String filePath)` | Reads XML file as JSON |
| `diff(JsonDot json1, JsonDot json2)` | Compares two JSON objects and returns differences |

</details>

## Contributing

We welcome contributions! To get started:

1. Fork the repository
2. Clone your fork: `git clone https://github.com/your-username/jsondot.git`
3. Create a new branch: `git checkout -b feature/your-feature-name`
4. Make your changes
5. Run tests: `mvn clean test`
6. Commit your changes: `git commit -m 'Add your feature'`
7. Push to your fork: `git push origin feature/your-feature-name`
8. Finally, open a pull request 🚀

## Roadmap

Future enhancements planned for JsonDot:

### JSON Schema Validation
- Support for validating JSON against schemas
- Custom validation rules and constraints
- Schema generation from existing JSON

### Performance Optimizations
- Path caching for frequently accessed nodes
- Streaming JSON parsing for large files
- Memory-efficient operations
- Performance benchmarks and metrics

### Additional Features
- JSON Patch Operations
- Versioned JSON with history tracking
- Template-based insertion
- Built-in encryption for sensitive data
- Advanced query language
- GraphQL-like querying capabilities

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.