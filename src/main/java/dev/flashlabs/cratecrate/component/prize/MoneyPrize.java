package dev.flashlabs.cratecrate.component.prize;

import com.google.common.collect.ImmutableList;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.Type;
import dev.flashlabs.cratecrate.internal.Config;
import dev.flashlabs.cratecrate.internal.SerializationException;
import dev.flashlabs.cratecrate.internal.Serializers;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DoublePlantTypes;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Tuple;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
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
    public Text name(Optional<BigDecimal> amount) {
        return name
            .map(s -> {
                s = s.replaceAll("\\$\\{amount}", amount.map(String::valueOf).orElse("${amount}"));
                return TextSerializers.FORMATTING_CODE.deserialize(s);
            })
            .orElseGet(() -> currency
                .orElse(Sponge.getServiceManager().provideUnchecked(EconomyService.class).getDefaultCurrency())
                .format(amount.orElse(BigDecimal.ZERO))
            );
    }

    /**
     * Returns the lore of this prize, defaulting to an empty list. If a
     * reference value is given, it replaces {@code ${amount}}.
     */
    @Override
    public List<Text> lore(Optional<BigDecimal> amount) {
        return lore.orElse(ImmutableList.of()).stream()
            .map(s -> {
                s = s.replaceAll("\\$\\{amount}", amount.map(String::valueOf).orElse("${amount}"));
                return TextSerializers.FORMATTING_CODE.deserialize(s);
            })
            .collect(Collectors.toList());
    }

    /**
     * Returns the icon of this prize, defaulting to a sunflower. If the icon
     * does not have a defined display name or lore, it is set to this prize's
     * name/lore.
     */
    @Override
    public ItemStack icon(Optional<BigDecimal> value) {
        ItemStack base = icon.map(ItemStackSnapshot::createStack)
            .orElseGet(() -> ItemStack.builder()
                .itemType(ItemTypes.DOUBLE_PLANT)
                .add(Keys.DOUBLE_PLANT_TYPE, DoublePlantTypes.SUNFLOWER)
                .build()
            );
        if (!base.get(Keys.DISPLAY_NAME).isPresent()) {
            base.offer(Keys.DISPLAY_NAME, name(value));
        }
        if (lore.isPresent() && !base.get(Keys.ITEM_LORE).isPresent()) {
            base.offer(Keys.ITEM_LORE, lore(value));
        }
        return base;
    }

    @Override
    public boolean give(User user, BigDecimal amount) {
        EconomyService service = Sponge.getServiceManager().provideUnchecked(EconomyService.class);
        return service.getOrCreateAccount(user.getUniqueId())
            .map(a -> {
                TransactionResult result = a.deposit(currency.orElse(service.getDefaultCurrency()), amount, Sponge.getCauseStackManager().getCurrentCause());
                return result.getResult() == ResultType.SUCCESS;
            })
            .orElse(false);
    }

    private static final class MoneyPrizeType extends Type<MoneyPrize, BigDecimal> {

        private MoneyPrizeType() {
            super("Money", CrateCrate.get().getContainer());
        }

        /**
         * Matches nodes having a {@code money} child or with a string value
         * prefixed with {@code '$'}.
         */
        @Override
        public boolean matches(ConfigurationNode node) {
            return !node.getNode("money").isVirtual() || Optional.ofNullable(node.getString())
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
            Optional<String> name = Optional.ofNullable(node.getNode("name").getString());
            Optional<ImmutableList<String>> lore = node.getNode("lore").isList()
                ? Optional.of(node.getChildrenList().stream()
                    .map(s -> s.getString(""))
                    .collect(ImmutableList.toImmutableList())
                )
                : Optional.empty();
            Optional<ItemStackSnapshot> icon = !node.getNode("icon").isVirtual()
                ? Optional.of(Serializers.ITEM_STACK.deserialize(node.getNode("icon")).createSnapshot())
                : Optional.empty();
            Optional<Currency> currency = !node.getNode("money", "currency").isVirtual()
                ? Optional.of(Serializers.CURRENCY.deserialize(node.getNode("money", "currency")))
                : Optional.empty();
            return new MoneyPrize(String.valueOf(node.getKey()), name, lore, icon, currency);
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
                prize = new MoneyPrize("MoneyPrize@" + Arrays.toString(node.getPath()), prize.name, prize.lore, prize.icon, prize.currency);
                Config.PRIZES.put(prize.id, prize);
            } else {
                String identifier = Optional.ofNullable(node.getString()).orElse("");
                if (Config.PRIZES.containsKey(identifier)) {
                    prize = (MoneyPrize) Config.PRIZES.get(identifier);
                } else if (identifier.matches("\\$[0-9]+(\\.[0-9]+)?")) {
                    prize = (MoneyPrize) Config.PRIZES.computeIfAbsent("$", k -> new MoneyPrize(k, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));
                    return Tuple.of(prize, new BigDecimal(identifier.substring(1)));
                } else if (identifier.startsWith("$")) {
                    Optional<Currency> currency = Sponge.getRegistry().getType(Currency.class, identifier.contains(":") ? identifier.substring(1) : "minecraft:" + identifier.substring(1));
                    prize = new MoneyPrize(identifier, Optional.empty(), Optional.empty(), Optional.empty(), currency);
                    Config.PRIZES.put(prize.id, prize);
                } else {
                    throw new AssertionError(identifier);
                }
            }
            //TODO: Validate reference value counts
            BigDecimal amount = new BigDecimal((!values.isEmpty() ? values.get(0) : node.getNode("amount")).getString("0"));
            return Tuple.of(prize, amount);
        }

        @Override
        public void reserializeReference(ConfigurationNode node, Tuple<MoneyPrize, BigDecimal> reference) throws SerializationException {
            throw new UnsupportedOperationException(); //TODO
        }

    }

}
