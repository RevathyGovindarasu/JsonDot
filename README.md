# JsonDot

[![Maven Central](https://img.shields.io/maven-central/v/org.coderscamp/jsondot.svg)](https://search.maven.org/artifact/org.coderscamp/jsondot)
[![Java Version](https://img.shields.io/badge/java-11-blue.svg)](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)
[![Build Status](https://img.shields.io/github/actions/workflow/status/coderscamp/jsondot/build.yml?branch=main)](https://github.com/coderscamp/jsondot/actions)
[![Code Coverage](https://img.shields.io/codecov/c/github/coderscamp/jsondot)](https://codecov.io/gh/coderscamp/jsondot)
[![Documentation](https://img.shields.io/badge/docs-latest-brightgreen)](https://coderscamp.github.io/jsondot/)

**JsonDot** is a powerful Java library for JSON manipulation with dot notation.

## Features

- Dot notation path access
- Array support with index and wildcard
- Type-safe accessors
- Deep merge functionality
- XML conversion
- Path validation
- Array filtering

## Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>org.coderscamp.jsondot</groupId>
    <artifactId>jsondot</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Basic Usage

```java
JsonDot jsonDot = new JsonDot();
JSONObject json = new JSONObject("{\"user\":{\"name\":\"John\"}}");

// Get value
String name = jsonDot.get(json, "user.name", String.class);

// Set value
jsonDot.set(json, "user.age", 30);

// Add element
jsonDot.addElement(json, "user.hobbies", "reading");

// Remove element
jsonDot.removeElement(json, "user.name");
```

## Advanced Features

### Deep Merge

Merge two JSON objects while preserving nested structures:

```java
JSONObject target = new JSONObject("{\"user\":{\"name\":\"John\",\"age\":30}}");
JSONObject source = new JSONObject("{\"user\":{\"age\":31,\"city\":\"NY\"}}");

JSONObject merged = JsonUtils.deepMerge(target, source);
// Result: {"user":{"name":"John","age":31,"city":"NY"}}
```

Features of deep merge:
- Preserves existing values unless explicitly overwritten
- Merges arrays by combining their elements
- Handles nested objects recursively
- Preserves null values when specified

### XML Conversion

Convert between JSON and XML:

```java
// JSON to XML
JSONObject json = new JSONObject("{\"user\":{\"name\":\"John\"}}");
String xml = JsonUtils.toXml(json);
// Result: <user><name>John</name></user>

// XML to JSON
String xml = "<user><name>John</name></user>";
JSONObject json = JsonUtils.fromXml(xml);
// Result: {"user":{"name":"John"}}
```

### JSON Validation

Check if a string is valid JSON:

```java
boolean isValid = JsonUtils.isValidJson("{\"key\":\"value\"}"); // true
boolean isValid = JsonUtils.isValidJson("{invalid}"); // false
```

## Array Operations

### Accessing Array Elements

```java
JSONObject json = new JSONObject("{\"users\":[{\"name\":\"John\"},{\"name\":\"Jane\"}]}");

// Get specific element
String name = jsonDot.get(json, "users[0].name", String.class); // "John"

// Get all elements using wildcard
List<String> names = jsonDot.get(json, "users[*].name", List.class); // ["John", "Jane"]
```

### Filtering Arrays

```java
JSONObject json = new JSONObject("""
    {
        "products": [
            {"id": 1, "price": 100},
            {"id": 2, "price": 200}
        ]
    }
    """);

// Filter products with price > 150
List<JSONObject> expensiveProducts = jsonDot.filterArray(
    json, 
    "products", 
    product -> product.getInt("price") > 150
);
```

## Best Practices

1. **Path Validation**
   - Always validate paths before operations
   - Use `isValidPath` to check path syntax
   - Handle `JsonDotException` for invalid paths

2. **Type Safety**
   - Use type-safe accessors (`getAsString`, `getAsInt`, etc.)
   - Specify expected return types
   - Handle type conversion exceptions

3. **Performance**
   - Cache frequently accessed paths
   - Use bulk operations for multiple updates
   - Consider using `JsonArrayDot` for array-heavy operations

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.