# Crates

Crates define everything necessary to open a crate, such as keys and rewards.

```java
<id> = {
    //name, lore, icon
    keys = [
        <key-reference>
        ...
    ]
    rewards = [
        <reward-reference>
        ...
    ]
}
```

### Properties

| Name | Type | Description | Default |
| --- | --- | --- | --- |
| `name` | Text | The crate name, inherited from Component | This crate's capitalized `id` |
| `lore` | List<Text> | The crate lore, inherited from Component | An empty list |
| `icon` | Item | The crate icon, inherited from Component | A `chest` with the above `name`/`lore` |
| |
| `keys` | List<KeyReference> | The keys for this crate | An empty list |
| `rewards` | List<RewardReference> | The rewards for this crate | Required |

### Examples

<details>
<summary>Example</summary>

A crate containing the example key and rewards.

```java
example = {
    name = "&eExample Crate"
    lore = ["&6An example crate"]
    keys = [
        ["example", 1]
    ]
    rewards = [
        ["command-prizes", 50.0]
        ["item-prizes", 50.0]
    ]
}
```

</details>

---
