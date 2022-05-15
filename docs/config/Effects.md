# Effects

Effects are things that are triggered by actions such as opening a crate. The
available types are:

 - [FireworkEffect](#FireworkEffect)
 - [PotionEffect](#PotionEffect)
 - [SoundEffect](#SoundEffect)

## FireworkEffect

An effect that launches a firework when given.

- **WARNING**: Firework explosions can damage players in survival - you should
  ensure the user is protected from damage and/or spawn the firework above the
  crate so the player is not in range of the explosion.

```java
<id> = {
    firework = {
        shape = "<firework-shape>"
        colors = [<RGB Color>...]
        fades = [<RGB Color>...]
        trail = true/false
        flicker = true/false
        duration = <Integer>
    }
}
```

If only the `shape` needs to be specified, prefer using an inline
[reference](#Referencing) instead of defining a new effect.

### Properties

| Name | Type | Description | Default |
| --- | --- | --- | --- |
| `shape` | FireworkShape | The shape of the firework (`"ball"`/`"large_ball"`/`"burst"`/`"star"`/`"creeper"`) | Random |
| `colors` | List<Integer> | The colors the firework has initially | Random (1-2 colors) |
| `fades` | List<Integer> | The colors the firework fades into | Random (0-1 colors) |
| `trail` | Boolean | Whether the firework leaves a trail | Random |
| `flicker` | Boolean | Whether the firework has a flicker | Random |
| `duration` | Integer | The duration of the firework flight | Random (1-3) |
| |
| `target` | `"player"`/`"location"` | The first part of the reference value, whether the initial position is at the player or crate location | `"location"` |
| `offset` | Vector3d | The second part of the reference value, the xyz offset from the initial position | `[0.0, 0.0, 0.0]` |

### Referencing

The reference value of a firework is its spawn position, which consists of a
`target` (the initial position) and `offset` (xyz offset for the position):

```java
effects = [
    ["<firework-id>"]
    ["<firework-id>", "player"]
    ["<firework-id>", 0.0, 2.0, 0.0]
    ["<firework-id>", "player", 0.0, 2.0, 0.0]
    {
        firework = ...
        target = "player"/"location"
        offset = [x, y, z]
    }
]
```

If only the `shape` needs to be specified, prefer an inline reference instead
of defining a new effect. Additionally, `"firework"` can be used for a random
shape.

```java
effects = [
    ["firework"] //random shape
    ["firework/<shape>"]
]
```

### Examples

<details>
<summary>Green Creeper</summary>

A green creeper firework with a random `trail`/`flicker`/`duration`.

```java
green-creeper = {
    firework = {
        shape = "creeper"
        colors = [0x00FF00]
        fades = []
    }
}
```
</details>

<details>
<summary>Random Burst (inline reference)</summary>

A burst firework with random colors, fades, etc. that spawns above the player.

```java
effects = [
    ["firework/burst", "player", 0.0, 2.0, 0.0]
]
```
</details>

---

## PotionEffect

An effect that gives the player a potion effect when given.

```java
<id> = {
    potion = {
        type = "<potion-type>/<strength>"
        ambient = true/false
        particles = true/false
    }
}
```

If only the `type` needs to be specified, prefer using an inline
[reference](#Referencing-1) instead of defining a new effect.

### Properties

| Name | Type | Description | Default |
| --- | --- | --- | --- |
| `type` | PotionType | The potion type, which consists of an effect id and optionally a strength (ex. `minecraft:regeneration/2` for Regeneration II) | Required |
| `ambient` | Boolean | Whether the effect is ambient, meaning it is displayed less prominently (normally used for beacon effects) | `false` |
| `particles` | Boolean | Whether the effect shows particles | `true` |
| |
| `duration` | Integer | The reference value, which is the duration of the effect in seconds | Required |

### Referencing

The reference value of a potion is its duration, which is in seconds.

```java
effects = [
    ["<potion-id>", 10]
    {
        potion = ...
        duration = Integer
    }
]
```

If only the `type` needs to be specified, prefer an inline reference instead of
defining a new effect.

```java
effects = [
    ["minecraft:regeneration/2"]
]
```

### Examples

<details>
<summary>Secret Night Vision</summary>

A Night Vision effect that is ambient and hides particles.

```java
secret-night-vision = {
    potion = {
        type = "minecraft:night_vision"
        ambient = true
        particles = false
    }
}
```
</details>

<details>
<summary>Regeneration II (inline reference)</summary>

A Regeneration II effect that lasts `10` seconds.

```java
effects = [
    ["minecraft:regeneration/2", 10]
]
```
</details>

---

## SoundEffect

An effect that plays a sound when given.

```java
<id> = {
    sound = {
        type = "<sound-type>"
        volume = <Decimal>
        pitch = <Decimal>
    }
}
```

If only the `type` needs to be specified, prefer using an inline
[reference](#Referencing-2) instead of defining a new effect.

### Properties

| Name | Type | Description | Default |
| --- | --- | --- | --- |
| `type` | SoundType | The type of the sound | Required |
| `volume` | Decimal | The volume of the sound | `1.0` |
| `pitch` | Decimal | The pitch of the sound | `1.0` |
| |
| `target` | `"player"`/`"location"` | The first part of the reference value, whether the initial position is at the player or crate location | `"location"` |
| `offset` | Vector3d | The second part of the reference value, the xyz offset from the initial position | `[0.0, 0.0, 0.0]` |

### Referencing

The reference value of a sound is its position, which consists of a `target`
(the initial position) and `offset` (xyz offset for the position):

```java
effects = [
    ["<sound-id>"]
    ["<sound-id>", "player"]
    ["<sound-id>", 0.0, 2.0, 0.0]
    ["<sound-id>", "player", 0.0, 2.0, 0.0]
    {
        sound = ...
        target = "player"/"location"
        offset = [x, y, z]
    }
]
```

If only the `type` needs to be specified, prefer an inline reference instead of
defining a new effect.

```java
effects = [
    ["minecraft:block_note_bell"]
]
```

### Examples

<details>
<summary>Loud Explosion</summary>

An explosion sound that plays twice as loud.

```java
loud-explosion = {
    sound = {
        type = "minecraft:entity_generic_explode"
        volume = 2.0
    }
}
```
</details>

<details>
<summary>Bell Note (inline reference)</summary>

A bell sound from a note block that plays above the player.

```java
effects = [
    ["minecraft:block_note_bell", "player", 0.0, 2.0, 0.0]
]
```
</details>

---
