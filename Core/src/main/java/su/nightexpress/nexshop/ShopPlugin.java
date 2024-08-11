package su.nightexpress.nexshop;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.auction.AuctionManager;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.config.Perms;
import su.nightexpress.nexshop.currency.CurrencyManager;
import su.nightexpress.nexshop.currency.command.CurrencyCommand;
import su.nightexpress.nexshop.data.DataHandler;
import su.nightexpress.nexshop.data.UserManager;
import su.nightexpress.nexshop.data.user.ShopUser;
import su.nightexpress.nexshop.hook.HookId;
import su.nightexpress.nexshop.hook.PlaceholderHook;
import su.nightexpress.nexshop.shop.ProductHandlerRegistry;
import su.nightexpress.nexshop.shop.ShopManager;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.compatibility.WorldGuardFlags;
import su.nightexpress.nexshop.shop.impl.handler.ItemsAdderHandler;
import su.nightexpress.nexshop.shop.impl.handler.MMOItemsHandler;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nightcore.NightDataPlugin;
import su.nightexpress.nightcore.command.experimental.ImprovedCommands;
import su.nightexpress.nightcore.command.experimental.impl.ReloadCommand;
import su.nightexpress.nightcore.command.experimental.node.ChainedNode;
import su.nightexpress.nightcore.config.PluginDetails;
import su.nightexpress.nightcore.util.Plugins;

public class ShopPlugin extends NightDataPlugin<ShopUser> implements ImprovedCommands {

    private DataHandler dataHandler;
    private UserManager userManager;

    private CurrencyManager currencyManager;
    private ShopManager     shopManager;

    private VirtualShopModule virtualShop;
    private ChestShopModule   chestShop;
    private AuctionManager    auction;

    @Override
    @NotNull
    protected PluginDetails getDefaultDetails() {
        return PluginDetails.create("Shop", new String[]{"excellentshop", "eshop"})
            .setConfigClass(Config.class)
            .setLangClass(Lang.class)
            .setPermissionsClass(Perms.class);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (this.getServer().getPluginManager().getPlugin(HookId.WORLD_GUARD) != null) {
            WorldGuardFlags.setupFlag();
        }
    }

    @Override
    public void enable() {
        this.registerCommands();

        this.dataHandler = new DataHandler(this);
        this.dataHandler.setup();

        this.userManager = new UserManager(this);
        this.userManager.setup();

        this.currencyManager = new CurrencyManager(this);
        this.currencyManager.setup();
        if (!this.currencyManager.hasCurrency()) {
            this.error("No currencies are available! Plugin will be disabled.");
            return;
        }

        this.registerProductHandlers();

        this.shopManager = new ShopManager(this);
        this.shopManager.setup();

        if (Config.MODULES_VIRTUAL_SHOP_ENABLED.get()) {
            this.virtualShop = new VirtualShopModule(this);
            this.virtualShop.setup();
        }
        if (Config.MODULES_CHEST_SHOP_ENABLED.get()) {
            this.chestShop = new ChestShopModule(this);
            this.chestShop.setup();
        }
        if (Config.MODULES_AUCTION_ENABLED.get()) {
            this.auction = new AuctionManager(this);
            this.auction.setup();
        }

        if (Plugins.hasPlaceholderAPI()) {
            PlaceholderHook.setup(this);
        }
    }

    @Override
    public void disable() {
        if (Plugins.hasPlaceholderAPI()) {
            PlaceholderHook.shutdown();
        }

        if (this.shopManager != null) {
            this.shopManager.shutdown();
        }

        if (this.virtualShop != null) {
            this.virtualShop.shutdown();
            this.virtualShop = null;
        }
        if (this.chestShop != null) {
            this.chestShop.shutdown();
            this.chestShop = null;
        }
        if (this.auction != null) {
            this.auction.shutdown();
            this.auction = null;
        }
        if (this.currencyManager != null) {
            this.currencyManager.shutdown();
            this.currencyManager = null;
        }
    }

    private void registerProductHandlers() {
        ProductHandlerRegistry.register(ProductHandlerRegistry.BUKKIT_ITEM);
        ProductHandlerRegistry.register(ProductHandlerRegistry.BUKKIT_COMMAND);

        if (Plugins.isInstalled(HookId.ITEMS_ADDER)) {
            ProductHandlerRegistry.register(new ItemsAdderHandler());
        }
        if (Plugins.isInstalled(HookId.MMOITEMS)) {
            ProductHandlerRegistry.register(new MMOItemsHandler());
        }
    }

    private void registerCommands() {
        ChainedNode rootNode = this.getRootNode();

        CurrencyCommand.inject(this, rootNode);
        ReloadCommand.inject(this, rootNode, Perms.COMMAND_RELOAD);
    }

    @Override
    @NotNull
    public DataHandler getData() {
        return this.dataHandler;
    }

    @NotNull
    @Override
    public UserManager getUserManager() {
        return userManager;
    }

    @NotNull
    public CurrencyManager getCurrencyManager() {
        return currencyManager;
    }

    @NotNull
    public ShopManager getShopManager() {
        return shopManager;
    }

    @Nullable
    public VirtualShopModule getVirtualShop() {
        return this.virtualShop;
    }

    @Nullable
    public ChestShopModule getChestShop() {
        return this.chestShop;
    }

    @Nullable
    public AuctionManager getAuction() {
        return this.auction;
    }
}
