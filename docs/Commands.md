# Commands

The available commands are as follows:

 - [`/cratecrate`](#cratecrate)
 - [`/cratecrate crate`](#cratecrate-crate)
    - [`/cratecrate crate open`](#cratecrate-crate-open)
    - [`/cratecrate crate give`](#cratecrate-crate-give)
 - [`/cratecrate reward`](#cratecrate-reward)
    - [`/cratecrate reward give`](#cratecrate-reward-give)
 - [`/cratecrate prize`](#cratecrate-prize)
    - [`/cratecrate prize give`](#cratecrate-prize-give)
 - [`/cratecrate key`](#cratecrate-key)
    - [`/cratecrate key balance`](#cratecrate-key-balance)
    - [`/cratecrate key give`](#cratecrate-key-give)
    - [`/cratecrate key take`](#cratecrate-key-take)
 - [`/cratecrate location`](#cratecrate-location)
    - [`/cratecrate location set`](#cratecrate-location-set)
    - [`/cratecrate location delete`](#cratecrate-location-delete)

---

## `/cratecrate`

 - Usage: `/cratecrate [subcommand]`
 - Aliases: `/crate`
 - Permission: `cratecrate.command.base`

The base command for CrateCrate, which displays plugin information.

---

## `/cratecrate crate`

 - Usage: `/cratecrate crate [subcommand]`
 - Permission: `cratecrate.command.crate.base`

The base command for working with crates.

### `/cratecrate crate open`

 - Usage: `/cratecrate crate open <player> <crate> [location]`
    - `<player>`: The player
    - `<crate>`: The crate id
    - `[location]`: The location (defaults to the player's location)
 - Permission: `cratecrate.command.crate.open.base`

Opens a crate for a player, which bypasses keys. Unlike `give`, `open` includes
any opening effects and rolls a random reward.

### `/cratecrate crate give`

 - Usage: `/cratecrate crate give <player> <crate> [location] <reward>`
    - `<player>`: The player
    - `<crate>`: The crate id
    - `[location]`: The location (defaults to the player's location)
    - `<reward>`: The reward id
 - Permission: `cratecrate.command.crate.give.base`

Gives a reward to a player as if they won it through a crate. Unlike `open`,
`give` does not include any opening effects and gives a specific reward.

---

## `/cratecrate reward`

 - Usage: `/cratecrate reward [subcommand]`
 - Permission: `cratecrate.command.reward.base`

The base command for working with rewards.

### `/cratecrate reward give`

 - Usage: `/cratecrate reward give <user> <reward>`
    - `<user>`: The user
    - `<reward>`: The reward id
 - Permission: `cratecrate.command.reward.give.base`

Gives a reward to a user.

---

## `/cratecrate prize`

- Usage: `/cratecrate prize [subcommand]`
- Permission: `cratecrate.command.prize.base`

The base command for working with prizes.

### `/cratecrate prize give`

 - Usage: `/cratecrate prize give <user> <prize> [value]`
    - `<user>`: The user
    - `<prize>`: The prize id
    - `[value]`: The reference value of the prize (see below)
 - Permission: `cratecrate.command.prize.give.base`

Gives a prize to a user. The `value` argument depends on the type of the prize:

 - `CommandPrize`: A `String` used as the `${value}` placeholder
 - `ItemPrize`: An `Integer` used as the `quantity`

---

## `/cratecrate key`

- Usage: `/cratecrate key [subcommand]`
- Permission: `cratecrate.command.key.base`

The base command for working with keys.

### `/cratecrate key balance`

 - Usage: `/cratecrate key balance <user> <key>`
    - `<user>`: The user
    - `<key>`: The key id
 - Permission: `cratecrate.command.key.balance.base`
    - `cratecrate.command.key.balance.other`: View the balance of another `user`

Displays the user's balance of a key.

### `/cratecrate key give`

 - Usage: `/cratecrate key give <user> <key> <quantity>`
    - `<user>`: The user
    - `<key>`: The key id
    - `<quantity>`: An `Integer` quantity, greater than `0`
 - Permission: `cratecrate.command.key.give.base`

Gives a quantity of keys to the user.

### `/cratecrate key take`

 - Usage: `/cratecrate key take <user> <key> <quantity>`
    - `<user>`: The user
    - `<key>`: The key id
    - `<quantity>`: An `Integer` quantity, greater than `0`
 - Permission: `cratecrate.command.key.take.base`

Takes a quantity of keys from the user.

---

## `/cratecrate location`

- Usage: `/cratecrate location [subcommand]`
- Permission: `cratecrate.command.location.base`

The base command for working with locations.

### `/cratecrate location set`

- Usage: `/cratecrate location set <location> <crate>`
   - `<location>`: The location
   - `<crate>`: The crate id
- Permission: `cratecrate.command.location.set.base`

Sets a location as a crate. This command does not change the block at the
location; placing a block normally or using an existing block works as expected.

### `/cratecrate location delete`

- Usage: `/cratecrate location delete <location>`
   - `<location>`: The location
- Permission: `cratecrate.command.location.delete.base`

Deletes a location.

---
