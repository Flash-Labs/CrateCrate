# Prizes

Prizes are individual things that can be given to a player, such as commands or
items. The available types are:

 - [CommandPrize](#CommandPrize): executes a command
 - [ItemPrizes](#ItemPrize): gives an item

## CommandPrize

A prize which executes a command when given. If the `command` node is present,
the prize is matched to this type.

```java
<id> {
    //name, lore, icon
    command {
        command = "/<command>"
        source = "server"/"player"
        online = true/false
    }
}
```

If only the `command` needs to be specified, prefer setting it directly which
will use the default values for the other properties.

```java
<id> {
    //name, lore, icon
    command = "/<command>"
}
```

If `name`/`lore`/`icon` are also unspecified, prefer using an inline
[reference](#Referencing) instead of defining a new prize.

### Properties

| Name | Type | Description | Default |
| --- | --- | --- | --- |
| `name` | Text | The prize name, inherited from Component | The command |
| `lore` | List<Text> | The prize lore, inherited from Component | An empty list |
| `icon` | Item | The prize icon, inherited from Component | A `filled_map` with the above `name`/`lore` |
| |
| `command` | String | The command, supporting placeholders for either `${player}` (if `online = true`) or `${user}` (if `online = false`) and `${value}` | Required |
| `source` | `"server"`/`"player"` | The source executing the command | `"server"` |
| `online` | Boolean | Whether the user must be online to receive this prize. If false, the `source` must be `"server"` (since offline users can't execute commands) | `true` if the source is `"player"`, else `false` |
| |
| `value` | String | The reference value, which replaces the `${value}` placeholder in the command | Required when `${value}` is used, otherwise is not allowed |

### Referencing

The reference value for a command prize is a value placeholder, which replaces
`${value}` in the command and is required when `${value}` is used (otherwise is
not allowed). Commands can also be defined inline using an object.

```java
prizes = [
    ["<command-prize-id>"] //no ${value}
    ["<command-prize-id>", "<value>"]
    {
        command = ...
    }
]
```

If only the `command` needs to be specified, prefer using an inline reference
instead of defining a new prize.

```java
prizes = [
    ["/<command>"]
]
```

### Examples

<details>
<summary>Greet (simple command)</summary>

Greets the user with a friendly message.

```java
greet {
    name = "Greet"
    lore = ["\"Greets the user\""]
    command = "/say Hello, ${user}"
}
```

```java
prizes = [
    ["greet"]
]
```
</details>

<details>
<summary>Me (player source, reference value)</summary>

Displays a `/me` action executed by the player

```java
me {
    name = "Me"
    lore = ["* ${value}"]
    command {
        command = "/me ${value}"
        source = "player"
    }
}
```

```java
prizes = [
    ["me", "rolls nat 20"]
]
```
</details>

<details>
<summary>Say (inline reference)</summary>

Says a message from the server.

> By `v0.1.0`, crates and rewards will have built-in broadcasts/messages which
> should be preferred over using commands.

```java
prizes = [
    ["/say The Server Speaks!"]
]
```
</details>

---

## ItemPrize

A prize which gives an item when given. If the `item` node is present, the prize
is matched to this type.

```java
<id> {
    //name, lore, icon
    item {
        type = "<item-type>"
        name = "name"
        lore = ["line1", "line2", ...]
        enchantments = [
            ["<enchantment-type>", <level>]
            ...
        ]
        nbt {
            <data>
        }
    }
}
```

If only the `type` needs to be specified, prefer setting it directly which will
use the default values for the other properties.

```java
<id> {
    //name, lore, icon
    item = "<item-type>"
}
```

If `name`/`lore`/`icon` are also unspecified, prefer using an inline
[reference](#Referencing-1) instead of defining a new prize.

### Properties

| Name | Type | Description | Default |
| --- | --- | --- | --- |
| `name` | Text | The prize name, inherited from Component | The `item`'s custom name if specified, else the item type |
| `lore` | List<Text> | The prize lore, inherited from Component | The `item`s lore if specified, else an empty list |
| `icon` | Item | The prize icon, inherited from Component | The `item` with the above `name`/`lore` if not specified |
| |
| `type` | String | The item type, in the form `<namespace>:<id>` | Required |
| `name` | Text | The item name, which applies to the item given to the user | The item type (default item name) |
| `lore` | List<Text> | The item lore, which applies to the item given to the user | An empty list |
| `enchantments` | List<[EnchantmentType, Integer]> | The item enchantments, which is a list of `["<enchantment-type>"`, `level]` pairs | An empty list |
| `nbt` | Object | The item nbt, which is arbitrary data (use with caution) | An empty map |
| |
| `quantity` | Integer | The reference value, which is the quantity of the item given | Required |

### Referencing

The reference value for an item prize is the `quantity`, which is required.
Items can also be defined inline using an object.

```java
prizes = [
    ["<item-prize-id>", <quantity>]
    {
        item = "item-type" | { ... }
        quantity = <quantity>
    }
]
```

If only the `type` and `quantity` need to be specified, prefer using an inline
reference instead of defining a new prize.

```java
prizes = [
    ["<item-type>", <quantity>]
]
```

### Examples

<details>
<summary>Apple (simple item)</summary>

Gives an apple to the user. The prize itself also has a custom display name and
lore, which is used in text/menus but does not apply to the item given.

```java
apple {
    name = "&cApple"
    lore = ["&7An apple a day keeps the doctor away"]
    item = "minecraft:apple"
}
```

```java
prizes = [
    ["apple", 1]
]
```
</details>

<details>
<summary>Monado (name/lore/enchantments)</summary>

Gives the player a [powerful sword](https://xenoblade.fandom.com/wiki/Monado).

```java
monado {
    item {
        type = "minecraft:diamond_sword"
        name = "&bMonado"
        lore = ["&f\"Today, we use our power to fell a god...\""]
        enchantments = [
            ["minecraft:sharpness", 10]
            ["minecraft:fortune", 1]
        ]
    }
}
```

```java
prizes = [
    ["monado", 1]
]
```
</details>

<details>
<summary>Cookie (inline reference)</summary>

Just a normal cookie.

```java
prizes = [
    ["minecaft:cookie", 3]
]
```
</details>

---
