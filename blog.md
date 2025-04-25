<groupId>com.rgov.jsondot</groupId>

implementation 'com.rgov.jsondot:jsondot:1.0.0'

import com.rgov.jsondot.JsonDot;
import com.rgov.jsondot.JsonArrayDot; 

# JsonDot: A Powerful Java Library for JSON Manipulation

## Introduction

JsonDot is a modern Java library that simplifies JSON manipulation using dot notation. It provides an intuitive API for accessing, modifying, and transforming JSON data with features like deep merging, XML conversion, and array operations.

## Key Features

### 1. Dot Notation Path Access
```java
JsonDot jsonDot = new JsonDot();
JSONObject json = new JSONObject("{\"user\":{\"name\":\"John\"}}");
String name = jsonDot.get(json, "user.name", String.class);
```

### 2. Array Support
- Index-based access: `users[0].name`
- Wildcard support: `users[*].name`
- Array filtering and manipulation

### 3. Utility Methods

#### Deep Merge
Merge JSON objects while preserving nested structures:
```java
JSONObject target = new JSONObject("{\"user\":{\"name\":\"John\",\"age\":30}}");
JSONObject source = new JSONObject("{\"user\":{\"age\":31,\"city\":\"NY\"}}");
JSONObject merged = JsonUtils.deepMerge(target, source);
```

Features:
- Preserves existing values
- Merges arrays intelligently
- Handles nested objects
- Preserves null values

#### XML Conversion
```java
// JSON to XML
String xml = JsonUtils.toXml(json);

// XML to JSON
JSONObject json = JsonUtils.fromXml(xml);
```

#### JSON Validation
```java
boolean isValid = JsonUtils.isValidJson("{\"key\":\"value\"}");
```

## Common Use Cases

### 1. Configuration Management
```java
JsonDot config = new JsonDot(configFile);
String dbUrl = config.getString("database.url");
int maxConnections = config.getInt("database.maxConnections");
```

### 2. API Response Handling
```java
JsonDot response = new JsonDot(apiResponse);
List<Object> items = response.query("data.items.*");
```

### 3. Data Transformation
```java
JsonDot source = new JsonDot(sourceData);
JsonDot target = new JsonDot();
target.addElement("user.name", source.getString("name"))
      .addElement("user.age", source.getInt("age"));
```

## Advanced Features

### Path Validation
```java
if (jsonDot.isValidPath("user.address.city")) {
    String city = jsonDot.get(json, "user.address.city", String.class);
}
```

### Array Filtering
```java
List<JSONObject> filtered = jsonDot.filterArray(
    json, 
    "products", 
    product -> product.getInt("price") > 100
);
```

### Type-Safe Accessors
```java
String name = jsonDot.getAsString(json, "user.name");
int age = jsonDot.getAsInt(json, "user.age");
boolean active = jsonDot.getAsBoolean(json, "user.active");
```

## Best Practices

1. **Path Validation**
   - Always validate paths before operations
   - Use `isValidPath` to check path syntax
   - Handle `JsonDotException` for invalid paths

2. **Type Safety**
   - Use type-safe accessors
   - Specify expected return types
   - Handle type conversion exceptions

3. **Performance**
   - Cache frequently accessed paths
   - Use bulk operations for multiple updates
   - Consider using `JsonArrayDot` for array-heavy operations

## Performance Considerations

- Memory-efficient design
- Optimized path traversal
- Efficient array operations
- Comparable performance to other libraries

## Conclusion

JsonDot provides a powerful and intuitive way to work with JSON data in Java. Its dot notation syntax, utility methods, and array support make it an excellent choice for modern Java applications.

## Getting Started

Add the dependency to your `pom.xml`:
```xml
<dependency>
    <groupId>com.rgov.jsondot</groupId>
    <artifactId>jsondot</artifactId>
    <version>1.0.0</version>
</dependency>
```

For more information, visit the [GitHub repository](https://github.com/rgov/jsondot). 