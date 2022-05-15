# Config

The config for CrateCrate can be tricky to understand. This file addresses the
main concepts, and the reference docs below contain more details and examples
for individual components.

 - `cratecrate.conf`: The main plugin config (currently unused)
 - [`config/crates.conf`](Crates.md)
 - [`config/effects.conf`](Effects.md)
 - [`config/keys.conf`](Keys.md)
 - [`config/prizes.conf`](Prizes.md)
 - [`config/rewards.conf`](Rewards.md)

## Overview

Configuring crates can be difficult, time-consuming, and require a lot of
duplicate work in the config. Compared to other crate plugins, CrateCrate
emphasizes a configuration workflow that promotes reuseability and natural
defaults, which can take some time to get used to.

The main thing to understand is the different between prizes and rewards:

 - A *prize* is something that can be given to a player and includes commands,
   items and money. The same prize can be used in different rewards.
 - A *reward* is something the player can win from a crate and contains
   *multiple prizes*. Rewards also account for other opening-related features
   like broadcast messages, effects, and player-specific behavior. The same
   reward can be used in different crates.

The config also supports a feature called *referencing*, which allows reusing
the same component in a slightly different manner. Items, for example, use the
quantity as a *reference value* - the example below shows how this can reuse the
`apple` prize with different quantities, thus configuring `apple` only once in
`prizes.conf`.

```java
prizes = [
    ["apple", 1]
    ["apple", 4]
]
```

Finally, an *inline reference* can be used to easily create a prize instead of
defining it in `prizes.conf`. Building on the previous example, using just
`minecraft:apple` will automatically create the appropriate prize with
reasonable defaults as shown below. 

```java
prizes = [
    ["minecraft:apple", 1]
]
```

More examples can be found in the docs for each type of component as well as in
the default config. Additional questions can be asked in the `#cratecrate`
channel in our [Discord Server](https://discord.gg/zWqnAa9KRn).
