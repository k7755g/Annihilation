package net.coasterman10.Annihilation.kits;

public enum KitType {
	CIVILIAN(new CivilianKit()), WARRIOR(new WarriorKit()), MINER(new MinerKit());

	private AbstractKit kit;

	private KitType(AbstractKit kit) {
		this.kit = kit;
	}

	public AbstractKit getKitClass() {
		return kit;
	}

	public static KitType getKitType(String name) {
		for (KitType type : values()) {
			if (type.name().equalsIgnoreCase(name))
				return type;
		}
		return null;
	}
}
