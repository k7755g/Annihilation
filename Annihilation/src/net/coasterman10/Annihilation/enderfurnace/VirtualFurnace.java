package net.coasterman10.Annihilation.enderfurnace;

import org.bukkit.craftbukkit.v1_7_R1.inventory.CraftInventoryFurnace;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import net.minecraft.server.v1_7_R1.Block;
import net.minecraft.server.v1_7_R1.Blocks;
import net.minecraft.server.v1_7_R1.EntityHuman;
import net.minecraft.server.v1_7_R1.TileEntityFurnace;

public class VirtualFurnace extends TileEntityFurnace {
	public VirtualFurnace(EntityHuman entity) {
		world = entity.world;
	}

	@Override
	public boolean a(EntityHuman entity) {
		return true;
	}

	@Override
	public int p() {
		return 0;
	}

	@Override
	public Block q() {
		return Blocks.FURNACE;
	}

	@Override
	public void update() {

	}

	@Override
	public InventoryHolder getOwner() {
		return new InventoryHolder() {
			@Override
			public Inventory getInventory() {
				return new CraftInventoryFurnace(VirtualFurnace.this);
			}
		};
	}
}
