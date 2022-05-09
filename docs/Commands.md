# Commands

The available commands are as follows:

 - [`/crate`](#crate)
 - [`/crate crate`](#crate-crate)
    - [`/crate crate give`](#crate-crate-give)
    - [`/crate crate list`](#crate-crate-list)
    - [`/crate crate open`](#crate-crate-open)
 - [`/crate key`](#crate-key)
   - [`/crate key balance`](#crate-key-balance)
   - [`/crate key give`](#crate-key-give)
   - [`/crate key list`](#crate-crate-list)
   - [`/crate key take`](#crate-key-take)
 - [`/crate location`](#crate-location)
    - [`/crate location delete`](#crate-location-delete)
    - [`/crate location set`](#crate-location-set)
 - [`/crate prize`](#crate-prize)
    - [`/crate prize give`](#crate-prize-give)
- [`/crate reward`](#crate-reward)
   - [`/crate reward give`](#crate-reward-give)

---

## `/crate`

 - Usage: `/crate [subcommand ...]`
    - `[subcommand ...]`: A subcommand (`crate`/`key`/`location`/`prize`/
      `reward`) and arguments.
 - Aliases: `/cratecrate`
 - Permission: `cratecrate.command.base`

The base command for CrateCrate.

---

## `/crate crate`

 - Usage: `/crate crate [subcommand ...]`
    - `[subcommand ...]`: A crate subcommand (`give`/`list`/`open`) and
      arguments.
 - Permission: `cratecrate.command.crate.base`

The base command for working with crates.

### `/crate crate give`

 - Usage: `/crate crate give [player] <crate> <reward> [position]`
    - `[player]`: A username or selector matching a single player, defaulting to
      the player executing this command.
    - `<crate>`: A registered crate id.
    - `<reward>`: A registered reward id.
    - `[position]`: An xyz position or either `#me`/`#target` (source position /
      target block), defaulting to the player's position.
 - Permission: `cratecrate.command.crate.give.base`

Gives a reward to a player as if received through a crate. Compared to `open`,
`give` does not include any opening effects and gives a specific reward.

### `/crate crate list`

 - Usage: `/crate crate list [--text]`
    - `[--text]`: List crates through text rather than GUI (always enabled for
      console).
 - Aliases: `/crates`
 - Permission: `cratecrate.command.crate.list.base`

Displays all crates the source has permission to open.

### `/crate crate open`

 - Usage: `/crate crate open [player] <crate> [position]`
    - `[player]`: A username or selector matching a single player, defaulting to
      the player executing this command.
    - `<crate>`: A registered crate id.
    - `[position]`: An xyz position or either `#me`/`#target` (source position /
      target block), defaulting to the player's position.
 - Permission: `cratecrate.command.crate.open.base`

Opens a crate for a player, which bypasses keys. Compared to `give`, `open`
includes any opening effects and rolls a random reward.

---

## `/crate key`

 - Usage: `/crate key [subcommand ...]`
    - `[subcommand ...]`: A key subcommand (`balance`/`give`/`list`/`take`) and
      arguments.
 - Permission: `cratecrate.command.key.base`

The base command for working with keys.

### `/crate key balance`

 - Usage: `/crate key balance [user] <key>`
    - `<user>`: A username or selector matching a single user (online/offline),
      defaulting to the player executing this command.
    - `<key>`: A registered key id.
 - Permission: `cratecrate.command.key.balance.base`
    - `cratecrate.command.key.balance.other`: View the balance of another
      `user`.

Displays the user's balance of a single key. To display all keys, use `list`.

### `/crate key give`

 - Usage: `/crate key give [user] <key> <quantity>`
    - `[user]`: A username or selector matching a single user (online/offline),
      defaulting to the player executing this command.
    - `<key>`: A registered key id.
    - `<quantity>`: An integer quantity, which must be greater than `0` and no
      greater than the user's current balance.
 - Permission: `cratecrate.command.key.give.base`

Gives a key to a user.

### `/crate key list`

 - Usage: `/crate key list [user] [--text]`
    - `[user]`: A username or selector matching a single user (online/offline),
      defaulting to the player executing this command.
    - `[--text]`: List keys through text rather than GUI (always enabled for
      console).
 - Permission: `cratecrate.command.key.list.base`
    - `cratecrate.command.key.list.other`: View the keys of another `user`.

Lists all of a user's keys.

### `/crate key take`

 - Usage: `/crate key take [user] <key> <quantity>`
    - `[user]`: A username or selector matching a single user (online/offline),
      defaulting to the player executing this command.
    - `<key>`: A registered key id.
    - `<quantity>`: An integer quantity, greater than `0`
 - Permission: `cratecrate.command.key.give.base`

Takes a quantity of a key from a user.

---

## `/crate location`

 - Usage: `/crate location [subcommand ...]`
    - `[subcommand ...]`: A location subcommand (`delete`/`set`) and arguments.
 - Permission: `cratecrate.command.location.base`

The base command for working with locations.

### `/crate location delete`

 - Usage: `/crate location delete <location>`
    - `<location>`: A world (optional for players) and xyz position.
 - Permission: `cratecrate.command.location.delete.base`

Deletes a registered crate location.

### `/crate location set`

 - Usage: `/crate location set <location> <crate>`
    - `<location>`: A world (optional for players) and xyz position.
   - `<crate>`: A registered crate id.
 - Permission: `cratecrate.command.location.set.base`

Sets a registered crate location. This command does not change the block at the
given location; whatever block is present will work with the crate.

---

## `/crate prize`

 - Usage: `/crate prize [subcommand ...]`
    - `[subcommand ...]`: A prize subcommand (`give`) and arguments.
 - Permission: `cratecrate.command.prize.base`

The base command for working with prizes.

### `/crate prize give`

 - Usage: `/crate prize give [user] <prize> [value]`
    - `[user]`: A username or selector matching a single user (online/offline),
     defaulting to the player executing this command.
    - `<prize>`: A registered prize id.
    - `[value]`: A reference value for the prize (varies by type, see below).
 - Permission: `cratecrate.command.prize.give.base`

Gives a prize to a user. The `value` argument depends on the type of the prize:

 - `Command`: The `${value}` placeholder
 - `Item`: The integer quantity, greater than `0`
 - `Money`: The decimal amount, greater than `0`

---

## `/crate reward`

 - Usage: `/crate reward [subcommand ...]`
    - `[subcommand ...]`: A reward subcommand (`give`) and arguments.
 - Permission: `cratecrate.command.reward.base`

The base command for working with rewards.

### `/crate reward give`

 - Usage: `/crate reward give [user] <reward>`
    - `[user]`: A username or selector matching a single user (online/offline),
      defaulting to the player executing this command.
    - `<reward>`: A registered reward id
 - Permission: `cratecrate.command.reward.give.base`

Gives a reward to a user.

---
