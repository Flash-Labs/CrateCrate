package dev.flashlabs.cratecrate.component.prize;

import com.google.common.collect.ImmutableList;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.Type;
import dev.flashlabs.cratecrate.internal.Config;
import dev.flashlabs.cratecrate.internal.Serializers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public final class MoneyPrize extends Prize<BigDecimal> {

    public static final Type<MoneyPrize, BigDecimal> TYPE = new MoneyPrizeType();

    private final Optional<String> name;
    private final Optional<ImmutableList<String>> lore;
    private final Optional<ItemStackSnapshot> icon;
    private final Optional<Currency> currency;

    private MoneyPrize(
        String id,
        Optional<String> name,
        Optional<ImmutableList<String>> lore,
        Optional<ItemStackSnapshot> icon,
        Optional<Currency> currency
    ) {
        super(id);
        this.name = name;
        this.lore = lore;
        this.icon = icon;
        this.currency = currency;
    }

    /**
     * Returns the name of this prize, defaulting to the format method of the
     * currency. If a reference value is given and the name is defined, it
     * replaces {@code ${amount}}. Else, if a reference value is not given and
     * the name is not defined then {@code BigDecimal.ZERO} is used as the
     * default amount.
     */
    @Override
    public Component name(Optional<BigDecimal> amount) {
        return name.map(s -> {
            s = s.replaceAll("\\$\\{amount}", amount.map(String::valueOf).orElse("${amount}"));
            return LegacyComponentSerializer.legacyAmpersand().deserialize(s).asComponent();
        }).orElseGet(() -> {
            var service = Sponge.server().serviceProvider().economyService().get();
            return currency.orElse(service.defaultCurrency()).format(amount.orElse(BigDecimal.ZERO));
        });
    }

    /**
     * Returns the lore of this prize, defaulting to an empty list. If a
     * reference value is given, it replaces {@code ${amount}}.
     */
    @Override
    public List<Component> lore(Optional<BigDecimal> amount) {
        return lore.orElseGet(ImmutableList::of).stream().map(s -> {
            s = s.replaceAll("\\$\\{amount}", amount.map(String::valueOf).orElse("${amount}"));
            return LegacyComponentSerializer.legacyAmpersand().deserialize(s).asComponent();
        }).toList();
    }

    /**
     * Returns the icon of this prize, defaulting to a sunflower. If the icon
     * does not have a defined display name or lore, it is set to this prize's
     * name/lore.
     */
    @Override
    public ItemStack icon(Optional<BigDecimal> value) {
        var base = icon.map(ItemStackSnapshot::createStack)
            .orElseGet(() -> ItemStack.of(ItemTypes.SUNFLOWER, 1));
        if (base.get(Keys.CUSTOM_NAME).isEmpty()) {
            base.offer(Keys.CUSTOM_NAME, name(value));
        }
        if (lore.isPresent() && base.get(Keys.LORE).isEmpty()) {
            base.offer(Keys.LORE, lore(value));
        }
        return base;
    }

    @Override
    public boolean give(User user, BigDecimal amount) {
        var service = Sponge.server().serviceProvider().economyService().get();
        var account = service.findOrCreateAccount(user.uniqueId()).orElse(null);
        if (account != null) {
            var result = account.deposit(currency.orElse(service.defaultCurrency()), amount);
            return result.result() == ResultType.SUCCESS;
        } else {
            return false;
        }
    }

    private static final class MoneyPrizeType extends Type<MoneyPrize, BigDecimal> {

        private MoneyPrizeType() {
            super("Money", CrateCrate.container());
        }

        /**
         * Matches nodes having a {@code money} child or with a string value
         * prefixed with {@code '$'}.
         */
        @Override
        public boolean matches(ConfigurationNode node) {
            return node.hasChild("money") || Optional.ofNullable(node.getString())
                .map(s -> s.startsWith("$"))
                .orElse(false);
        }

        /**
         * Deserializes a money prize, defined as:
         *
         * <pre>{@code
         * MoneyPrize:
         *     name: Optional<String>
         *     lore: Optional<List<String>>
         *     icon: Optional<ItemStack>
         *     money: Object
         *         currency: String (a registered currency)
         * }</pre>
         */
        @Override
        public MoneyPrize deserializeComponent(ConfigurationNode node) throws SerializationException {
            var name = Optional.ofNullable(node.node("name").get(String.class));
            var lore = node.node("lore").isList()
                ? Optional.ofNullable(node.node("lore").getList(String.class)).map(ImmutableList::copyOf)
                : Optional.<ImmutableList<String>>empty();
            var icon = node.hasChild("icon")
                ? Optional.of(Serializers.ITEM_STACK.deserialize(node.node("icon")).createSnapshot())
                : Optional.<ItemStackSnapshot>empty();
            var currency = node.hasChild("money", "currency")
                ? Optional.of(Serializers.CURRENCY.deserialize(node.node("money", "currency")))
                : Optional.<Currency>empty();
            return new MoneyPrize(String.valueOf(node.key()), name, lore, icon, currency);
        }

        @Override
        public void reserializeComponent(ConfigurationNode node, MoneyPrize component) throws SerializationException {
            throw new UnsupportedOperationException(); //TODO
        }

        /**
         * Deserializes a money prize reference, defined as:
         *
         * <pre>{@code
         * MoneyPrizeReference:
         *     node:
         *        MoneyPrize |
         *        String (MoneyPrize id or prefixed with '$')
         *     values: [
         *        Optional<Decimal> (only allowed with String MoneyPrize id)
         *     ]
         * }</pre>
         */
        @Override
        public Tuple<MoneyPrize, BigDecimal> deserializeReference(ConfigurationNode node, List<? extends ConfigurationNode> values) throws SerializationException {
            MoneyPrize prize;
            if (node.isMap()) {
                prize = deserializeComponent(node);
                prize = new MoneyPrize("MoneyPrize@" + node.path(), prize.name, prize.lore, prize.icon, prize.currency);
                Config.PRIZES.put(prize.id, prize);
            } else {
                var identifier = Optional.ofNullable(node.getString()).orElse("");
                if (Config.PRIZES.containsKey(identifier)) {
                    prize = (MoneyPrize) Config.PRIZES.get(identifier);
                } else if (identifier.matches("\\$[0-9]+(\\.[0-9]+)?")) {
                    prize = (MoneyPrize) Config.PRIZES.computeIfAbsent("$", k -> new MoneyPrize(k, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));
                    return Tuple.of(prize, new BigDecimal(identifier.substring(1)));
                } else if (identifier.startsWith("$")) {
                    //TODO: Currency registry is empty and not accessible via EconomyService
                    var currency = RegistryTypes.CURRENCY.get().findValue(ResourceKey.resolve(identifier.substring(1))).get();
                    prize = new MoneyPrize(identifier, Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(currency));
                    Config.PRIZES.put(prize.id, prize);
                } else {
                    throw new AssertionError(identifier);
                }
            }
            //TODO: Validate reference value counts
            var amount = new BigDecimal((!values.isEmpty() ? values.get(0) : node.node("amount")).getString("0"));
            return Tuple.of(prize, amount);
        }

        @Override
        public void reserializeReference(ConfigurationNode node, Tuple<MoneyPrize, BigDecimal> reference) throws SerializationException {
            throw new UnsupportedOperationException(); //TODO
        }

    }

}
