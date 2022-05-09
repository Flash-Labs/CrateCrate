# Permissions

The available permissions are as follows:

 - [`cratecrate.command.base`](#cratecratecommandbase)
    - [`cratecrate.command.crate.list.base`](#cratecratecommandcratelistbase)
    - [`cratecrate.command.key.list.base`](#cratecratecommandkeylistbase)
 - [`cratecrate.crates.<id>.base`](#cratecratecratesidbase)
    - [`cratecrate.crates.<id>.preview`](#cratecratecratesidpreview)

## `cratecrate.command.base`

The base command for CrateCrate, which is required to use most commands. For
individual command permissions, see [Commands](Commands.md).

### `cratecrate.command.crate.list.base`

The base command for `/crates`, which is the same as `/crate crate list`. This
permission can be given directly to allow `/crates` but not `/crate crate list`.

### `cratecrate.command.key.list.base`

The base command for `/keys`, which is the same as `/crate key list`. This
permission can be given directly to allow `/keys` but not `/crate key list`.

## `cratecrate.crates.<id>.base`

The base permission for a crate, which is required to interact with a crate.

### `cratecrate.crates.<id>.preview`

The preview permission for a crate, which allows previewing the available
rewards.
