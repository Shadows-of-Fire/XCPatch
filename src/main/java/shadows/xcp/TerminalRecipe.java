package shadows.xcp;

import java.util.Set;

import appeng.api.config.Actionable;
import appeng.api.features.INetworkEncodable;
import appeng.api.implementations.items.IAEItemPowerStorage;
import extracells.item.ItemWirelessTerminalUniversal;
import extracells.item.WirelessTerminalType;
import extracells.registries.ItemEnum;
import extracells.util.UniversalTerminal;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class TerminalRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

	@ObjectHolder("extracells:terminal.universal.wireless")
	public static final Item TERMINAL = null;

	@Override
	public boolean matches(InventoryCrafting inv, World world) {
		ItemStack terminal = ItemStack.EMPTY;
		ItemStack module = ItemStack.EMPTY;
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack s = inv.getStackInSlot(i);
			if (s.getItem() == TERMINAL || UniversalTerminal.isWirelessTerminal(s)) {
				if (!terminal.isEmpty()) return false;
				terminal = s;
			}
			if (UniversalTerminal.isTerminal(s)) {
				if (!module.isEmpty()) return false;
				module = s;
			}
		}

		WirelessTerminalType type = UniversalTerminal.getTerminalType(module);
		Set<WirelessTerminalType> types = ItemWirelessTerminalUniversal.getInstalledModules(terminal);
		if (terminal.isEmpty() || module.isEmpty() || type == null) return false;

		return !types.contains(type);
	}

	@Override
	public ItemStack getRecipeOutput() {
		return ItemEnum.UNIVERSALTERMINAL.getDamagedStack(0);
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		ItemStack terminal = ItemStack.EMPTY;
		ItemStack module = ItemStack.EMPTY;
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack s = inv.getStackInSlot(i);
			if (s.getItem() == TERMINAL || UniversalTerminal.isWirelessTerminal(s)) {
				terminal = s;
			}
			if (UniversalTerminal.isTerminal(s)) {
				module = s;
			}
		}
		return installModule(terminal, UniversalTerminal.getTerminalType(module));
	}

	private static ItemStack installModule(ItemStack terminal, WirelessTerminalType type) {
		boolean isUniversal = terminal.getItem() == TERMINAL;
		if (isUniversal) {
			XCPatch.installModule(terminal, type);
		} else {
			WirelessTerminalType terminalType = UniversalTerminal.getTerminalType(terminal);
			Item itemTerminal = terminal.getItem();
			ItemStack t = new ItemStack(TERMINAL);
			if (itemTerminal instanceof INetworkEncodable) {
				String key = ((INetworkEncodable) itemTerminal).getEncryptionKey(terminal);
				if (key != null) ItemWirelessTerminalUniversal.setEncryptionKey(t, key, null);
			}
			if (itemTerminal instanceof IAEItemPowerStorage) {
				double power = ((IAEItemPowerStorage) itemTerminal).getAECurrentPower(terminal);
				ItemWirelessTerminalUniversal.injectAEPower(t, power, Actionable.MODULATE);
			}
			if (terminal.hasTagCompound()) {
				NBTTagCompound nbt = terminal.getTagCompound();
				if (!t.hasTagCompound()) t.setTagCompound(new NBTTagCompound());
				if (nbt.hasKey("BoosterSlot")) {
					t.getTagCompound().setTag("BoosterSlot", nbt.getTag("BoosterSlot"));
				}
				if (nbt.hasKey("MagnetSlot")) t.getTagCompound().setTag("MagnetSlot", nbt.getTag("MagnetSlot"));
			}
			XCPatch.installModule(t, terminalType);
			t.getTagCompound().setByte("type", (byte) terminalType.ordinal());
			terminal = t;
			XCPatch.installModule(terminal, type);
		}
		return terminal;
	}

	@Override
	public boolean canFit(int width, int height) {
		return width * height >= 2;
	}
}