# Keys

Keys are things that are taken from a user when opening a crate. There is
currently only one key type:

 - [StandardKey](#StandardKey)

## StandardKey

A key that is tracked virtually for each user.

```java
<id> {
    //name, lore, icon
}
```

### Properties

| Name | Type | Description | Default |
| --- | --- | --- | --- |
| `name` | Text | The key name, inherited from Component | This key's `id` |
| `lore` | List<Text> | The key lore, inherited from Component | An empty list |
| `icon` | Item | The key icon, inherited from Component | A `tripwire_hook` with the above `name`/`lore` |
| |
| `quantity` | Integer | The reference value, which is the number of keys needed for that crate | Required |

### Referencing

The reference value of a key is the `quantity`, which is the number of keys
needed for that crate and is required. Keys can also be defined inline using an
object.

```java
keys = [
    ["<key-id>", <quantity>]
    {
        quantity = <quantity>
    }
]
```

Keys can also be defined inline, though it's recommended to define keys
separately to customize `name`/`lore`/`icon` as needed.

```java
keys = [
    ["<new-key-id>", <quantity>]
]
```

### Examples

<details>
<summary>Example (name/lore/icon)</summary>

A standard key with `name`/`lore`/`icon`. Though the default icon is a
`tripwire_hook`, this adds an enchantment effect for style points.

```java
example {
    name = "&eExample Key"
    lore = ["&6An example key"]
    icon = {
        type = "minecraft:tripwire_hook"
        enchantments = [
            ["minecraft:fortune", 1]
        ]
    }
}
```
</details>

<details>
<summary>Example (inline reference)</summary>

A key with id `example` defined inline.

> It's recommended to define keys separately to customize `name`/`lore`/`icon`
> as needed, but this example is included here for completion.

```java
keys = [
    ["example", 1]
]
```
</details>

---
