package net.eonzenx.spool_ge;

import net.eonzenx.spool_ge.registry_handlers.ItemRegistry;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

public class SpoolGameplayEdits implements ModInitializer
{
	public static final String MOD_ID = "spool_ge";

	@Override
	public void onInitialize() {
		ItemRegistry.init();

		System.out.println("Spool - Gameplay Edits initialize complete");
	}


	public static Identifier newId(String tag) { return new Identifier(MOD_ID, tag); }
}
