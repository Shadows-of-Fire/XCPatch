package shadows.xcp;

import extracells.item.WirelessTerminalType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistryModifiable;

@Mod(modid = XCPatch.MODID, name = XCPatch.MODNAME, version = XCPatch.VERSION, dependencies = "required-after:extracells;before:mekanism@[1.12.2-9.8.2,)")
public class XCPatch {

	public static final String MODID = "xcpatch";
	public static final String MODNAME = "Extra Cells Patch";
	public static final String VERSION = "1.0.2";

	static ResourceLocation brokenRecipe = new ResourceLocation("extracells:universalcraftingterminalrecipe");

	public XCPatch() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void post(Register<IRecipe> e) {
		((IForgeRegistryModifiable<?>) ForgeRegistries.RECIPES).remove(brokenRecipe);
		ForgeRegistries.RECIPES.register(new TerminalRecipe().setRegistryName(MODID, "unbroken_terminal"));
	}

	static void installModule(ItemStack itemStack, WirelessTerminalType module) {
		if (isInstalled(itemStack, module)) return;
		byte install = (byte) (1 << module.ordinal());
		NBTTagCompound tag = itemStack.getTagCompound();
		if (tag == null) {
			tag = new NBTTagCompound();
			itemStack.setTagCompound(tag);
		}
		byte installed;
		if (tag.hasKey("modules")) installed = (byte) (tag.getByte("modules") + install);
		else installed = install;

		tag.setByte("modules", installed);
	}

	static boolean isInstalled(ItemStack itemStack, WirelessTerminalType module) {
		NBTTagCompound tag = itemStack.getTagCompound();
		if (tag == null) {
			tag = new NBTTagCompound();
			itemStack.setTagCompound(tag);
		}
		byte installed = 0;
		if (tag.hasKey("modules")) installed = tag.getByte("modules");
		return (1 == (installed >> module.ordinal()) % 2);
	}

}
