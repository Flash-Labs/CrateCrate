# Crates

Crates define everything necessary to open a crate, such as keys, rewards, and
effects.

```java
<id> = {
    //name, lore, icon
    keys = [
        <key-reference>
        ...
    ]
    opener = undefined/"gui"/"roulette"
    effects = {
        idle = [
            <effect-reference>
        ]
        ...
    }
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
| `opener` | undefined/`"gui"`/`"roulette"` | The opener for this crate | undefined (none) |
| `effects` | Map<Action, List<EffectReference | The effects for each action of this crate (`idle`/`open`/`give`/`reject`/`preview`) | An empty map |
| `rewards` | List<RewardReference> | The rewards for this crate | Required |

### Examples

<details>
<summary>Example</summary>

A crate containing the example key, rewards, and some effects.

```java
example = {
    name = "&eExample Crate"
    lore = ["&6An example crate"]
    keys = [
        ["example", 1]
    ]
    opener = "gui"
    effects = {
        idle = [
            ["rainbow-helix"]
        ]
        give = [
            ["green-creeper"]
        ]
    }
    rewards = [
        ["command-prizes", 50.0]
        ["item-prizes", 50.0]
    ]
}
```

</details>

---
