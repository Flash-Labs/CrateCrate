package dev.flashlabs.cratecrate.component.prize;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import dev.flashlabs.cratecrate.CrateCrate;
import dev.flashlabs.cratecrate.component.Type;
import dev.flashlabs.cratecrate.internal.Config;
import dev.flashlabs.cratecrate.internal.Serializers;
import dev.willbanders.storm.Storm;
import dev.willbanders.storm.config.Node;
import dev.willbanders.storm.serializer.SerializationException;
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
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Tuple;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
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
     * currency. If a reference value is given, it replaces {@code ${amount}}.
     */
    @Override
    public Text name(Optional<BigDecimal> amount) {
        return name
            .map(s -> {
                s = s.replaceAll("\\$\\{amount}", amount.map(String::valueOf).orElse("${amount}"));
                return TextSerializers.FORMATTING_CODE.deserialize("&f" + s);
            })
            .orElseGet(() -> Text.of(TextColors.WHITE, currency
                .orElse(Sponge.getServiceManager().provideUnchecked(EconomyService.class).getDefaultCurrency())
                .format(amount.orElse(BigDecimal.ZERO))
                .replace(Pattern.compile("0+(\\.0+)"), Text.of("${amount}"))
            ));
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
                return TextSerializers.FORMATTING_CODE.deserialize("&f" + s);
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
        if (!base.get(Keys.ITEM_LORE).isPresent()) {
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
        public boolean matches(Node node) {
            return node.get("money").getType() != Node.Type.UNDEFINED
                || node.getType() == Node.Type.STRING && node.get(Storm.STRING).startsWith("$");
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
        public MoneyPrize deserializeComponent(Node node) throws SerializationException {
            Optional<String> name = node.get("name", Storm.STRING.optional());
            Optional<ImmutableList<String>> lore = node.get("lore", Storm.LIST.of(Storm.STRING).optional()).map(ImmutableList::copyOf);
            Optional<ItemStackSnapshot> icon = node.get("icon", Serializers.ITEM_STACK.optional()).map(ItemStack::createSnapshot);
            Optional<Currency> currency = node.get("money.currency", Serializers.CURRENCY.optional());
            return new MoneyPrize(String.valueOf(node.getKey()), name, lore, icon, currency);
        }

        @Override
        public void reserializeComponent(Node node, MoneyPrize component) throws SerializationException {
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
        public Tuple<MoneyPrize, BigDecimal> deserializeReference(Node node, List<? extends Node> values) throws SerializationException {
            MoneyPrize prize;
            if (node.getType() == Node.Type.OBJECT) {
                prize = deserializeComponent(node);
                prize = new MoneyPrize("MoneyPrize@" + node.getPath(), prize.name, prize.lore, prize.icon, prize.currency);
                Config.PRIZES.put(prize.id, prize);
            } else {
                String identifier = node.get(Storm.STRING);
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
            if (values.isEmpty() && node.get("amount").getType() == Node.Type.UNDEFINED) {
                throw new SerializationException(node, "Expected a reference value for the amount.");
            }
            BigDecimal amount = (!values.isEmpty() ? values.get(values.size() - 1) : node.get("amount"))
                .get(Storm.BIG_DECIMAL.range(Range.greaterThan(BigDecimal.ZERO)));
            return Tuple.of(prize, amount);
        }

        @Override
        public void reserializeReference(Node node, Tuple<MoneyPrize, BigDecimal> reference) throws SerializationException {
            throw new UnsupportedOperationException(); //TODO
        }

    }

}
