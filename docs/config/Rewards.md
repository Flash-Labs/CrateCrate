# Rewards

Rewards are things which can be received from a crate, which includes one or
more prizes as well as additional properties to customize how the reward
interacts with a crate (such as messages or restrictions).

```java
<id> = {
    //name, lore, icon
    prizes = [
        <prize-reference>
        ...
    ]
}
```

### Properties

| Name | Type | Description | Default |
| --- | --- | --- | --- |
| `name` | Text | The reward name, inherited from Component | The `name` of the first prize (if only one prize exists) or this reward's `id` (if multiple prizes exist) |
| `lore` | List<Text> | The reward lore, inherited from Component | The `lore` of the first prize (if only one prize exists) or the `name`s of all prizes (if multiple prizes exist) |
| `icon` | Item | The reward icon, inherited from Component | The `icon` of the first prize (if only one prize exists) or a `book` with the above `name`/`lore` (if multiple prizes exist) |
| |
| `prizes` | List<PrizeReference> | The prizes for this reward | Required |
| |
| `weight` | Integer | The reference value, which is the likelihood of received the reward in a crate | Required |

### Referencing

The reference value for a reward is the `weight`, which is the likelihood of
receiving the reward in a crate and is required. Rewards can also be defined
inline using an object.

```java
rewards = [
    ["<reward-id>", <weight>]
    {
        prizes = ...
        weight = <weight>
    }
]
```

If the reward contains only one prize, prefer using a reference for that prize
directly as shown below. In addition to the reference value for the prize, the
rewards `weight` must also be provided.

```java
rewards = [
    //CommandPrize
    ["/<command>", <weight>]
    ["<command-prize-id>", <weight>]
    ["<command-prize-id>", "<value>", <weight>]
    {
        command = ...
        weight = <weight>
    }
    //ItemPrize
    ["<item-type>", <quantity>, <weight>]
    ["<item-prize-id>", <quantity>, <weight>]
    {
        item = ...
        quantity = <quantity>
        weight = <weight>
    }
]
```

### Examples

<details>
<summary>CommandPrizes (referencing commands)</summary>

A reward containing the example CommandPrizes.

```java
command-prizes = {
    prizes = [
        ["greet"]
        ["me", "rolls nat 20"]
        ["/say The Server Speaks!"]
    ]
}
```
</details>

<details>
<summary>ItemPrizes (referencing items)</summary>

A reward containing the example ItemPrizes.

```java
item-prizes = {
    prizes = [
        ["apple", 1]
        ["monado", 1]
        ["minecraft:cookie", 3]
    ]
}
```
</details>

---

<details>
<summary>MoneyPrizes (referencing currencies)</summary>

A reward containing the example MoneyPrizes. Since this requires an economy
plugin, it is not included in the default config.

```java
money-prizes = {
    prizes = [
        ["tokens", 100]
        ["$", 250]
    ]
}
```
</details>
